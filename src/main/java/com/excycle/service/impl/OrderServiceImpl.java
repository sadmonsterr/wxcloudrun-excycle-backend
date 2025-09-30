package com.excycle.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.dto.OrderQueryRequest;
import com.excycle.entity.Order;
import com.excycle.entity.OrderItems;
import com.excycle.entity.User;
import com.excycle.enums.OrderStatus;
import com.excycle.mapper.OrderItemsMapper;
import com.excycle.mapper.OrderMapper;
import com.excycle.mapper.UserMapper;
import com.excycle.service.OrderService;
import com.excycle.vo.OrderItemsVO;
import com.excycle.vo.OrderVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;

    private final OrderItemsMapper orderItemsMapper;

    @Autowired
    private UserMapper userMapper;

    // TODO dynamic load
    private static final Map<String, String> ITEM_NAME_BY_ID = Collections.unmodifiableMap(new HashMap<String, String>() {{
        put("1", "轮胎");
        put("2", "机油");
        put("3", "电池");
        put("4", "火花塞");
    }});

    public OrderServiceImpl(OrderMapper orderMapper, OrderItemsMapper orderItemsMapper) {
        this.orderMapper = orderMapper;
        this.orderItemsMapper = orderItemsMapper;
    }

    @Override
    public Page<OrderVO> getOrderPage(OrderQueryRequest request) {
        Page<Order> page = new Page<>(request.getPage(), request.getSize());
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<Order>()
                .like(StringUtils.isNotBlank(request.getOrderNumber()),  Order::getOrderNo, request.getOrderNumber())
                .eq(StringUtils.isNotBlank(request.getStatus()), Order::getStatus, request.getStatus())
                .eq(StringUtils.isNotBlank(request.getOpenId()), Order::getOpenId, request.getOpenId())
                .eq(StringUtils.isNotBlank(request.getDriverId()), Order::getDriverId, request.getDriverId())
                .orderByDesc(Order::getId);
        Page<Order> orderPage = orderMapper.selectPage(page, queryWrapper);
        // 转换为VO
        Page<OrderVO> orderVOPage = new Page<>();
        orderVOPage.setCurrent(orderPage.getCurrent());
        orderVOPage.setSize(orderPage.getSize());
        orderVOPage.setTotal(orderPage.getTotal());
        // 批量查询用户信息
        List<OrderVO> orderVOs = convertToOrderVOBatch(orderPage.getRecords());
        orderVOPage.setRecords(orderVOs);
        return orderVOPage;
    }

    private List<OrderVO> convertToOrderVOBatch(List<Order> orders) {
        // 收集所有非空的openId
        List<String> openIds = orders.stream()
                .map(Order::getOpenId)
                .filter(openId -> openId != null && !openId.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 收集所有非空的driverId
        List<String> driverIds = orders.stream()
                .map(Order::getDriverId)
                .filter(driverId -> driverId != null && !driverId.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        // 批量查询用户信息
        Map<String, String> userNameMap = new HashMap<>();
        if (!openIds.isEmpty()) {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.in(User::getOpenId, openIds);
            List<User> users = userMapper.selectList(queryWrapper);
            userNameMap.putAll(users.stream()
                    .collect(Collectors.toMap(User::getOpenId, User::getName)));
        }

        // 批量查询司机信息
        Map<String, String> driverNameMap = new HashMap<>();
        if (!driverIds.isEmpty()) {
            LambdaQueryWrapper<User> driverWrapper = new LambdaQueryWrapper<>();
            driverWrapper.in(User::getId, driverIds);
            List<User> drivers = userMapper.selectList(driverWrapper);
            driverNameMap.putAll(drivers.stream()
                    .collect(Collectors.toMap(User::getId, User::getName)));
        }

        // 批量转换Order到OrderVO
        return orders.stream()
                .map(order -> {
                    OrderVO orderVO = new OrderVO();
                    BeanUtils.copyProperties(order, orderVO);
                    orderVO.setStatus(OrderStatus.fromKey(order.getStatus()));
                    orderVO.setUsername(userNameMap.get(order.getOpenId()));
                    orderVO.setDriverName(driverNameMap.get(order.getDriverId()));
                    return orderVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<Order> getOrderPage(Page<Order> page, Order order) {
        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();

        if (order.getOrderNo() != null && !order.getOrderNo().isEmpty()) {
            queryWrapper.like(Order::getOrderNo, order.getOrderNo());
        }

        if (order.getPhone() != null && !order.getPhone().isEmpty()) {
            queryWrapper.like(Order::getPhone, order.getPhone());
        }

        if (order.getStatus() != null && !order.getStatus().isEmpty()) {
            queryWrapper.eq(Order::getStatus, order.getStatus());
        }

        queryWrapper.orderByDesc(Order::getCreatedAt);

        return baseMapper.selectPage(page, queryWrapper);
    }

    @Override
    public OrderVO getDetailById(String id) {
        Order order = getById(id);
        OrderVO orderVO = convertToOrderVOBatch(Collections.singletonList(order)).get(0);
        List<OrderItems> items = orderItemsMapper.getListByOrderId(id);
        orderVO.setItems(convertFromItemList(items));
        int totalQuantity = items.stream()
                .mapToInt(OrderItems::getQuantity)
                .sum();
        orderVO.setTotalQuantity(totalQuantity);
        return orderVO;
    }

    private List<OrderItemsVO> convertFromItemList(List<OrderItems> orders) {
        return orders.stream()
                .map(orderItem -> {
                    OrderItemsVO orderItemsVO = new OrderItemsVO();
                    BeanUtils.copyProperties(orderItem, orderItemsVO);
                    orderItemsVO.setItemName(ITEM_NAME_BY_ID.getOrDefault(orderItem.getItemId(), "未知"));
//                    orderItemsVO.setStatus(OrderStatus.fromKey(order.getStatus()));
                    return orderItemsVO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateOrderStatus(String id, String status) {
        Order order = new Order();
        order.setId(id);
        order.setStatus(status.toUpperCase());
        return updateById(order);
    }

    @Override
    public boolean addOrder(Order order) {
        return save(order);
    }

    @Override
    public boolean updateOrder(Order order) {
        Order currentOrder = getById(order.getId());
        if ( OrderStatus.WAITING.getKey().equals(currentOrder.getStatus()) && order.getDriverId() != null) {
            order.setStatus(OrderStatus.ASSIGNED.getKey());
        }
        return updateById(order);

    }

    @Override
    public boolean deleteOrder(Long id) {
        return removeById(id);
    }
}
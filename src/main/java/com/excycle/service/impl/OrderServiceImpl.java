package com.excycle.service.impl;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.excycle.dto.OrderQueryRequest;
import com.excycle.dto.CreateOrderRequest;
import com.excycle.entity.FileInfo;
import com.excycle.entity.Order;
import com.excycle.entity.OrderItems;
import com.excycle.entity.User;
import com.excycle.enums.OrderStatus;
import com.excycle.mapper.OrderItemsMapper;
import com.excycle.mapper.OrderMapper;
import com.excycle.mapper.UserMapper;
import com.excycle.service.CloudBaseService;
import com.excycle.service.FinanceService;
import com.excycle.service.OrderService;
import com.excycle.utils.UUIDUtils;
import com.excycle.vo.OrderItemsVO;
import com.excycle.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static com.excycle.enums.OrderStatus.WAITING;

@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private final OrderMapper orderMapper;

    private final OrderItemsMapper orderItemsMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FinanceService financeService;

    @Autowired
    private CloudBaseService cloudBaseService;

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
        FileInfo orderImage = cloudBaseService.getTempFileURL(order.getOrderImages());
        orderVO.setOrderImage(orderImage);
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
        if ( OrderStatus.COMPLETED.getKey().equals(currentOrder.getStatus()) || OrderStatus.TRANSFERRING.getKey().equals(currentOrder.getStatus())
                || OrderStatus.CANCELLED.getKey().equals(currentOrder.getStatus())) {
            throw new IllegalStateException("订单状态异常，不能更新");
        }
        if ( WAITING.getKey().equals(currentOrder.getStatus()) && order.getDriverId() != null) {
            order.setStatus(OrderStatus.ASSIGNED.getKey());
        }
        if ( OrderStatus.COLLECTED.getKey().equals(currentOrder.getStatus()) &&
                OrderStatus.TRANSFERRING.getKey().equals(order.getStatus())) {
            financeService.transfer(currentOrder.getUserId(), currentOrder.getTotalPrice());
            order.setStatus(OrderStatus.COMPLETED.getKey());
        }

        return updateById(order);

    }

    @Override
    public boolean deleteOrder(Long id) {
        return removeById(id);
    }


    public static void main(String[] args) {
        String orderNo = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        System.out.println(orderNo);
    }
    @Override
    public Order createOrderWithItems(CreateOrderRequest createOrderRequest) {
        // 生成订单号
        String orderNo = LocalDateTime.now(ZoneId.of("Asia/Shanghai"))
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));

        // 计算结束时间（开始时间 + 1小时）
        Long endTime = createOrderRequest.getStartTime() + 3600 * 1000L;
        User user = userMapper.selectByOpenId(createOrderRequest.getOpenId());

        // 计算总价和总数量
        double totalPrice = 0.0;
        int totalQuantity = 0;
        for (CreateOrderRequest.OrderItemDTO item : createOrderRequest.getItems()) {
            totalPrice += item.getPrice() * item.getQuantity();
            totalQuantity += item.getQuantity();
        }

        // 创建订单
        Order order = new Order();
        order.setId(UUIDUtils.nextBase62SnowflakeId());
        order.setUserId(user.getId());
        order.setOrderNo(orderNo);
        order.setStartTime(createOrderRequest.getStartTime());
        order.setEndTime(endTime);
        order.setOpenId(createOrderRequest.getOpenId());
        order.setAddress(createOrderRequest.getAddress());
        order.setPhone(createOrderRequest.getPhone());
        order.setOrderImages(createOrderRequest.getOrderImages());
        order.setStatus(WAITING.getKey());
        order.setTotalPrice(totalPrice);
        order.setOpenId(createOrderRequest.getOpenId());
        order.setCreatedAt(System.currentTimeMillis());
        order.setUpdatedAt(System.currentTimeMillis());

        // 保存订单
        boolean orderSaved = save(order);
        if (!orderSaved) {
            throw new RuntimeException("订单保存失败");
        }

        // 创建订单项
        List<OrderItems> orderItemsList = createOrderRequest.getItems().stream()
                .map(item -> {
                    OrderItems orderItem = new OrderItems();
                    orderItem.setId(UUIDUtils.nextBase62SnowflakeId());
                    orderItem.setOrderId(order.getId());
                    orderItem.setItemId(item.getId());
                    orderItem.setQuantity(item.getQuantity());
                    orderItem.setPrice(item.getPrice());
                    orderItem.setOpenId(order.getOpenId());
                    return orderItem;
                })
                .collect(Collectors.toList());

        // 批量保存订单项
        for (OrderItems orderItem : orderItemsList) {
            orderItemsMapper.insert(orderItem);
        }

        return order;
    }
}
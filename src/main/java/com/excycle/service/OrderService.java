package com.excycle.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.excycle.dto.OrderQueryRequest;
import com.excycle.dto.CreateOrderRequest;
import com.excycle.entity.Order;
import com.excycle.vo.OrderVO;

public interface OrderService extends IService<Order> {

    Page<OrderVO> getOrderPage(OrderQueryRequest orderQueryRequest);

    Page<Order> getOrderPage(Page<Order> page, Order order);

    OrderVO getDetailById(String id);

    OrderVO getCurrentActiveOrder(String openId);

    boolean updateOrderStatus(String id, String status);

    boolean addOrder(Order order);

    boolean updateOrder(Order order);

    boolean deleteOrder(Long id);

    Order createOrderWithItems(CreateOrderRequest createOrderRequest);
}
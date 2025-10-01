package com.excycle.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.excycle.entity.Order;
import com.excycle.entity.User;
import com.excycle.enums.OrderStatus;
import com.excycle.mapper.UserMapper;
import com.excycle.vo.OrderVO;
import com.excycle.service.OrderService;
import com.excycle.common.Result;
import com.excycle.dto.OrderQueryRequest;
import com.excycle.dto.CreateOrderRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单管理RESTful API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
public class OrderApiController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取订单列表
     * POST /api/v1/orders/list
     */
    @PostMapping("/list")
    public Result<Page<OrderVO>> getOrders(@RequestBody OrderQueryRequest queryRequest, HttpServletRequest request) {
        log.info("headers {}", Collections.list(request.getHeaderNames()));
        return Result.success(orderService.getOrderPage(queryRequest));
    }

    /**
     * 根据ID获取订单
     * GET /api/v1/orders/{id}
     */
    @GetMapping("/{id}")
    public Result<OrderVO> getOrderById(@PathVariable String id) {
        return Result.success(orderService.getDetailById(id));
    }

    /**
     * 创建订单
     * POST /api/v1/orders
     */
    @PostMapping
    public Result<String> createOrder(@Validated @RequestBody CreateOrderRequest createOrderRequest, HttpServletRequest request) {
        String openId = request.getHeader("x-wx-openid");
        createOrderRequest.setOpenId(openId);
        Order order = orderService.createOrderWithItems(createOrderRequest);
        return Result.success("订单创建成功", order.getId());
    }

    /**
     * 更新订单
     * POST /api/v1/orders/{id}
     */
    @PostMapping("/{id}")
    public Result<String> updateOrder(@PathVariable String id, @RequestBody Order order) {
        order.setId(id);
        orderService.updateOrder(order);
        return Result.success("订单更新成功", id);
    }

    /**
     * 删除订单
     * DELETE /api/v1/orders/{id}
     */
    @DeleteMapping("/{id}")
    public Result<String> deleteOrder(@PathVariable Long id) {
        orderService.deleteOrder(id);
        return Result.success("订单删除成功");
    }

    /**
     * 批量删除订单
     * DELETE /api/v1/orders?ids=1,2,3
     */
    @DeleteMapping
    public Result<String> batchDeleteOrders(@RequestParam List<Long> ids) {
        orderService.removeByIds(ids);
        return  Result.success("批量删除成功");
    }


    /**
     * 获取订单总数
     * GET /api/v1/orders/count
     */
    @GetMapping("/count")
    public Result<Long> getOrderCount() {
        long count = orderService.count();
        return Result.success(count);
    }

}
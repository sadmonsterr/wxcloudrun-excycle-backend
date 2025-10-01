package com.excycle.vo;

import com.excycle.config.TimestampSerializer;
import com.excycle.entity.FileInfo;
import com.excycle.entity.OrderItems;
import com.excycle.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;

@Data
public class OrderVO {

    private String id;

    private String orderNo;

    private String phone;

    private String username;

    /**
     * 订单状态
     * CANCELLED - 已取消
     * WAITING - 待确认
     * IN_PROGRESS - 进行中
     * COMPLETED - 已完成
     * ASSIGNED - 已确认
     */
    private OrderStatus status;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long startTime;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long endTime;

    private String address;

    private String orderImages;

    private FileInfo orderImage;

    private String openId;

    private String driverId;

    private String driverName;

    private Double totalPrice;

    private Integer totalQuantity;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;

    private List<OrderItemsVO> items;

    @JsonGetter
    public String getStatusName() {
        return status.getDescription();
    }

}
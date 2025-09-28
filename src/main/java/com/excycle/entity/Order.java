package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.excycle.config.TimestampSerializer;
import lombok.Data;


@Data
@TableName("`order`")
public class Order {

    @TableId(type = IdType.AUTO, value = "_id")
    private String id;

    private String orderNo;

    private String phone;

    /**
     * 订单状态
     * WAITING 待接单
     * ASSIGNED 已接单
     * IN_PROGRESS 进行中
     * COLLECTED 已取货
     * COMPLETED 已转账
     * CANCELED 取消
     */
    private String status;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long startTime;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long endTime;

    private String address;

    private String orderImages;

    @TableField(value = "_openid")
    private String openId;

    private String driverId;

    private Double totalPrice;

    @TableField("createdAt")
    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @TableField("updatedAt")
    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;
}
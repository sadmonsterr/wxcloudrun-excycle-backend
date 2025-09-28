package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName(value = "orderItems")
public class OrderItems {

    @TableId(value = "_id")
    private String id;

    private String orderId;

    private Double quantity;

    private String itemId;

    private String price;
}

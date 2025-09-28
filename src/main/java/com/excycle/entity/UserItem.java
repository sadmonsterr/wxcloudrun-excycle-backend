package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@TableName("userItem")
public class UserItem {

    @TableId(type = IdType.AUTO, value = "_id")
    private String id;

    private String owner;

    @TableField(exist = false)
    private String itemName;

    private String itemId;

    private String userId;

    @TableField(value = "_openid")
    private String openid;

    private Double price;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;
}
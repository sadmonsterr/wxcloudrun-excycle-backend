package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;


@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO, value = "_id")
    private String id;

    private String phone;

    private String name;

    private String company;

    private String shop;

    private String role;

    private String businessLicense;

    @TableField("createdAt")
    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @TableField("updatedAt")
    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;

    @TableField(value = "_openid")
    private String openId;
}
package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("user_wallet")
public class UserWallet {

    @TableId(type = IdType.AUTO, value = "id")
    private Long id;

    @TableField("user_id")
    private String userId;

    private BigDecimal balance;

    @TableField("frozen_balance")
    private BigDecimal frozenBalance;

    private Integer version;

    @TableField("created_at")
    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @TableField("updated_at")
    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;
}
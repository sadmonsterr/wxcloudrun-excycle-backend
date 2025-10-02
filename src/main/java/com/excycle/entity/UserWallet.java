package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

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

    @TableField("open_id")
    private String openId;

    @TableField("created_at")
    private Timestamp createdAt;

    @TableField("updated_at")
    private Timestamp updatedAt;
}
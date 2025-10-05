package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("third_party_notify")
public class ThirdPartyNotify {

    @TableId(type = IdType.AUTO, value = "id")
    private Long id;

    @TableField("notify_id")
    private String notifyId;

    @TableField("notify_body")
    private String notifyBody;

    @TableField("request_id")
    private String requestId;

    @TableField("out_trade_no")
    private String outTradeNo;

    @TableField("trade_state")
    private String tradeState;

    @TableField("status")
    private String status;

    @TableField("created_at")
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp createdAt;

    @TableField("updated_at")
    @JsonSerialize(using = TimestampSerializer.class)
    private Timestamp updatedAt;
}
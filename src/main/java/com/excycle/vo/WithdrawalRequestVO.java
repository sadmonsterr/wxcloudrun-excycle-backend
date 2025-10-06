package com.excycle.vo;

import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class WithdrawalRequestVO {

    private Long id;

    private String requestId;

    private String userId;

    private String username;

    private String openId;

    private BigDecimal amount;

    private String status;

    private String statusDescription;

    private String thirdPartyOrderNo;

    private String errorReason;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long completedAt;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;
}
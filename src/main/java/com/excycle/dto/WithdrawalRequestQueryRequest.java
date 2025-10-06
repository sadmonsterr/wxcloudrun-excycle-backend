package com.excycle.dto;

import lombok.Data;

@Data
public class WithdrawalRequestQueryRequest {

    private Integer page = 1;

    private Integer size = 10;

    private String userId;

    private String openId;

    private String status;

    private String requestId;

    private String thirdPartyOrderNo;

    private String orderBy = "created_at";

    private Boolean isAsc = false;
}
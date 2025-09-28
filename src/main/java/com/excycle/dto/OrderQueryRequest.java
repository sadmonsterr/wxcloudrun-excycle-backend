package com.excycle.dto;

import lombok.Data;

@Data
public class OrderQueryRequest {

    private Integer page = 1;

    private Integer size = 10;

    private String status;

    private String orderNumber;

    private String driverId;

    private String openId;
}
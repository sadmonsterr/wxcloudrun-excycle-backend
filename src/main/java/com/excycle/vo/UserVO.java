package com.excycle.vo;

import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class UserVO {

    private String id;

    private String phone;

    private String name;

    private String company;

    private String shop;

    private String role;

    private String businessLicense;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;

    private String openId;
}
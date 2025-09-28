package com.excycle.vo;

import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class UserItemVO {

    private String id;

    private String owner;

    private String itemName;

    private String itemId;

    private String userId;

    private String openid;

    private Double price;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long createdAt;

    @JsonSerialize(using = TimestampSerializer.class)
    private Long updatedAt;
}
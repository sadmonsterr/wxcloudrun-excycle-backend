package com.excycle.vo;

import com.excycle.config.TimestampSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
public class ItemVO {

    private String id;

    private String name;

    private String userId;

    private Double price;

    private String icon;

}

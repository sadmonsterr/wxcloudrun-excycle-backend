package com.excycle.dto;

import lombok.Data;

@Data
public class UserQueryRequest {
    private Integer page = 1;
    private Integer size = 10;
    private String name;
    private String phone;
    private String role;
}
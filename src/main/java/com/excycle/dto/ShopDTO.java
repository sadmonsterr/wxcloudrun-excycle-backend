package com.excycle.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class ShopDTO {
    private String shopId;

    @NotBlank(message = "店铺名称不能为空")
    private String name;
}
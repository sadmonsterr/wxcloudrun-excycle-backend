package com.excycle.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserRegisterRequest {

    @NotBlank(message = "姓名不能为空")
    private String name;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String businessLicense;

    @NotBlank(message = "省份不能为空")
    private String province;

    @NotBlank(message = "城市不能为空")
    private String city;

    @NotBlank(message = "区县不能为空")
    private String district;

    @NotBlank(message = "详细地址不能为空")
    private String address;

    @NotBlank(message = "公司名称不能为空")
    private String company;

    @NotBlank(message = "店铺名称不能为空")
    private String storeName;

    @NotBlank(message = "角色不能为空")
    private String role;
}
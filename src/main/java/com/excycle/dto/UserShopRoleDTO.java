package com.excycle.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class UserShopRoleDTO {
    private String userId;

    @NotBlank(message = "店铺ID不能为空")
    private String shopId;

    @NotBlank(message = "角色ID不能为空")
    private String roleId;
}
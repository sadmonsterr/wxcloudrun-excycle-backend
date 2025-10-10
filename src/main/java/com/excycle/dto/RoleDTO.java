package com.excycle.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class RoleDTO {
    private String roleId;

    @NotBlank(message = "角色名称不能为空")
    private String name;
}
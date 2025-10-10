package com.excycle.vo;

import lombok.Data;

@Data
public class UserShopRoleVO {
    private Long id;

    private String userId;

    private String shopId;

    private String roleId;

    private String userName;

    private String shopName;

    private String roleName;

    private Long deleted;
}
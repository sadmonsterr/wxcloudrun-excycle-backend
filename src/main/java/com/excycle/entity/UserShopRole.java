package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_shop_role")
public class UserShopRole {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private String userId;

    @TableField(value = "shop_id")
    private String shopId;

    @TableField(value = "role_id")
    private String roleId;

    private Long deleted;
}
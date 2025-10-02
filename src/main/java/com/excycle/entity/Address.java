package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

@Data
@TableName
public class Address extends Model<Address> {

    @TableId(value = "_id")
    private String id;

    private String name;

    private String phone;

    private String detail;

    private String province;

    private String city;

    private String district;

    private Boolean isDefault;

    @TableField(value = "_openid")
    private String openId;

}

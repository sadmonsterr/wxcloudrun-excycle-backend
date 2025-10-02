package com.excycle.vo;

import lombok.Data;

@Data
public class AddressVO {

    private String id;

    private String name;

    private String phone;

    private String detail;

    private String province;

    private String city;

    private String district;

    private Boolean isDefault;

}

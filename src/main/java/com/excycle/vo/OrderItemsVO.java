package com.excycle.vo;

import lombok.Data;

@Data
public class OrderItemsVO {

    private String id;

    private String orderId;

    private Double quantity;

    private String itemId;

    private String itemName;

    private String price;


}

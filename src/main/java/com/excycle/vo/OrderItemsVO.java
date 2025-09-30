package com.excycle.vo;

import lombok.Data;

@Data
public class OrderItemsVO {

    private String id;

    private String orderId;

    private Integer quantity;

    private String itemId;

    private String itemName;

    private Double price;


}

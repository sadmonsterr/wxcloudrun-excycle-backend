package com.excycle.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

@Data
public class CreateOrderRequest {

    @Valid
    @NotEmpty(message = "订单商品不能为空")
    private List<OrderItemDTO> items;

//    @NotNull(message = "用户ID不能为空")
    private String userId;

    private String openId;

    @NotNull(message = "开始时间不能为空")
    private Long startTime;

    @NotBlank(message = "地址不能为空")
    private String address;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    private String orderImages;

    @Data
    public static class OrderItemDTO {

        @NotNull(message = "商品ID不能为空")
        private String id;

        @NotNull(message = "商品价格不能为空")
        @Positive(message = "商品价格必须大于0")
        private Double price;

        @NotNull(message = "商品数量不能为空")
        @Positive(message = "商品数量必须大于0")
        private Integer quantity;
    }
}
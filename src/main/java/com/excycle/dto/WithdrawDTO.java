package com.excycle.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

@Data
public class WithdrawDTO {

    @NotNull
    @Positive
    private Double amount;

}

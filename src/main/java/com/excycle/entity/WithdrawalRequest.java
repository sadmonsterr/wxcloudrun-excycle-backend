package com.excycle.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Withdrawal Request Entity
 * 
 * @author Your Name
 * @since 2025-10-03
 */
@Data
@Accessors(chain = true)
@TableName("withdrawal_request")
public class WithdrawalRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Primary key ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Unique request ID
     */
    @TableField("request_id")
    private String requestId;

    /**
     * User ID
     */
    @TableField("user_id")
    private String userId;

    /**
     * WeChat Open ID
     */
    @TableField("open_id")
    private String openId;

    /**
     * Withdrawal amount
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * Status: SUCCESS, PENDING, APPROVED, PROCESSING, COMPLETED, REJECTED, FAILED
     */
    @TableField("status")
    private String status;

    /**
     * Third party (WeChat) order number
     */
    @TableField("third_party_order_no")
    private String thirdPartyOrderNo;

    /**
     * Error reason if failed or rejected
     */
    @TableField("error_reason")
    private String errorReason;

    /**
     * Completion timestamp
     */
    @TableField("completed_at")
    private LocalDateTime completedAt;

    /**
     * Creation timestamp
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * Last update timestamp
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * Withdrawal request status enum
     */
    public enum Status {
        SUCCESS("SUCCESS", "Success"),
        PENDING("PENDING", "Pending"),
        APPROVED("APPROVED", "Approved"),
        PROCESSING("PROCESSING", "Processing"),
        COMPLETED("COMPLETED", "Completed"),
        REJECTED("REJECTED", "Rejected"),
        CANCELED("CANCELED", "Canceled"),
        FAILED("FAILED", "Failed");

        private final String code;
        private final String description;

        Status(String code, String description) {
            this.code = code;
            this.description = description;
        }

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }
    }
}
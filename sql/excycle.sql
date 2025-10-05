
-- User Wallet Table
CREATE TABLE user_wallet (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id varchar(16) NOT NULL UNIQUE,
                             balance DECIMAL(11, 2) NOT NULL DEFAULT 0.00,
                             open_id varchar(32) NOT NULL UNIQUE,
                             frozen_balance DECIMAL(11, 2) NOT NULL DEFAULT 0.00,
                             version INT NOT NULL DEFAULT 0, -- For optimistic locking
                             status TINYINT NOT NULL DEFAULT 1, -- 1: active, 0: frozen, 2: closed
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             INDEX idx_user_id (user_id),
                             CONSTRAINT chk_balance CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User wallet balance';


CREATE TABLE withdrawal_request (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    request_id VARCHAR(32) NOT NULL UNIQUE,
                                    user_id VARCHAR(32) NOT NULL,
                                    open_id VARCHAR(32) NOT NULL,
                                    amount DECIMAL(11, 2) NOT NULL,
                                    status VARCHAR(20) NOT NULL, -- PENDING, APPROVED, PROCESSING, COMPLETED, REJECTED, FAILED
                                    third_party_order_no VARCHAR(128), -- WeChat transfer order number
                                    error_reason VARCHAR(512),
                                    completed_at TIMESTAMP NULL,
                                    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                    INDEX idx_user_id (user_id),
                                    INDEX idx_request_no (request_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Withdrawal requests';

CREATE TABLE third_party_notify (
                                id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                notify_body VARCHAR(2048) NOT NULL,
                                notify_type VARCHAR(50) NOT NULL DEFAULT 'TRANSFER',
                                notify_id VARCHAR(64),
                                request_id VARCHAR(32),
                                out_trade_no VARCHAR(64),
                                status VARCHAR(20) NOT NULL DEFAULT 'PROCESSED',
                                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                                INDEX idx_request_id (request_id),
                                INDEX idx_out_trade_no (out_trade_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
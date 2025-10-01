
-- User Wallet Table
CREATE TABLE user_wallet (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id varchar(16) NOT NULL UNIQUE,
                             balance DECIMAL(11, 2) NOT NULL DEFAULT 0.00,
                             frozen_balance DECIMAL(11, 2) NOT NULL DEFAULT 0.00,
                             version INT NOT NULL DEFAULT 0, -- For optimistic locking
                             status TINYINT NOT NULL DEFAULT 1, -- 1: active, 0: frozen, 2: closed
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                             INDEX idx_user_id (user_id),
                             CONSTRAINT chk_balance CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='User wallet balance';
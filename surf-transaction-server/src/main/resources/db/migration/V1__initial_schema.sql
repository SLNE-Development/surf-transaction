-- Create table for currencies
CREATE TABLE transaction_currencies
(
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             CHAR(10)                            NOT NULL UNIQUE,
    display_name     TEXT                                NOT NULL,
    symbol           CHAR(5)                             NOT NULL,
    symbol_display   TEXT                                NOT NULL,
    scale            VARCHAR(255)                        NOT NULL,
    default_currency BOOLEAN                             NOT NULL,
    minimum_amount   DECIMAL(20, 10)                     NOT NULL,
    created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP
);

-- Create table for transactions
CREATE TABLE transaction_transactions
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    identifier UUID                                NOT NULL UNIQUE,
    sender     UUID,
    receiver   UUID,
    currency   BIGINT                              NOT NULL,
    amount     DECIMAL(20, 10)                     NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_currency FOREIGN KEY (currency) REFERENCES transaction_currencies (id)
);

-- Create table for transaction data
CREATE TABLE transaction_transaction_data
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction BIGINT       NOT NULL,
    data_key    VARCHAR(255) NOT NULL,
    data_value  TEXT         NOT NULL,
    CONSTRAINT fk_transaction_data_transaction FOREIGN KEY (transaction) REFERENCES transaction_transactions (id)
);

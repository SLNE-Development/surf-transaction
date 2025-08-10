CREATE TABLE IF NOT EXISTS transaction_accounts
(
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id UUID        NOT NULL,
    owner_id   UUID        NOT NULL,
    `name`     VARCHAR(64) NOT NULL
);
ALTER TABLE transaction_accounts
    ADD CONSTRAINT transaction_accounts_name_unique UNIQUE (`name`);
ALTER TABLE transaction_transactions
    ADD initiator_id UUID NULL;
ALTER TABLE transaction_transactions
    ADD sender_account BIGINT NULL;
ALTER TABLE transaction_transactions
    ADD receiver_account BIGINT NULL;
ALTER TABLE transaction_transactions
    MODIFY COLUMN created_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL;
ALTER TABLE transaction_transactions
    MODIFY COLUMN updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) NOT NULL;
ALTER TABLE transaction_currencies
    MODIFY COLUMN `name` CHAR(16) NOT NULL;
ALTER TABLE transaction_currencies
    MODIFY COLUMN symbol CHAR(16) NOT NULL;
ALTER TABLE transaction_transactions
    ADD CONSTRAINT fk_transaction_transactions_sender_account__id FOREIGN KEY (sender_account) REFERENCES transaction_accounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE transaction_transactions
    ADD CONSTRAINT fk_transaction_transactions_receiver_account__id FOREIGN KEY (receiver_account) REFERENCES transaction_accounts (id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE transaction_transactions
    DROP COLUMN sender;
ALTER TABLE transaction_transactions
    DROP COLUMN receiver;
ALTER TABLE transaction_currencies
    DROP COLUMN created_at;
ALTER TABLE transaction_currencies
    DROP COLUMN updated_at;

ALTER TABLE transaction_transactions
    DROP FOREIGN KEY fk_transaction_transactions_sender_account__id;
ALTER TABLE transaction_transactions
    ADD CONSTRAINT fk_transaction_transactions_sender_account__id FOREIGN KEY (sender_account) REFERENCES transaction_accounts (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE transaction_transactions
    DROP FOREIGN KEY fk_transaction_transactions_receiver_account__id;
ALTER TABLE transaction_transactions
    ADD CONSTRAINT fk_transaction_transactions_receiver_account__id FOREIGN KEY (receiver_account) REFERENCES transaction_accounts (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE transaction_transactions
    DROP FOREIGN KEY fk_transaction_currency;
ALTER TABLE transaction_transactions
    ADD CONSTRAINT fk_transaction_transactions_currency__id FOREIGN KEY (currency) REFERENCES transaction_currencies (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE transaction_transaction_data
    DROP FOREIGN KEY fk_transaction_data_transaction;
ALTER TABLE transaction_transaction_data
    ADD CONSTRAINT fk_transaction_transaction_data_transaction__id FOREIGN KEY (`transaction`) REFERENCES transaction_transactions (id) ON DELETE CASCADE ON UPDATE CASCADE;

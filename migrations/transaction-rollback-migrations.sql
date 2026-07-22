ALTER TABLE transactions ADD ignore_minimum_amount BOOLEAN DEFAULT FALSE NOT NULL;
ALTER TABLE transactions ADD `state` VARCHAR(16) DEFAULT 'COMMITTED' NOT NULL;
ALTER TABLE transactions ADD expires_at TIMESTAMP(6) NULL;
ALTER TABLE transactions ADD operation_id UUID NULL;
CREATE INDEX transactions_state_expires_at ON transactions (`state`, expires_at);
CREATE INDEX transactions_operation_id_state_expires_at ON transactions (operation_id, `state`, expires_at);
ALTER TABLE transactions MODIFY COLUMN updated_at TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) NOT NULL;
ALTER TABLE transactions ADD CONSTRAINT transactions_pending_has_expiration CHECK ((`state` <> 'PENDING') OR (expires_at IS NOT NULL));
ALTER TABLE transactions ADD CONSTRAINT transactions_pending_has_operation CHECK ((`state` <> 'PENDING') OR (operation_id IS NOT NULL));

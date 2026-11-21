CREATE TABLE IF NOT EXISTS pay_order (
    order_id UUID PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL,
    request_id UUID NOT NULL,
    product_id VARCHAR(16) NOT NULL,
    product_name VARCHAR(64) NOT NULL,
    total_amount DECIMAL(8, 2) NOT NULL,
    status VARCHAR(16) NOT NULL CHECK (status IN ('pay_wait','pay_success','close')),
    order_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pay_time TIMESTAMP,
    UNIQUE (user_id, request_id)
);

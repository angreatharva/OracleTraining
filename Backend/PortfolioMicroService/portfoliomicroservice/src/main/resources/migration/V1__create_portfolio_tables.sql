CREATE TABLE portfolio_account (
    portfolio_account_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    account_status VARCHAR(20) NOT NULL,
    opened_date DATE NOT NULL,
    closed_date DATE NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (portfolio_account_id),
    CONSTRAINT uk_portfolio_account_user UNIQUE (user_id)
);

CREATE TABLE portfolio_holding (
    holding_id BIGINT NOT NULL AUTO_INCREMENT,
    portfolio_account_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity DECIMAL(19,4) NOT NULL,
    average_cost DECIMAL(19,4) NOT NULL,
    market_value DECIMAL(19,4) NOT NULL,
    holding_status VARCHAR(20) NOT NULL,
    last_valued_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (holding_id),
    CONSTRAINT uk_holding_account_product UNIQUE (portfolio_account_id, product_id),
    CONSTRAINT fk_holding_portfolio_account FOREIGN KEY (portfolio_account_id)
        REFERENCES portfolio_account (portfolio_account_id)
);

CREATE INDEX idx_holding_product ON portfolio_holding (product_id);
CREATE INDEX idx_holding_account_status ON portfolio_holding (portfolio_account_id, holding_status);

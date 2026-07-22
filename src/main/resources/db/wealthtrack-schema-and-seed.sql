-- WealthTrack schema and seed data for MySQL 8+.
-- This is safe to rerun only when the existing tables already match this schema.
-- For an older or inconsistent training schema, run wealthtrack-reset.sql first,
-- then run this file. Do not remove ID or timestamp columns from the inserts:
-- they are required by the Java entity mappings.
CREATE DATABASE IF NOT EXISTS mydb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mydb;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS statement_transaction;
DROP TABLE IF EXISTS portfolio_statement;
DROP TABLE IF EXISTS trade_transaction;
DROP TABLE IF EXISTS portfolio_holding;
DROP TABLE IF EXISTS portfolio_account;
DROP TABLE IF EXISTS investment_product;
DROP TABLE IF EXISTS product_type;
DROP TABLE IF EXISTS bank_account;
DROP TABLE IF EXISTS kyc_document;
DROP TABLE IF EXISTS user_detail;
DROP TABLE IF EXISTS user;
DROP TABLE IF EXISTS role;

SET FOREIGN_KEY_CHECKS = 1;


CREATE TABLE IF NOT EXISTS role (
                                    role_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    role_name VARCHAR(100) NOT NULL UNIQUE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user (
                                    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    manager_id BIGINT NULL,
                                    role_id BIGINT NOT NULL,
                                    password_hash VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    phone VARCHAR(30), status VARCHAR(30),
    created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_user_manager FOREIGN KEY (manager_id) REFERENCES user(user_id),
    CONSTRAINT fk_user_role FOREIGN KEY (role_id) REFERENCES role(role_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS user_detail (
                                           user_detail_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                           user_id BIGINT NOT NULL UNIQUE,
                                           date_of_birth DATE, risk_level VARCHAR(30), risk_score INT, kyc_status VARCHAR(30),
    created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_user_detail_user FOREIGN KEY (user_id) REFERENCES user(user_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS kyc_document (
                                            kyc_document_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            user_id BIGINT NOT NULL,
                                            document_type VARCHAR(50) NOT NULL, document_number VARCHAR(100) NOT NULL UNIQUE,
    file_name VARCHAR(255), verification_status VARCHAR(30), submitted_date DATE, verified_date DATE,
    status VARCHAR(30), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_kyc_document_user FOREIGN KEY (user_id) REFERENCES user(user_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bank_account (
                                            bank_account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            user_id BIGINT NOT NULL, bank_name VARCHAR(100) NOT NULL, branch_name VARCHAR(100),
    account_number VARCHAR(50) NOT NULL UNIQUE, account_type VARCHAR(30), ifsc_code VARCHAR(20),
    balance DECIMAL(19,2) NOT NULL DEFAULT 0.00, is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_bank_account_user FOREIGN KEY (user_id) REFERENCES user(user_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS product_type (
                                            product_type_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                            type_code VARCHAR(30) NOT NULL UNIQUE, type_name VARCHAR(100) NOT NULL,
    description VARCHAR(500), status VARCHAR(30), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS investment_product (
                                                  product_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                  product_type_id BIGINT NOT NULL, product_name VARCHAR(255) NOT NULL UNIQUE,
    base_price DECIMAL(19,2), current_price DECIMAL(19,2), minimum_investment DECIMAL(19,2),
    risk_category VARCHAR(30), price_method VARCHAR(50), tenure_months INT, interest_rate DECIMAL(7,4),
    issue_date DATE, maturity_date DATE, status VARCHAR(30), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_investment_product_product_type FOREIGN KEY (product_type_id) REFERENCES product_type(product_type_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS portfolio_account (
                                                 portfolio_account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 user_id BIGINT NOT NULL UNIQUE, account_status VARCHAR(30), opened_date DATE, closed_date DATE,
    status VARCHAR(30), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_portfolio_account_user FOREIGN KEY (user_id) REFERENCES user(user_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS portfolio_holding (
                                                 holding_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 portfolio_account_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
                                                 quantity DECIMAL(19,4), average_cost DECIMAL(19,2), market_value DECIMAL(19,2),
    holding_status VARCHAR(30), last_valued_at DATETIME, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    UNIQUE KEY uk_holding_account_product (portfolio_account_id, product_id),
    CONSTRAINT fk_holding_account FOREIGN KEY (portfolio_account_id) REFERENCES portfolio_account(portfolio_account_id),
    CONSTRAINT fk_holding_product FOREIGN KEY (product_id) REFERENCES investment_product(product_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS trade_transaction (
                                                 transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                 portfolio_account_id BIGINT NOT NULL, holding_id BIGINT NOT NULL, product_id BIGINT NOT NULL,
                                                 transaction_type VARCHAR(20), quantity DECIMAL(18,2), unit_price DECIMAL(18,2), total_amount DECIMAL(18,2),
    transaction_status VARCHAR(30), transaction_date DATETIME, created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_trade_account FOREIGN KEY (portfolio_account_id) REFERENCES portfolio_account(portfolio_account_id),
    CONSTRAINT fk_trade_holding FOREIGN KEY (holding_id) REFERENCES portfolio_holding(holding_id),
    CONSTRAINT fk_trade_product FOREIGN KEY (product_id) REFERENCES investment_product(product_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS portfolio_statement (
                                                   statement_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   portfolio_account_id BIGINT NOT NULL, holding_id BIGINT NOT NULL, transaction_id BIGINT NOT NULL,
                                                   statement_start DATE, statement_end DATE, opening_value DECIMAL(18,2), closing_value DECIMAL(18,2),
    generated_at DATETIME, status VARCHAR(30), created_at DATETIME NOT NULL, updated_at DATETIME NOT NULL,
    CONSTRAINT fk_statement_account FOREIGN KEY (portfolio_account_id) REFERENCES portfolio_account(portfolio_account_id),
    CONSTRAINT fk_statement_holding FOREIGN KEY (holding_id) REFERENCES portfolio_holding(holding_id),
    CONSTRAINT fk_statement_transaction FOREIGN KEY (transaction_id) REFERENCES trade_transaction(transaction_id)
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS statement_transaction (
                                                     statement_id BIGINT NOT NULL, transaction_id BIGINT NOT NULL,
                                                     PRIMARY KEY (statement_id, transaction_id),
    CONSTRAINT fk_statement_transaction_statement FOREIGN KEY (statement_id) REFERENCES portfolio_statement(statement_id),
    CONSTRAINT fk_statement_transaction_trade FOREIGN KEY (transaction_id) REFERENCES trade_transaction(transaction_id)
    ) ENGINE=InnoDB;

-- Only these two roles are seeded: one manager role and one investor role.
INSERT INTO role (role_id, role_name) VALUES (1,'MANAGER'),(2,'INVESTOR')
    ON DUPLICATE KEY UPDATE role_name=VALUES(role_name);

-- One manager supervises five investors.
INSERT INTO user (user_id,manager_id,role_id,password_hash,email,full_name,phone,status,created_at,updated_at) VALUES
                                                                                                                   (1,NULL,1,'$2a$10$demo.manager.hash','priya.shah@wealthtrack.test','Priya Shah','9000000001','ACTIVE','2026-01-01 09:00:00','2026-01-01 09:00:00'),
                                                                                                                   (2,1,2,'$2a$10$demo.investor.01','aarav.mehta@wealthtrack.test','Aarav Mehta','9000000002','ACTIVE','2026-01-02 09:00:00','2026-01-02 09:00:00'),
                                                                                                                   (3,1,2,'$2a$10$demo.investor.02','diya.iyer@wealthtrack.test','Diya Iyer','9000000003','ACTIVE','2026-01-03 09:00:00','2026-01-03 09:00:00'),
                                                                                                                   (4,1,2,'$2a$10$demo.investor.03','kabir.singh@wealthtrack.test','Kabir Singh','9000000004','ACTIVE','2026-01-04 09:00:00','2026-01-04 09:00:00'),
                                                                                                                   (5,1,2,'$2a$10$demo.investor.04','meera.nair@wealthtrack.test','Meera Nair','9000000005','ACTIVE','2026-01-05 09:00:00','2026-01-05 09:00:00'),
                                                                                                                   (6,1,2,'$2a$10$demo.investor.05','rohan.verma@wealthtrack.test','Rohan Verma','9000000006','ACTIVE','2026-01-06 09:00:00','2026-01-06 09:00:00')
    ON DUPLICATE KEY UPDATE full_name=VALUES(full_name),phone=VALUES(phone),status=VALUES(status),updated_at=VALUES(updated_at);

INSERT INTO user_detail (user_detail_id,user_id,date_of_birth,risk_level,risk_score,kyc_status,created_at,updated_at) VALUES
                                                                                                                          (1,2,'1990-05-14','MODERATE',55,'VERIFIED',NOW(),NOW()),(2,3,'1988-11-23','HIGH',78,'VERIFIED',NOW(),NOW()),
                                                                                                                          (3,4,'1995-02-17','LOW',32,'VERIFIED',NOW(),NOW()),(4,5,'1992-08-09','MODERATE',60,'VERIFIED',NOW(),NOW()),
                                                                                                                          (5,6,'1985-12-30','HIGH',82,'VERIFIED',NOW(),NOW())
    ON DUPLICATE KEY UPDATE risk_level=VALUES(risk_level),risk_score=VALUES(risk_score),kyc_status=VALUES(kyc_status),updated_at=NOW();

INSERT INTO kyc_document (kyc_document_id,user_id,document_type,document_number,file_name,verification_status,submitted_date,verified_date,status,created_at,updated_at) VALUES
                                                                                                                                                                             (1,2,'PAN','ABCDE1234F','aarav-pan.pdf','VERIFIED','2026-01-02','2026-01-03','ACTIVE',NOW(),NOW()),
                                                                                                                                                                             (2,3,'PAN','BCDEF2345G','diya-pan.pdf','VERIFIED','2026-01-03','2026-01-04','ACTIVE',NOW(),NOW()),
                                                                                                                                                                             (3,4,'PAN','CDEFG3456H','kabir-pan.pdf','VERIFIED','2026-01-04','2026-01-05','ACTIVE',NOW(),NOW()),
                                                                                                                                                                             (4,5,'PAN','DEFGH4567I','meera-pan.pdf','VERIFIED','2026-01-05','2026-01-06','ACTIVE',NOW(),NOW()),
                                                                                                                                                                             (5,6,'PAN','EFGHI5678J','rohan-pan.pdf','VERIFIED','2026-01-06','2026-01-07','ACTIVE',NOW(),NOW())
    ON DUPLICATE KEY UPDATE verification_status=VALUES(verification_status),verified_date=VALUES(verified_date),status=VALUES(status),updated_at=NOW();

INSERT INTO bank_account (bank_account_id,user_id,bank_name,branch_name,account_number,account_type,ifsc_code,balance,is_primary,status,created_at,updated_at) VALUES
                                                                                                                                                                   (1,2,'HDFC Bank','Andheri','501000000001','SAVINGS','HDFC0000123',250000.00,TRUE,'ACTIVE',NOW(),NOW()),
                                                                                                                                                                   (2,3,'ICICI Bank','Indiranagar','501000000002','SAVINGS','ICIC0000456',320000.00,TRUE,'ACTIVE',NOW(),NOW()),
                                                                                                                                                                   (3,4,'SBI','Sector 17','501000000003','SAVINGS','SBIN0000789',180000.00,TRUE,'ACTIVE',NOW(),NOW()),
                                                                                                                                                                   (4,5,'Axis Bank','Kakkanad','501000000004','SAVINGS','UTIB0000321',410000.00,TRUE,'ACTIVE',NOW(),NOW()),
                                                                                                                                                                   (5,6,'Kotak Mahindra Bank','Viman Nagar','501000000005','SAVINGS','KKBK0000654',275000.00,TRUE,'ACTIVE',NOW(),NOW())
    ON DUPLICATE KEY UPDATE balance=VALUES(balance),status=VALUES(status),updated_at=NOW();

INSERT INTO product_type (product_type_id,type_code,type_name,description,status,created_at,updated_at) VALUES
                                                                                                            (1,'EQUITY','Equity','Listed shares and equity funds','ACTIVE',NOW(),NOW()),
                                                                                                            (2,'BOND','Bond','Government and corporate debt','ACTIVE',NOW(),NOW()),
                                                                                                            (3,'MUTUAL_FUND','Mutual Fund','Professionally managed investment fund','ACTIVE',NOW(),NOW()),
                                                                                                            (4,'FIXED_DEPOSIT','Fixed Deposit','Fixed-term bank deposit','ACTIVE',NOW(),NOW()),
                                                                                                            (5,'ETF','Exchange Traded Fund','Exchange-traded index fund','ACTIVE',NOW(),NOW())
    ON DUPLICATE KEY UPDATE type_name=VALUES(type_name),description=VALUES(description),status=VALUES(status),updated_at=NOW();

INSERT INTO investment_product (product_id,product_type_id,product_name,base_price,current_price,minimum_investment,risk_category,price_method,tenure_months,interest_rate,issue_date,maturity_date,status,created_at,updated_at) VALUES
                                                                                                                                                                                                                                      (1,1,'Reliance Industries',1200,1450,1000,'HIGH','MARKET',NULL,NULL,'2020-01-01',NULL,'ACTIVE',NOW(),NOW()),
                                                                                                                                                                                                                                      (2,2,'India Government Bond 2030',1000,1045.50,1000,'LOW','NAV',60,7.1000,'2025-01-01','2030-01-01','ACTIVE',NOW(),NOW()),
                                                                                                                                                                                                                                      (3,3,'HDFC Flexi Cap Fund',500,678.25,500,'MODERATE','NAV',NULL,NULL,'2022-01-01',NULL,'ACTIVE',NOW(),NOW()),
                                                                                                                                                                                                                                      (4,4,'SBI Fixed Deposit 2028',1000,1000,10000,'LOW','FIXED',36,6.7500,'2025-01-01','2028-01-01','ACTIVE',NOW(),NOW()),
                                                                                                                                                                                                                                      (5,5,'Nifty 50 ETF',200,245.80,1000,'MODERATE','MARKET',NULL,NULL,'2021-01-01',NULL,'ACTIVE',NOW(),NOW())
    ON DUPLICATE KEY UPDATE current_price=VALUES(current_price),status=VALUES(status),updated_at=NOW();

INSERT INTO portfolio_account (portfolio_account_id,user_id,account_status,opened_date,closed_date,status,created_at,updated_at) VALUES
                                                                                                                                     (1,2,'OPEN','2026-01-02',NULL,'ACTIVE',NOW(),NOW()),(2,3,'OPEN','2026-01-03',NULL,'ACTIVE',NOW(),NOW()),
                                                                                                                                     (3,4,'OPEN','2026-01-04',NULL,'ACTIVE',NOW(),NOW()),(4,5,'OPEN','2026-01-05',NULL,'ACTIVE',NOW(),NOW()),
                                                                                                                                     (5,6,'OPEN','2026-01-06',NULL,'ACTIVE',NOW(),NOW())
    ON DUPLICATE KEY UPDATE account_status=VALUES(account_status),status=VALUES(status),updated_at=NOW();

INSERT INTO portfolio_holding (holding_id,portfolio_account_id,product_id,quantity,average_cost,market_value,holding_status,last_valued_at,created_at,updated_at) VALUES
                                                                                                                                                                      (1,1,1,100,1200,145000,'ACTIVE','2026-07-22 09:00:00',NOW(),NOW()),
                                                                                                                                                                      (2,2,2,150,1000,156825,'ACTIVE','2026-07-22 09:00:00',NOW(),NOW()),
                                                                                                                                                                      (3,3,3,200,500,135650,'ACTIVE','2026-07-22 09:00:00',NOW(),NOW()),
                                                                                                                                                                      (4,4,4,250,1000,250000,'ACTIVE','2026-07-22 09:00:00',NOW(),NOW()),
                                                                                                                                                                      (5,5,5,300,200,73740,'ACTIVE','2026-07-22 09:00:00',NOW(),NOW())
    ON DUPLICATE KEY UPDATE quantity=VALUES(quantity),average_cost=VALUES(average_cost),market_value=VALUES(market_value),last_valued_at=VALUES(last_valued_at),updated_at=NOW();

INSERT INTO trade_transaction (transaction_id,portfolio_account_id,holding_id,product_id,transaction_type,quantity,unit_price,total_amount,transaction_status,transaction_date,created_at,updated_at) VALUES
                                                                                                                                                                                                          (1,1,1,1,'BUY',100,1200,120000,'COMPLETED','2026-01-10 10:00:00',NOW(),NOW()),
                                                                                                                                                                                                          (2,2,2,2,'BUY',150,1000,150000,'COMPLETED','2026-01-11 10:00:00',NOW(),NOW()),
                                                                                                                                                                                                          (3,3,3,3,'BUY',200,500,100000,'COMPLETED','2026-01-12 10:00:00',NOW(),NOW()),
                                                                                                                                                                                                          (4,4,4,4,'BUY',250,1000,250000,'COMPLETED','2026-01-13 10:00:00',NOW(),NOW()),
                                                                                                                                                                                                          (5,5,5,5,'BUY',300,200,60000,'COMPLETED','2026-01-14 10:00:00',NOW(),NOW())
    ON DUPLICATE KEY UPDATE transaction_status=VALUES(transaction_status),transaction_date=VALUES(transaction_date),updated_at=NOW();

INSERT INTO portfolio_statement (statement_id,portfolio_account_id,holding_id,transaction_id,statement_start,statement_end,opening_value,closing_value,generated_at,status,created_at,updated_at) VALUES
                                                                                                                                                                                                      (1,1,1,1,'2026-01-01','2026-01-31',0,145000,'2026-02-01 00:05:00','GENERATED',NOW(),NOW()),
                                                                                                                                                                                                      (2,2,2,2,'2026-01-01','2026-01-31',0,156825,'2026-02-01 00:05:00','GENERATED',NOW(),NOW()),
                                                                                                                                                                                                      (3,3,3,3,'2026-01-01','2026-01-31',0,135650,'2026-02-01 00:05:00','GENERATED',NOW(),NOW()),
                                                                                                                                                                                                      (4,4,4,4,'2026-01-01','2026-01-31',0,250000,'2026-02-01 00:05:00','GENERATED',NOW(),NOW()),
                                                                                                                                                                                                      (5,5,5,5,'2026-01-01','2026-01-31',0,73740,'2026-02-01 00:05:00','GENERATED',NOW(),NOW())
    ON DUPLICATE KEY UPDATE closing_value=VALUES(closing_value),generated_at=VALUES(generated_at),status=VALUES(status),updated_at=NOW();

INSERT INTO statement_transaction (statement_id,transaction_id) VALUES (1,1),(2,2),(3,3),(4,4),(5,5)
    ON DUPLICATE KEY UPDATE transaction_id=VALUES(transaction_id);

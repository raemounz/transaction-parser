-- Rates table
DROP TABLE IF EXISTS txn_db.rates;

CREATE TABLE txn_db.rates (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    from_ccy VARCHAR(3) NOT NULL,
    to_ccy VARCHAR(3) NOT NULL,
    rate DOUBLE NOT NULL
);

CREATE INDEX date_idx ON txn_db.rates(date);

-- Transactions table
DROP TABLE IF EXISTS txn_db.transactions;

CREATE TABLE IF NOT EXISTS txn_db.transactions (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    account INT NOT NULL,
    amount FLOAT NOT NULL,
    ccy VARCHAR(3) NOT NULL
);

CREATE INDEX date_idx ON txn_db.transactions(date);

-- Daily total transactions table
DROP TABLE IF EXISTS txn_db.daily_txn;

CREATE TABLE IF NOT EXISTS txn_db.daily_txn (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    no_of_txn SMALLINT NOT NULL DEFAULT 0,
    total_amt DOUBLE NOT NULL,
    ccy VARCHAR(3) NOT NULL
);

CREATE INDEX date_idx ON txn_db.daily_txn(date);

-- Daily total transactions per account table
DROP TABLE IF EXISTS txn_db.daily_acct_txn;

CREATE TABLE IF NOT EXISTS txn_db.daily_acct_txn (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    account INT NOT NULL,
    no_of_txn SMALLINT NOT NULL DEFAULT 0,
    total_amt DOUBLE NOT NULL,
    ccy VARCHAR(3) NOT NULL
);

CREATE INDEX date_idx ON txn_db.daily_acct_txn(date);
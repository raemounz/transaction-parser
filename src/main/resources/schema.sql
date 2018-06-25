-- Rates table
DROP TABLE IF EXISTS test.rates;

CREATE TABLE test.rates (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    from_ccy VARCHAR(3) NOT NULL,
    to_ccy VARCHAR(3) NOT NULL,
    rate DOUBLE NOT NULL
);

CREATE INDEX date_idx ON test.rates(date);

-- Transactions table
DROP TABLE IF EXISTS test.transactions;

CREATE TABLE IF NOT EXISTS test.transactions (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    account INT NOT NULL,
    amount FLOAT NOT NULL,
    ccy VARCHAR(3) NOT NULL
);

CREATE INDEX date_idx ON test.transactions(date);

-- Daily total transactions table
DROP TABLE IF EXISTS test.daily_txn;

CREATE TABLE IF NOT EXISTS test.daily_txn (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    no_of_txn SMALLINT NOT NULL DEFAULT 0,
    total_amt DOUBLE NOT NULL,
    ccy VARCHAR(3) NOT NULL
);

CREATE INDEX date_idx ON test.daily_txn(date);

-- Daily total transactions per account table
DROP TABLE IF EXISTS test.daily_acct_txn;

CREATE TABLE IF NOT EXISTS test.daily_acct_txn (
    id INT(6) UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    date DATETIME NOT NULL,
    account INT NOT NULL,
    no_of_txn SMALLINT NOT NULL DEFAULT 0,
    total_amt DOUBLE NOT NULL,
    ccy VARCHAR(3) NOT NULL
);

CREATE INDEX date_idx ON test.daily_acct_txn(date);
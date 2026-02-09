CREATE TABLE daily_exchange_rate (
                                   id BIGSERIAL PRIMARY KEY,
                                   date DATE NOT NULL,
                                   source VARCHAR(255) NOT NULL,
                                   CONSTRAINT uk_date_source UNIQUE (date, source)
);

CREATE TABLE currency_rate (
                             id BIGSERIAL PRIMARY KEY,
                             daily_rate_id BIGINT NOT NULL,
                             currency_code VARCHAR(3) NOT NULL,
                             exchange_rate NUMERIC(19, 6) NOT NULL,
                             CONSTRAINT fk_daily_rate FOREIGN KEY (daily_rate_id) REFERENCES daily_exchange_rate (id)
);

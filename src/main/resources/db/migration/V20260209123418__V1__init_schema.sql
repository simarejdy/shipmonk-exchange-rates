CREATE SEQUENCE daily_exchange_rate_seq START WITH 1 INCREMENT BY 50;
CREATE SEQUENCE currency_rate_seq START WITH 1 INCREMENT BY 50;

CREATE TABLE daily_exchange_rates (
                                    id BIGINT NOT NULL,
                                    rate_date DATE NOT NULL,
                                    source_currency VARCHAR(3) NOT NULL,
                                    CONSTRAINT pk_daily_exchange_rates PRIMARY KEY (id)
);

ALTER TABLE daily_exchange_rates
  ADD CONSTRAINT uc_daily_exchange_rates_date UNIQUE (rate_date);

CREATE TABLE currency_rates (
                              id BIGINT NOT NULL,
                              currency_code VARCHAR(3) NOT NULL,
                              exchange_rate NUMERIC(19, 6) NOT NULL,
                              daily_rate_id BIGINT,
                              CONSTRAINT pk_currency_rates PRIMARY KEY (id),
                              CONSTRAINT fk_currency_rates_daily FOREIGN KEY (daily_rate_id) REFERENCES daily_exchange_rates (id)
);

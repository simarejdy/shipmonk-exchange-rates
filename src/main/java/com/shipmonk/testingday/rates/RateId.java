package com.shipmonk.testingday.rates;

import com.fasterxml.jackson.annotation.JsonValue;
import com.shipmonk.testingday.common.BaseId;

public class RateId extends BaseId {

    public RateId(long value) {
        super(value);
    }

    public static RateId of(long value) {
        return new RateId(value);
    }

    @JsonValue
    public long toValue() {
        return getValue();
    }
}

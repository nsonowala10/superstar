package com.quotes.premium.operation;

import com.quotes.premium.dto.Applicable;
import com.quotes.premium.dto.Attribute;

public interface Operation {
    void apply(Applicable obj, String key, String baseValueKey, Attribute attribute) throws Exception;
}

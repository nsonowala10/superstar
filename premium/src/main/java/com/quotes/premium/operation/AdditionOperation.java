package com.quotes.premium.operation;

import com.quotes.premium.dto.Applicable;
import com.quotes.premium.dto.Attribute;

import java.lang.reflect.Field;

public class AdditionOperation implements Operation {
    @Override
    public void apply(final Applicable obj, final String key, final String baseValueKey, final Attribute attribute) throws Exception {
        if(!attribute.isMultiplicative()){
            return ;
        }

        final Field discountField = Applicable.class.getDeclaredField(key);
        discountField.setAccessible(true);
        final Double currentDiscount = (Double) discountField.get(obj);

        final Field baseField = Applicable.class.getDeclaredField(baseValueKey);
        baseField.setAccessible(true);
        Double baseValue = (Double) baseField.get(obj);

        baseValue = baseValue + ("loading".equals(attribute.getExpenseType()) ? currentDiscount : -currentDiscount);
        baseField.set(obj, baseValue);
    }
}

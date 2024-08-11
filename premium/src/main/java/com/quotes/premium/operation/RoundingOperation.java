package com.quotes.premium.operation;

import com.quotes.premium.dto.Applicable;
import com.quotes.premium.dto.Attribute;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class RoundingOperation implements Operation {

    private static String capitalizeFirstLetter(final String str) {
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    @Override
    public void apply(final Applicable obj, final String key, final String baseValueKey, final Attribute attribute) throws Exception {
        if(!attribute.isRounding()){
            return ;
        }
        final Field field = Applicable.class.getDeclaredField(key);
        field.setAccessible(true);
        Double currentDiscount = (Double) field.get(obj);
        currentDiscount = (double) Math.round(currentDiscount);
        final Method method = Applicable.class.getMethod("set" + RoundingOperation.capitalizeFirstLetter(key), Double.class);
        method.invoke(obj, currentDiscount);
    }
}

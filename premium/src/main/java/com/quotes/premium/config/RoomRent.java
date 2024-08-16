package com.quotes.premium.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RoomRent {

    @Value("${room.rent.discount.mapping}")
    private String roomRentDiscountMapping;

    private final Map<String, Double> roomRentDiscount = new HashMap<>();
    public Double getRoomRentDiscount(final String roomType){
        if(this.roomRentDiscount.isEmpty()){
            final String[] item = this.roomRentDiscountMapping.split(",");
            for(final String i : item){
                final String[] each = i.split(":");
                this.roomRentDiscount.put(each[0], Double.valueOf(each[1]));
            }
        }
        return this.roomRentDiscount.get(roomType);
    }
}

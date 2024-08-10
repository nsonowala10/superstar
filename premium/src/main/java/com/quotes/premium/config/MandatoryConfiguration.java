package com.quotes.premium.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import com.quotes.premium.dto.Attribute;

@Configuration
public class MandatoryConfiguration {
    @Value("${floater.prerequisite.configurations}")
    private String featureConf;
    private Map<String, Attribute> featureConfMap = new HashMap<>();

    public Attribute getConf(final String feature)  {
        if(this.featureConfMap.isEmpty()) {
            try {
                prepareConfMap();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return this.featureConfMap.get(feature);
    }

    private void prepareConfMap() throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, Map<String, Object>> tempMap = objectMapper.readValue(this.featureConf, new TypeReference<Map<String, Map<String, Object>>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });

        for (Map.Entry<String, Map<String, Object>> entry : tempMap.entrySet()) {
            String key = entry.getKey();
            System.out.println(key);
            Map<String, Object> attributesMap = entry.getValue();
            Attribute attributes = new Attribute(
                    (String) attributesMap.get("insured"),
                    (String) attributesMap.get("year"),
                    (Boolean) attributesMap.get("multiplicative"),
                    (Boolean) attributesMap.get("rounding"),
                    (String) attributesMap.get("stage")
            );
            this.featureConfMap.put(key, attributes);
        }
    }


}

package com.quotes.premium.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import java.util.HashMap;
import java.util.Map;
import com.quotes.premium.dto.Attribute;

@Configuration
public class MandatoryConfiguration {
    @Value("${floater.prerequisite.configurations}")
    private String floaterConf;

    @Value("${individual.prerequisite.configurations}")
    private String individualConf;

    private final Map<String, Attribute> floaterMandatoryConf = new HashMap<>();
    private final Map<String, Attribute> individualMandatoryConf = new HashMap<>();

    public Attribute getConf(final String feature, final String policyType) throws JsonProcessingException {
        if ("individual".equals(policyType) && this.individualMandatoryConf.isEmpty()) {
            this.prepareConfMap(this.floaterConf, this.individualMandatoryConf);
        } else if ("floater".equals(policyType) && this.floaterMandatoryConf.isEmpty()) {
            this.prepareConfMap(this.individualConf, this.floaterMandatoryConf);
        }
        return this.floaterMandatoryConf.get(feature);
    }


    private void prepareConfMap(final String featureConf, final Map<String, Attribute> map) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, Map<String, Object>> tempMap = objectMapper.readValue(
                featureConf,
                new TypeReference<Map<String, Map<String, Object>>>() {}
        );

        tempMap.forEach((key, attributesMap) -> {
            System.out.println(key);
            final Attribute attributes = new Attribute(
                    (String) attributesMap.get("insured"),
                    (String) attributesMap.get("year"),
                    (Boolean) attributesMap.get("multiplicative"),
                    (Boolean) attributesMap.get("rounding"),
                    (String) attributesMap.get("stage")
            );
            map.put(key, attributes);
        });
    }
}
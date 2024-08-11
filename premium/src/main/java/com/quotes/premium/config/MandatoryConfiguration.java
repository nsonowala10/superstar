package com.quotes.premium.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.*;

import com.quotes.premium.dto.Attribute;

@Configuration
@Log4j2
@Getter
public class MandatoryConfiguration {

    @Value("${floater.prerequisite.configurations}")
    private String floaterConf;

    @Value("${individual.prerequisite.configurations}")
    private String individualConf;

    @Value("${execution.keys}")
    private String executionKeys;

    final private List<String> executionKeysList = new LinkedList<>();
    private final Map<String, Attribute> floaterMandatoryConf = new HashMap<>();
    private final Map<String, Attribute> individualMandatoryConf = new HashMap<>();

    public List<String> getExecutionKeys(){
        if(this.executionKeysList.isEmpty()){
            this.executionKeysList.addAll(Arrays.asList(this.executionKeys.split(",")));
        }
        return this.executionKeysList;
    }

    public Attribute getFeature(final String feature, final String policyType) {
        try{
            if ("individual".equals(policyType) && this.individualMandatoryConf.isEmpty()) {
                this.prepareConfMap(this.floaterConf, this.individualMandatoryConf);
            } else if ("floater".equals(policyType) && this.floaterMandatoryConf.isEmpty()) {
                this.prepareConfMap(this.individualConf, this.floaterMandatoryConf);
            }
        }
        catch(final Exception e){
            MandatoryConfiguration.log.error("exception in getting configuration {} for policy type {} ", feature, policyType, e);
        }
        return this.floaterMandatoryConf.get(feature);
    }


    private void prepareConfMap(final String featureConf, final Map<String, Attribute> map) throws JsonProcessingException {
        if(!map.isEmpty())
            return ;

        final ObjectMapper objectMapper = new ObjectMapper();
        final Map<String, Map<String, Object>> tempMap = objectMapper.readValue(
                featureConf,
                new TypeReference<Map<String, Map<String, Object>>>() {}
        );

        tempMap.forEach((key, attributesMap) -> {
            final Attribute attributes = new Attribute(
                    (String) attributesMap.get("insured"),
                    (String) attributesMap.get("year"),
                    (Boolean) attributesMap.get("multiplicative"),
                    (Boolean) attributesMap.get("rounding"),
                    (String) attributesMap.get("stage"),
                    (String) attributesMap.get("expenseType")
            );
            map.put(key, attributes);
        });
    }

    public Map<String, Attribute> getConf(final String policyType) throws JsonProcessingException {
        if ("floater".equals(policyType)) {
            this.prepareConfMap(this.floaterConf, this.floaterMandatoryConf);
            return this.floaterMandatoryConf;
        } else {
            this.prepareConfMap(this.individualConf, this.individualMandatoryConf);
            return this.individualMandatoryConf;
        }
    }
}
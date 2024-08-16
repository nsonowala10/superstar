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

    @Value("${floater.fresh.prerequisite.configurations}")
    private String floaterFreshConf;

    @Value("${floater.renewal.prerequisite.configurations}")
    private String floaterRenewalConf;

    @Value("${individual.fresh.prerequisite.configurations}")
    private String individualFreshConf;

    @Value("${individual.renewal.prerequisite.configurations}")
    private String individualRenewalConf;

    @Value("${execution.keys}")
    private String executionKeys;

    @Value("${validation.keys}")
    private String validationKeys;

    final private List<String> validationKeysList = new LinkedList<>();
    final private List<String> executionKeysList = new LinkedList<>();
    private Map<String, Attribute> floaterFreshMandatoryConf = new HashMap<>();
    private Map<String, Attribute> individualFreshMandatoryConf = new HashMap<>();
    private Map<String, Attribute> floaterRenewalMandatoryConf = new HashMap<>();
    private Map<String, Attribute> individualRenewalMandatoryConf = new HashMap<>();

    public List<String> getValidationKeys(){
        if(this.validationKeysList.isEmpty()){
            this.validationKeysList.addAll(Arrays.asList(this.validationKeys.split(",")));
        }
        return this.validationKeysList;
    }

    public List<String> getExecutionKeys(){
        if(this.executionKeysList.isEmpty()){
            this.executionKeysList.addAll(Arrays.asList(this.executionKeys.split(",")));
        }
        return this.executionKeysList;
    }

    public Attribute getFeature(final String feature, final String policyType, boolean fresh) {
        try{
            if ("individual".equals(policyType) && fresh ) {
                return this.individualFreshMandatoryConf.get(feature);
            } else if ("floater".equals(policyType) && fresh) {
                return this.floaterFreshMandatoryConf.get(feature);
            } else if ("individual".equals(policyType) && !fresh) {
                return this.individualRenewalMandatoryConf.get(feature);
            } else if ("floater".equals(policyType) && !fresh) {
                return this.floaterRenewalMandatoryConf.get(feature);
            }
        }
        catch(final Exception e){
            MandatoryConfiguration.log.error("exception in getting configuration {} for policy type {} ", feature, policyType, e);
        }
        return null;
    }


    private  Map<String, Attribute> prepareConfMap(final String featureConf) throws JsonProcessingException {
        final ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Attribute> map = new HashMap<>();
        map = objectMapper.readValue(
                featureConf,
                new TypeReference<Map<String, Attribute>>() {}
        );

        return map;
    }

    public Map<String, Attribute> getConf(final String policyType, boolean fresh) throws JsonProcessingException {
        if ("floater".equals(policyType) && fresh) {
            this.floaterFreshMandatoryConf = this.prepareConfMap(this.floaterFreshConf);
            return this.floaterFreshMandatoryConf;
        }

        else if("individual".equals(policyType) && fresh) {
            this.individualFreshMandatoryConf = this.prepareConfMap(this.individualFreshConf);
            return this.individualFreshMandatoryConf;
        }

        else if("floater".equals(policyType)) {
            this.floaterRenewalMandatoryConf = this.prepareConfMap(this.floaterRenewalConf);
            return this.floaterRenewalMandatoryConf;
        }

        else  {
            this.individualRenewalMandatoryConf = this.prepareConfMap(this.individualRenewalConf);
            return this.individualRenewalMandatoryConf;
        }
    }
}
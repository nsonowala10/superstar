package com.quotes.premium;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class Configuraitons {
    static private Map<String,Attributes> map = new HashMap<>();

    public static void main(String[] args) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String arr[] = {"lookup","ped","floater","powerbooster","instantCover","consumableCover","futureReady","specificDisease","pedWaitingPeriod","infiniteCare","preferredhospitalNetwork","copay","deductible","roomRent","subLimitModeration","medicalEquipmentCover","wellnessDiscount","nriDiscount","maternityExpense","womenCare","highEndDiagnostic","airAmbulance","annualHealthCheckUp","internationalSecondOpinion","CompassionateVisit","hospitalCash","paCover","healthQuestionnaire","cibilDiscount","earlyRenewal","longTermDiscount","zonalDiscount","paymentTerm"};
        for(String str : arr){
            map.put(str, new Attributes("all","all",true,true,"1"));

        }


        Map<String,Attributes> cMap = new HashMap<>();
        String jsonString = "{\"earlyRenewal\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"powerbooster\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"infiniteCare\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"hospitalCash\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"maternityExpense\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"airAmbulance\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"annualHealthCheckUp\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"ped\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"futureReady\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"healthQuestionnaire\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"preferredhospitalNetwork\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"floater\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"longTermDiscount\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"copay\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"roomRent\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"lookup\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"instantCover\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"wellnessDiscount\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"highEndDiagnostic\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"paCover\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"medicalEquipmentCover\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"specificDisease\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"subLimitModeration\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"nriDiscount\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"womenCare\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"pedWaitingPeriod\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"internationalSecondOpinion\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"CompassionateVisit\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"cibilDiscount\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"deductible\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"zonalDiscount\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"consumableCover\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"},\"paymentTerm\":{\"insured\":\"all\",\"year\":\"all\",\"multiplicative\":true,\"rounding\":true,\"stage\":\"1\"}}\n";
        Map<String, Map<String, Object>> tempMap = objectMapper.readValue(jsonString, new TypeReference<Map<String, Map<String, Object>>>() {
            @Override
            public Type getType() {
                return super.getType();
            }
        });

        for (Map.Entry<String, Map<String, Object>> entry : tempMap.entrySet()) {
            String key = entry.getKey();
            Map<String, Object> attributesMap = entry.getValue();
            Attributes attributes = new Attributes(
                    (String) attributesMap.get("insured"),
                    (String) attributesMap.get("year"),
                    (Boolean) attributesMap.get("multiplicative"),
                    (Boolean) attributesMap.get("rounding"),
                    (String) attributesMap.get("stage")
            );
            cMap.put(key, attributes);
        }

        System.out.println(cMap.entrySet());

    }
}


@Data
@AllArgsConstructor
class Attributes{
    private String insured; //adult, all, child
    private String year; // all, inception
    private boolean multiplicative;
    private boolean rounding;
    private String stage;
}

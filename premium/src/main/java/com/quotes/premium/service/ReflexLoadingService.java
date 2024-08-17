package com.quotes.premium.service;

import com.quotes.premium.dto.ReflexLoading;
import com.quotes.premium.dto.ReflexQuestionAndAnswer;
import org.springframework.stereotype.Service;

@Service
public class ReflexLoadingService {

    public static ReflexLoading handleBMIQuestions(ReflexQuestionAndAnswer reflexQuestionAndAnswer) {
        String type = "STP";
        float percentage = 0.0f;
        switch (reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q1")) {
            // 15 to 30
            case "15 to 30":
                if ( !reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q2").equals("Normal")) {
                    type = "NSTP";
                    percentage = 0.0f; //not clear
                }
                break;
            // 30 to 32
            case "30 to 32":
                if ( reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q2").equals("Yes") ||
                        reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q3").equals("Yes") ||
                        reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q4").equals("Yes")) {
                    type = "NSTP";
                    percentage = 20;
                } else {
                    type = "STP";
                    percentage = 15;
                }
                break;
            case "32.1 to 35":
                if ( reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q2").equals("Yes") ||
                        reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q3").equals("Yes") ||
                        reflexQuestionAndAnswer.getQuestionAndAnswer().get("BMI_Q4").equals("Yes")) {
                    type = "NSTP";
                    percentage = 20;
                } else {
                    type = "STP";
                    percentage = 20;
                }
                break;
        }

        return ReflexLoading.builder()
                .type(type)
                .percent(percentage)
                .build();
    }


    public static ReflexLoading handleDiabetes(ReflexQuestionAndAnswer reflexQuestionAndAnswer) {
        String type = "NSTP";
        Float percentage = 25.0f;

        if ( reflexQuestionAndAnswer.getQuestionAndAnswer().get("DIABETES_Q1").equals("less than 10 years")) {
            if(reflexQuestionAndAnswer.getQuestionAndAnswer().get("DIABETES_Q2").equals("Type-2") &&
                   reflexQuestionAndAnswer.getQuestionAndAnswer().get("DIABETES_Q3").equals("No") &&
                   reflexQuestionAndAnswer.getQuestionAndAnswer().get("DIABETES_Q4").equals("Less than 7%") &&
                   reflexQuestionAndAnswer.getQuestionAndAnswer().get("DIABETES_Q5").equals("15 to 30")) {
                type = "STP";
                percentage = 25.0f;
            }
        }

        return ReflexLoading.builder()
                .type(type)
                .percent(percentage)
                .build();
    }

    public static ReflexLoading handleHypertension(ReflexQuestionAndAnswer reflexQuestionAndAnswer) {
        String type = "NSTP";
        Float percentage = 15.0f;

        if ( reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERTENSION_Q1").equals("less than 10 years")) {
            if(reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERTENSION_Q2").equals("No") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERTENSION_Q3").equals("15 to 30")) {
                type = "STP";
                percentage = 15.0f;
            }
        }

        return ReflexLoading.builder()
                .type(type)
                .percent(percentage)
                .build();
    }

    public static ReflexLoading handleAsthma(ReflexQuestionAndAnswer reflexQuestionAndAnswer) {
        String type = "NSTP";
        Float percentage = 10.0f;

        if ( reflexQuestionAndAnswer.getQuestionAndAnswer().get("ASTHMA_Q1").equals("less than 15 years")) {
            if(reflexQuestionAndAnswer.getQuestionAndAnswer().get("ASTHMA_Q2").equals("Less than twice a week") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("ASTHMA_Q3").equals("No") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("ASTHMA_Q4").equals("No") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("ASTHMA_Q5").equals("No") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("ASTHMA_Q6").equals("More than 70")) {
                type = "STP";
                percentage = 10.0f;
            }
        }

        return ReflexLoading.builder()
                .type(type)
                .percent(percentage)
                .build();
    }

    public static ReflexLoading handleHyperlipidemia(ReflexQuestionAndAnswer reflexQuestionAndAnswer) {
        String type = "NSTP";
        Float percentage = 10.0f;

        if ( reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERLIPIDEMIA_Q1").equals("less than 10 Years")) {
            if(reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERLIPIDEMIA_Q2").equals("2") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERLIPIDEMIA_Q3").equals("less than 240mg/dl") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERLIPIDEMIA_Q4").equals("Less than or equal to 30") &&
                    reflexQuestionAndAnswer.getQuestionAndAnswer().get("HYPERLIPIDEMIA_Q5").equals("No")) {
                type = "STP";
                percentage = 10.0f;
            }
        }

        return ReflexLoading.builder()
                .type(type)
                .percent(percentage)
                .build();
    }

}

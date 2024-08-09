package com.quotes.premium;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class RulesEvaluator {

    public static void main(String[] args) throws IOException {
        // Load properties file
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/main/resources/rules.properties")) {
            properties.load(input);
        }

        // Get the rule from properties file
        String rule = properties.getProperty("rule");

        // JSON input example
        String json = "{ \"first\": { \"name\": \"anil\", \"age\": 30 } }";
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readTree(json);

        // Extract values using JsonPath
        String name = JsonPath.read(json, "$.first.name");
        Integer age = JsonPath.read(json, "$.first.age");

        // Set up SpEL context with variables
        ExpressionParser parser = new SpelExpressionParser();
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("name", name);
        context.setVariable("age", age);

        // Since JsonPath doesn't directly support operations like >, we extract values and apply logic
        String parsedRule = rule
                .replace("$.first.name", "#name")
                .replace("$.first.age", "#age");

        // Parse and evaluate rule
        boolean result = parser.parseExpression(parsedRule).getValue(context, Boolean.class);

        // Output result
        System.out.println(result ? "yes" : "no");
    }
}

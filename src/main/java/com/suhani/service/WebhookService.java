package com.suhani.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.suhani.WebhookResponse;

@Service
public class WebhookService {

    private final RestTemplate restTemplate = new RestTemplate();

    public void generateWebhookAndSubmitSolution() {
        String url = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "John Doe");
        requestBody.put("regNo", "REG12347");
        requestBody.put("email", "john@example.com");

        try {
          
            String rawJson = restTemplate.postForObject(
                url,
                new HttpEntity<>(requestBody),
                String.class
            );

            System.out.println(" RAW JSON Response:");
            System.out.println(rawJson);

           
            ResponseEntity<WebhookResponse> response = restTemplate.postForEntity(
                url,
                new HttpEntity<>(requestBody),
                WebhookResponse.class
            );

            WebhookResponse webhookResponse = response.getBody();

            if (webhookResponse != null) {
                System.out.println("Webhook URL: " + webhookResponse.getWebhookUrl());
                System.out.println("Access Token: " + webhookResponse.getAccessToken());

                if (webhookResponse.getWebhookUrl() == null || webhookResponse.getWebhookUrl().isBlank()) {
                    System.out.println(" Webhook URL is missing. Cannot submit SQL.");
                    return;
                }

                String finalSql = """
                    SELECT 
                        p.AMOUNT AS SALARY,
                        CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME,
                        TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE,
                        d.DEPARTMENT_NAME
                    FROM PAYMENTS p
                    JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
                    JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
                    WHERE DAY(p.PAYMENT_TIME) != 1
                    ORDER BY p.AMOUNT DESC
                    LIMIT 1
                """;

                sendSolution(webhookResponse.getWebhookUrl(), webhookResponse.getAccessToken(), finalSql);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void sendSolution(String webhookUrl, String token, String finalQuery) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token); 
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("finalQuery", finalQuery);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, entity, String.class);
            System.out.println("Webhook Response: " + response.getBody());
        } catch (Exception e) {
            System.out.println("Failed to submit SQL query.");
            e.printStackTrace();
        }
    }

}

package com.vinayak.healing.ai;

import java.time.Duration;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class AiModelClient {

    private static final String URL =
            "http://localhost:11434/api/generate";

    private static final ObjectMapper MAPPER =
            new ObjectMapper();

    public String ask(String prompt)
            throws Exception {

        OkHttpClient client =
                new OkHttpClient.Builder()
                        .connectTimeout(
                                Duration.ofSeconds(30))
                        .readTimeout(
                                Duration.ofSeconds(180))
                        .writeTimeout(
                                Duration.ofSeconds(30))
                        .build();

        // ==========================================
        // BUILD REQUEST JSON SAFELY
        // ==========================================

        Map<String, Object> requestData =
                new HashMap<>();

        requestData.put(
                "model",
                "qwen3:8b");

        requestData.put(
                "prompt",
                prompt);

        requestData.put(
        "stream",
        false);

requestData.put(
        "think",
        false);

        String json =
                MAPPER.writeValueAsString(
                        requestData);

        System.out.println(
                "\n===== AI REQUEST =====");

        System.out.println(
                "Model : qwen3:8b");

        System.out.println(
                "Prompt Length : "
                        + (prompt == null
                        ? 0
                        : prompt.length()));

        // ==========================================
        // HTTP REQUEST
        // ==========================================

        RequestBody body =
                RequestBody.create(
                        json,
                        MediaType.parse(
                                "application/json"));

        Request request =
                new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();

        long start =
                System.currentTimeMillis();

        try (Response response =
                     client.newCall(request)
                             .execute()) {

            long responseTime =
                    System.currentTimeMillis()
                            - start;

            System.out.println(
                    "AI Response Time = "
                            + responseTime
                            + " ms");

            // ======================================
            // HANDLE HTTP FAILURE
            // ======================================

            if (!response.isSuccessful()) {

                String errorBody =
                        response.body() == null
                                ? ""
                                : response.body().string();

                System.out.println(
                        "AI HTTP ERROR = "
                                + errorBody);

                throw new RuntimeException(
                        "AI call failed. HTTP Code: "
                                + response.code()
                                + " | Response: "
                                + errorBody);
            }

            // ======================================
            // HANDLE EMPTY RESPONSE
            // ======================================

            if (response.body() == null) {

                throw new RuntimeException(
                        "AI returned empty response");
            }

            String responseBody =
                    response.body().string();

            System.out.println(
                    "\n===== AI RESPONSE RECEIVED =====");

            System.out.println(
                    responseBody);

            return responseBody;
        }
    }
}
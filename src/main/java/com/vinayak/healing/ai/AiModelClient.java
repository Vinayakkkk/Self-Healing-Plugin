package com.vinayak.healing.ai;
import java.time.Duration;
import okhttp3.*;

public class AiModelClient {

    private static final String URL =
            "http://localhost:11434/api/generate";

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

        String json = """
{
"model":"qwen2.5:7b",
"prompt":%s,
"stream":false
}
"""
                .formatted(
                        "\"" +
                        prompt.replace("\"","\\\"")
                                .replace("\n","\\n")
                        + "\"");

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
        Response response =
        client.newCall(request)
                .execute();

                System.out.println(
        "AI Response Time = "
                + (System.currentTimeMillis() - start)
                + " ms");

if (!response.isSuccessful()) {

    throw new RuntimeException(
            "AI call failed. HTTP Code: "
                    + response.code());
}

if (response.body() == null) {

    throw new RuntimeException(
            "AI returned empty response");
}

return response.body().string();
    }
}
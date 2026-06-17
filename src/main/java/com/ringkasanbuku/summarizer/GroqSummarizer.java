package com.ringkasanbuku.summarizer;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.List;
import java.util.Map;

public class GroqSummarizer extends LLMSummarizer {

    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private final Gson gson = new Gson();

    @Override
    protected String callAPI(String prompt, int maxTokens) {
        String body = gson.toJson(Map.of(
                "model", MODEL,
                "max_tokens", maxTokens,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt))));

        Request request = new Request.Builder()
                .url(API_URL)
                .header("Authorization", "Bearer " + apiKey)
                .post(RequestBody.create(body, JSON_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP " + response.code() + " - " + res);
            }

            return JsonParser.parseString(res).getAsJsonObject()
                    .getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message")
                    .get("content").getAsString();
        } catch (Exception e) {
            throw new RuntimeException("Groq API error: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getEnvKeyName() {
        return "GROQ_API_KEY";
    }

    @Override
    public String getProviderName() {
        return "GROQ";
    }
}
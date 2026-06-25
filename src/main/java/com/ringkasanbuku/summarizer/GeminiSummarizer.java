package com.ringkasanbuku.summarizer;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.util.List;
import java.util.Map;

public class GeminiSummarizer extends LLMSummarizer {

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent";
    private final Gson gson = new Gson();

    @Override
    // Method khusus untuk melakukan HTTP request (mengirim teks) ke server / API milik Google Gemini.
    // Format JSON yang dikirimkan (body) sudah disesuaikan dengan aturan API Gemini (memiliki 'candidates' dan 'parts').
    protected String callAPI(String prompt, int maxTokens) {
        String url = API_URL + "?key=" + apiKey;
        
        String body = gson.toJson(Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(Map.of("text", prompt)))),
                "generationConfig", Map.of(
                        "maxOutputTokens", maxTokens,
                        "thinkingConfig", Map.of("thinking_level", "MINIMAL")))); // ← "MINIMAL" bukan "low", pakai ALL_CAPS

        Request request = new Request.Builder()
                .url(url)
                .post(RequestBody.create(body, JSON_TYPE))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String res = response.body().string();

            if (!response.isSuccessful()) {
                throw new RuntimeException("HTTP " + response.code() + " - " + res);
            }

            var root = JsonParser.parseString(res).getAsJsonObject();

            // ✅ Cek candidates null dulu (safety block atau config error)
            if (!root.has("candidates") || root.getAsJsonArray("candidates").isEmpty()) {
                String feedback = root.has("promptFeedback") 
                    ? root.get("promptFeedback").toString() 
                    : res; // print raw response kalau ga ada feedback
                throw new RuntimeException("No candidates returned. Response: " + feedback);
            }

            return root.getAsJsonArray("candidates").get(0).getAsJsonObject()
                    .getAsJsonObject("content")
                    .getAsJsonArray("parts").get(0).getAsJsonObject()
                    .get("text").getAsString();

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Gemini API error: " + e.getMessage(), e);
        }
    }

    @Override
    protected String getEnvKeyName() {
        return "GEMINI_API_KEY";
    }

    @Override
    public String getProviderName() {
        return "GEMINI";
    }
}
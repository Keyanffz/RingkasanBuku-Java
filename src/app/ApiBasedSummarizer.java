package app;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublishers;

public class ApiBasedSummarizer implements Summarizer {

    private static final String API_KEY = System.getenv("GROQ_API_KEY");
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions";

    @Override
    public String summarize(String text) {
        try {
            // Bersihkan teks sebelum dikirim ke API
            String cleanText = text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", " ")
                .replace("\r", " ")
                .replace("\t", " ");

            String body = "{"
                + "\"model\": \"llama-3.3-70b-versatile\","
                + "\"messages\": ["
                + "{"
                + "\"role\": \"user\","
                + "\"content\": \"Ringkas teks berikut dalam 3-4 kalimat bahasa Indonesia:\\n\\n" + cleanText + "\""
                + "}"
                + "]"
                + "}";

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + API_KEY)
                .POST(BodyPublishers.ofString(body))
                .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("RESPONSE: " + response.body());

            // Ambil teks dari response JSON
            String result = response.body();
            int start = result.indexOf("\"content\":\"") + 11;
            int end = result.indexOf("\"", start);
            return result.substring(start, end)
                .replace("\\n", "\n")
                .replace("\\\"", "\"");

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
package com.ringkasanbuku.summarizer;

import com.ringkasanbuku.model.SummaryOptions;

import java.util.ArrayList;
import java.util.List;

public class APIFallbackManager {
    private List<LLMSummarizer> providers = new ArrayList<>();

    public void addProvider(LLMSummarizer provider) {
        providers.add(provider);
    }

    public String summarize(String text, SummaryOptions options) {
        for (LLMSummarizer provider : providers) {
            try {
                return provider.summarize(text, options);
            } catch (Exception e) {
                System.err.println(provider.getProviderName() + " API error: " + e.getMessage() + ". Mencoba provider selanjutnya...");
            }
        }
        throw new RuntimeException("Maaf, token API anda habis atau API Key tidak tersedia/tidak valid di file .env. Silakan periksa kembali konfigurasi API anda.");
    }
}

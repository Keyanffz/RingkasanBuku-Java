package com.ringkasanbuku.summarizer;

import com.ringkasanbuku.model.SummaryOptions;

import java.util.ArrayList;
import java.util.List;

public class APIFallbackManager {
    private java.util.List<LLMSummarizer> providers = new java.util.ArrayList<>();
    private int currentProviderIndex = 0;

    public void addProvider(LLMSummarizer provider) {
        providers.add(provider);
    }

    public String summarize(String text, SummaryOptions options) {
        if (providers.isEmpty()) {
            throw new RuntimeException("Tidak ada provider API yang dikonfigurasi.");
        }
        
        int startIndex = currentProviderIndex;
        int attempts = 0;
        
        while (attempts < providers.size()) {
            LLMSummarizer provider = providers.get(currentProviderIndex);
            try {
                String result = provider.summarize(text, options);
                String keyInfo = provider.getCurrentKeyInfo();
                System.out.println("[\u2713] Sukses meringkas bagian ini menggunakan provider: " + provider.getProviderName() + " dengan key " + keyInfo);
                return result;
            } catch (Exception e) {
                System.err.println(provider.getProviderName() + " API error: " + e.getMessage()
                        + ". Mencoba provider selanjutnya...");
                // Geser ke provider selanjutnya
                currentProviderIndex = (currentProviderIndex + 1) % providers.size();
                attempts++;
            }
        }
        throw new RuntimeException(
                "Semua provider API limit atau gagal. Silakan periksa kembali konfigurasi API anda.");
    }
}

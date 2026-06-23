package com.ringkasanbuku.summarizer;

public class APITester {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("        API KEY HEALTH CHECKER           ");
        System.out.println("=========================================");
        
        testProvider(new GroqSummarizer());
        testProvider(new GeminiSummarizer());
        testProvider(new OpenAISummarizer());
        
        System.out.println("\n=========================================");
        System.out.println("           TESTING SELESAI               ");
        System.out.println("=========================================");
    }

    private static void testProvider(LLMSummarizer provider) {
        System.out.println("\n\u25B6 Provider: " + provider.getProviderName());
        
        if (provider.apiKeys == null || provider.apiKeys.isEmpty()) {
            System.out.println("  \u274C Tidak ada API Key yang dikonfigurasi di .env");
            return;
        }

        for (int i = 0; i < provider.apiKeys.size(); i++) {
            // Set API key yang akan ditest
            provider.apiKey = provider.apiKeys.get(i);
            provider.currentKeyIndex = i;
            
            String keyInfo = provider.getCurrentKeyInfo();
            System.out.print("  \u2023 Menguji " + keyInfo + " ... ");
            
            try {
                // Kita kirim request paling minimal untuk hemat token saat nge-test
                provider.callAPI("Balas pesan ini dengan kata 'OK' saja.", 10);
                System.out.println("\u2705 AKTIF (Siap digunakan)");
            } catch (Exception e) {
                String errMsg = e.getMessage().toLowerCase();
                // Deteksi tipe error
                if (errMsg.contains("429") || errMsg.contains("rate limit") || errMsg.contains("too many requests")) {
                    System.out.println("\u26A0\uFE0F KENA LIMIT (Rate Limited)");
                    System.out.println("      Detail: " + e.getMessage());
                } else if (errMsg.contains("401") || errMsg.contains("unauthorized") || errMsg.contains("invalid")) {
                    System.out.println("\u274C API KEY SALAH (Invalid/Unauthorized)");
                } else {
                    System.out.println("\u274C ERROR (" + e.getMessage() + ")");
                }
            }
        }
    }
}

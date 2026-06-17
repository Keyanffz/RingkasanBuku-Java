package com.ringkasanbuku.summarizer;

import com.ringkasanbuku.model.SummaryOptions;
import com.ringkasanbuku.util.EnvLoader;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;

import java.util.concurrent.TimeUnit;

public abstract class LLMSummarizer implements Summarizer {

    protected String apiKey;
    protected OkHttpClient client;
    protected static final MediaType JSON_TYPE = MediaType.get("application/json; charset=utf-8");

    public LLMSummarizer() {
        this.apiKey = EnvLoader.get(getEnvKeyName());
        this.client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();
    }

    @Override
    public String summarize(String text, SummaryOptions options) {
        String prompt = buildPrompt(text, options);
        return callAPI(prompt, options.getMaxTokens());
    }

    // wajib diimplementasi tiap subclass
    protected abstract String callAPI(String prompt, int maxTokens);
    protected abstract String getEnvKeyName();
    public abstract String getProviderName();

    protected String buildPrompt(String text, SummaryOptions options) {
        return "Kamu adalah asisten yang bertugas meringkas teks dalam Bahasa Indonesia.\n\n"
            + options.toPromptInstruction()
            + "\n\nTeks:\n" + text;
    }
}

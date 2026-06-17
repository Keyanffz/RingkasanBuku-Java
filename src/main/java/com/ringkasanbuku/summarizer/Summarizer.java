package com.ringkasanbuku.summarizer;

import com.ringkasanbuku.model.SummaryOptions;

public interface Summarizer {
    String summarize(String text, SummaryOptions options);
}

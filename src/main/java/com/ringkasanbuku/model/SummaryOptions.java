package com.ringkasanbuku.model;

public class SummaryOptions {
    private SummaryLength length;
    private SummaryStyle style;

    public SummaryOptions(SummaryLength length, SummaryStyle style) {
        this.length = length;
        this.style = style;
    }

    public int getSentenceCount(int totalSentences) {
        return switch (length) {
            case SHORT -> Math.max(3, (int) Math.ceil(totalSentences * 0.2));
            case MEDIUM -> Math.max(5, (int) Math.ceil(totalSentences * 0.4));
            case LONG -> Math.max(8, (int) Math.ceil(totalSentences * 0.6));
        };
    }

    public int getMaxTokens() {
        return switch (length) {
            case SHORT -> 150;
            case MEDIUM -> 300;
            case LONG -> 1500;
        };
    }

    public String toPromptInstruction() {
        return switch (length) {
            case SHORT -> "Buat ringkasan sangat singkat, maksimal 2-3 kalimat.";
            case MEDIUM -> "Buat ringkasan sedang, 4-5 kalimat, pertahankan poin utama.";
            case LONG -> "Buat ringkasan lengkap, pertahankan semua poin penting.";
        };
    }

    public SummaryLength getLength() {
        return length;
    }

    public SummaryStyle getStyle() {
        return style;
    }

    public void setLength(SummaryLength length) {
        this.length = length;
    }

    public void setStyle(SummaryStyle style) {
        this.style = style;
    }
}

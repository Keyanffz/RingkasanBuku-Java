package com.ringkasanbuku.model;

public class SummaryOptions {
    private SummaryLength length;
    private SummaryStyle style;

    public SummaryOptions(SummaryLength length, SummaryStyle style) {
        this.length = length;
        this.style = style;
    }

    public int getSentenceCount() {
        return switch (length) {
            case SHORT  -> 3;
            case MEDIUM -> 5;
            case LONG   -> 8;
        };
    }

    public int getMaxTokens() {
        return switch (length) {
            case SHORT  -> 150;
            case MEDIUM -> 300;
            case LONG   -> 1500; // <-- Naikkan nilainya
        };
    }

    public String toPromptInstruction() {
        return switch (length) {
            case SHORT  -> "Buat ringkasan sangat singkat, maksimal 2-3 kalimat.";
            case MEDIUM -> "Buat ringkasan sedang, 4-5 kalimat, pertahankan poin utama.";
            case LONG   -> "Buat ringkasan lengkap, pertahankan semua poin penting.";
        };
    }

    public SummaryLength getLength() { return length; }
    public SummaryStyle getStyle() { return style; }
    public void setLength(SummaryLength length) { this.length = length; }
    public void setStyle(SummaryStyle style) { this.style = style; }
}

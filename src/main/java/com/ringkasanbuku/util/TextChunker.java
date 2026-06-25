package com.ringkasanbuku.util;

import com.ringkasanbuku.model.Book;
import com.ringkasanbuku.model.Chapter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TextChunker {
    private final int maxChars;
    private final int overlap;

    public TextChunker(int maxChars, int overlap) {
        this.maxChars = maxChars;
        this.overlap = overlap;
    }

    // Memotong-motong (chunking) teks panjang menjadi beberapa bagian kecil
    // agar tidak melebihi limit token saat dikirim ke LLM (seperti Groq/Gemini).
    // Potongan juga dibuat sedikit tumpang tindih (overlap) agar konteks kalimat tidak terputus.
    public List<String> chunk(String text) {
        List<String> chunks = new ArrayList<>();
        if (text == null || text.isBlank())
            return chunks;

        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());

            if (end < text.length()) {
                // Memastikan pemotongan teks dilakukan pada akhir kata (spasi), bukan di tengah kata
                int lastSpace = text.lastIndexOf(' ', end);
                if (lastSpace > start)
                    end = lastSpace;
            }

            chunks.add(text.substring(start, end).strip());

            if (end >= text.length())
                break;
            start = end - overlap;
            if (start < 0)
                start = 0;
        }
        return chunks;
    }

    public Map<Chapter, List<String>> chunkByChapter(Book book) {
        Map<Chapter, List<String>> result = new LinkedHashMap<>();
        for (Chapter chapter : book.getChapters()) {
            result.put(chapter, chunk(chapter.getContent()));
        }
        return result;
    }
}
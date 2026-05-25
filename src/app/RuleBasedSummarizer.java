package app;
import java.util.*;

public class RuleBasedSummarizer implements Summarizer {
    @Override
    public String summarize(String text) {
        // 1. Pecah jadi kalimat, simpan urutan aslinya
        String[] sentences = text.split("(?<=[.!?])\\s+");

        // 2. Hitung frekuensi kata (skip kata pendek)
        Map<String, Integer> freq = new HashMap<>();
        for (String word : text.toLowerCase().split("\\s+")) {
            if (word.length() > 3) { // skip kata seperti "di", "dan", "yang"
                freq.put(word, freq.getOrDefault(word, 0) + 1);
            }
        }

        // 3. Kasih skor tiap kalimat
        Map<Integer, Integer> scores = new LinkedHashMap<>();
        for (int i = 0; i < sentences.length; i++) {
            int score = 0;
            for (String word : sentences[i].toLowerCase().split("\\s+")) {
                score += freq.getOrDefault(word, 0);
            }
            scores.put(i, score); // simpan INDEX kalimat, bukan kalimatnya
        }

        // 4. Ambil 3 index dengan skor tertinggi
        List<Integer> topIndex = scores.entrySet().stream()
            .sorted(Map.Entry.<Integer, Integer>comparingByValue().reversed())
            .limit(3)
            .map(Map.Entry::getKey)
            .sorted() // ← urutkan balik sesuai urutan asli!
            .toList();

        // 5. Gabungkan kalimat sesuai urutan asli
        StringBuilder hasil = new StringBuilder();
        for (int i : topIndex) {
            hasil.append(sentences[i].trim()).append(" ");
        }

        return hasil.toString().trim();
    }
}
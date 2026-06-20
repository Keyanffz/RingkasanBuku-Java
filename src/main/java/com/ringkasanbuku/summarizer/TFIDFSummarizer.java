package com.ringkasanbuku.summarizer;

import com.ringkasanbuku.model.SummaryOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class TFIDFSummarizer implements Summarizer {

    private List<String> stopwords;

    private static final Set<String> ABBREVIATIONS = Set.of(
        "dr", "ir", "drs", "prof", "tbk", "pt", "cv", "no", "jl",
        "dll", "dst", "yth", "an", "sda", "hlm", "vol", "ed", "dkk"
    );

    public TFIDFSummarizer() {
        this.stopwords = loadStopwords();
    }

    private List<String> loadStopwords() {
        List<String> defaults = new ArrayList<>(Arrays.asList(
            "yang", "dan", "di", "ke", "dari", "ini", "itu", "dengan",
            "untuk", "pada", "adalah", "dalam", "tidak", "akan", "juga",
            "karena", "ada", "oleh", "atau", "bisa", "sudah", "saya",
            "kami", "kita", "mereka", "dia", "ia", "nya", "kamu", "anda",
            "bagi", "telah", "dapat", "lebih", "jika", "maka", "agar",
            "seperti", "saja", "namun", "bahwa", "sehingga", "ketika",
            "hal", "sebuah", "saat", "antara", "setelah", "hingga",
            "sejak", "selama", "belum", "hanya", "jadi", "tapi", "tetapi",
            "walaupun", "meskipun", "pun", "pula", "masih", "sedang",
            "lagi", "tersebut", "merupakan", "yakni", "yaitu", "bahkan",
            "secara", "salah", "satu", "dua", "tiga", "sangat", "lebih",
            "paling", "sangat", "amat", "semua", "setiap", "para"
        ));
        try {
            InputStream is = getClass().getResourceAsStream("/stopwords_id.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                List<String> fromFile = reader.lines()
                    .map(String::trim)
                    .filter(l -> !l.isEmpty())
                    .collect(Collectors.toList());
                if (!fromFile.isEmpty()) return fromFile;
            }
        } catch (Exception ignored) {}
        return defaults;
    }

    @Override
    public String summarize(String text, SummaryOptions options) {
        List<String> sentences = splitSentences(text);
        if (sentences.size() <= options.getSentenceCount()) return text;

        Map<String, Double> idf = computeIDF(sentences);

        Map<Integer, Double> scores = new LinkedHashMap<>();
        for (int i = 0; i < sentences.size(); i++) {
            scores.put(i, scoreSentence(sentences.get(i), idf));
        }

        int topN = Math.min(options.getSentenceCount(), sentences.size());
        List<Integer> topIndices = scores.entrySet().stream()
            .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
            .limit(topN)
            .map(Map.Entry::getKey)
            .sorted()
            .collect(Collectors.toList());

        return topIndices.stream()
            .map(sentences::get)
            .collect(Collectors.joining(" "));
    }

    private List<String> splitSentences(String text) {
        String[] rawParts = text.split("(?<=[.!?])\\s+(?=[A-Z0-9\"'])");
        List<String> sentences = new ArrayList<>();
        StringBuilder buffer = new StringBuilder();

        for (String part : rawParts) {
            if (buffer.length() > 0) buffer.append(" ");
            buffer.append(part);

            if (endsWithAbbreviationOrListNumber(buffer.toString())) {
                continue;
            }

            sentences.add(buffer.toString().trim());
            buffer.setLength(0);
        }
        if (buffer.length() > 0) sentences.add(buffer.toString().trim());

        return sentences.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private boolean endsWithAbbreviationOrListNumber(String segment) {
        String trimmed = segment.trim();
        if (!trimmed.endsWith(".")) return false;
        String withoutDot = trimmed.substring(0, trimmed.length() - 1);
        int lastSpace = withoutDot.lastIndexOf(' ');
        String lastWord = (lastSpace >= 0 ? withoutDot.substring(lastSpace + 1) : withoutDot).toLowerCase();

        if (lastWord.matches("\\d{1,2}")) return true;
        return ABBREVIATIONS.contains(lastWord);
    }

    private List<String> tokenize(String sentence) {
        String normalized = sentence.toLowerCase()
            .replaceAll("(\\b\\w+)-\\1\\b", "$1");
        return Arrays.stream(normalized.split("\\W+"))
            .filter(w -> !w.isEmpty() && !stopwords.contains(w) && w.length() > 2)
            .collect(Collectors.toList());
    }

    private Map<String, Double> computeTF(String sentence) {
        List<String> tokens = tokenize(sentence);
        Map<String, Double> tf = new HashMap<>();
        if (tokens.isEmpty()) return tf;
        for (String token : tokens) tf.merge(token, 1.0, Double::sum);
        int total = tokens.size();
        tf.replaceAll((k, v) -> v / total);
        return tf;
    }

    private Map<String, Double> computeIDF(List<String> sentences) {
        Map<String, Integer> docFreq = new HashMap<>();
        for (String sentence : sentences) {
            new HashSet<>(tokenize(sentence)).forEach(t -> docFreq.merge(t, 1, Integer::sum));
        }
        Map<String, Double> idf = new HashMap<>();
        int N = sentences.size();
        docFreq.forEach((k, v) -> idf.put(k, Math.log((double) N / v)));
        return idf;
    }

    private double scoreSentence(String sentence, Map<String, Double> idf) {
        Map<String, Double> tf = computeTF(sentence);
        if (tf.isEmpty()) return 0.0;
        return tf.entrySet().stream()
            .mapToDouble(e -> e.getValue() * idf.getOrDefault(e.getKey(), 0.0))
            .sum();
    }
}
package com.ringkasanbuku.summarizer;

import com.ringkasanbuku.model.SummaryOptions;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class TFIDFSummarizer implements Summarizer {

    private List<String> stopwords;
    private Set<String> abbreviations;

    public TFIDFSummarizer() {
        this.stopwords = loadStopwords();
        this.abbreviations = loadAbbreviations();
    }

    private Set<String> loadAbbreviations() {
        Set<String> defaults = Set.of(
                "dr", "ir", "drs", "prof", "tbk", "pt", "cv", "no", "jl",
                "dll", "dst", "yth", "an", "sda", "hlm", "vol", "ed", "dkk");
        try {
            InputStream is = getClass().getResourceAsStream("/abbreviations_id.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                Set<String> fromFile = reader.lines()
                        .map(String::trim)
                        .filter(l -> !l.isEmpty())
                        .collect(Collectors.toSet());
                if (!fromFile.isEmpty()) return fromFile;
            }
        } catch (Exception ignored) {
        }
        return defaults;
    }

    private List<String> loadStopwords() {
        List<String> defaults = new ArrayList<>();
        for (Object stopWord : org.apache.lucene.analysis.id.IndonesianAnalyzer.getDefaultStopSet()) {
            if (stopWord instanceof char[]) {
                defaults.add(new String((char[]) stopWord));
            } else {
                defaults.add(stopWord.toString());
            }
        }
        try {
            InputStream is = getClass().getResourceAsStream("/stopwords_id.txt");
            if (is != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                List<String> fromFile = reader.lines()
                        .map(String::trim)
                        .filter(l -> !l.isEmpty())
                        .collect(Collectors.toList());
                if (!fromFile.isEmpty())
                    return fromFile;
            }
        } catch (Exception ignored) {
        }
        return defaults;
    }

    @Override
    // Method utama untuk melakukan ringkasan berbasis rule-based (TF-IDF)
    public String summarize(String text, SummaryOptions options) {
        List<String> sentences = splitSentences(text);
        int targetSentenceCount = options.getSentenceCount(sentences.size());
        if (sentences.size() <= targetSentenceCount) return text;

        Map<String, Double> idf = computeIDF(sentences);

        Map<Integer, Double> scores = new LinkedHashMap<>();
        for (int i = 0; i < sentences.size(); i++) {
            scores.put(i, scoreSentence(sentences.get(i), idf));
        }

        int topN = Math.min(targetSentenceCount, sentences.size());
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
            if (buffer.length() > 0)
                buffer.append(" ");
            buffer.append(part);

            if (endsWithAbbreviationOrListNumber(buffer.toString())) {
                continue;
            }

            sentences.add(buffer.toString().trim());
            buffer.setLength(0);
        }
        if (buffer.length() > 0)
            sentences.add(buffer.toString().trim());

        return sentences.stream().filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    private boolean endsWithAbbreviationOrListNumber(String segment) {
        String trimmed = segment.trim();
        if (!trimmed.endsWith("."))
            return false;
        String withoutDot = trimmed.substring(0, trimmed.length() - 1);
        int lastSpace = withoutDot.lastIndexOf(' ');
        String lastWord = (lastSpace >= 0 ? withoutDot.substring(lastSpace + 1) : withoutDot).toLowerCase();

        if (lastWord.matches("\\d{1,2}"))
            return true;
        return abbreviations.contains(lastWord);
    }

    private List<String> tokenize(String sentence) {
        String normalized = sentence.toLowerCase()
                .replaceAll("(\\b\\w+)-\\1\\b", "$1");
        return Arrays.stream(normalized.split("\\W+"))
                .filter(w -> !w.isEmpty() && !stopwords.contains(w) && w.length() > 2)
                .collect(Collectors.toList());
    }

    // Menghitung seberapa sering sebuah kata (term) muncul dalam satu kalimat (Term
    // Frequency)
    private Map<String, Double> computeTF(String sentence) {
        List<String> tokens = tokenize(sentence);
        Map<String, Double> tf = new HashMap<>();
        if (tokens.isEmpty())
            return tf;
        for (String token : tokens)
            tf.merge(token, 1.0, Double::sum);
        int total = tokens.size();
        tf.replaceAll((k, v) -> v / total);
        return tf;
    }

    // Menghitung bobot pentingnya sebuah kata (Inverse Document Frequency)
    // Berdasarkan seberapa jarang kata tersebut muncul di seluruh teks/kalimat.
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

    // Menghitung skor akhir sebuah kalimat dengan mengalikan nilai TF dan IDF.
    // Kalimat dengan skor tertinggi akan dipilih menjadi bagian dari ringkasan.
    private double scoreSentence(String sentence, Map<String, Double> idf) {
        Map<String, Double> tf = computeTF(sentence);
        if (tf.isEmpty())
            return 0.0;
        return tf.entrySet().stream()
                .mapToDouble(e -> e.getValue() * idf.getOrDefault(e.getKey(), 0.0))
                .sum();
    }
}
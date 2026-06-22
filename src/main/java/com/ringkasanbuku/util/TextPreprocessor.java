package com.ringkasanbuku.util;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class TextPreprocessor {

    private static final List<String> START_SECTION_MARKERS = Arrays.asList(
        "1. pendahuluan",
        "1.pendahuluan",
        "i. pendahuluan",
        "pendahuluan",
        "1. introduction",
        "introduction",
        "latar belakang"
    );

    private static final List<String> END_SECTION_MARKERS = Arrays.asList(
        "daftar pustaka",
        "references",
        "bibliography",
        "ucapan terima kasih",
        "acknowledgement",
        "acknowledgments"
    );

    private static final List<Pattern> NOISE_PATTERNS = Arrays.asList(
        Pattern.compile("(?i).{5,50}\\|\\s*Page\\s*\\d+.*"),
        Pattern.compile("(?i)^(DOI|WEB|E-ISSN|CC|ISSN)\\s*[:.].*"),
        Pattern.compile("(?i)^Volume\\s*\\d+.*Nomor.*"),
        Pattern.compile("(?i)^[\\w\\s,]+@[\\w.]+.*"),
        Pattern.compile("(?i)^(Gambar|Tabel|Figure|Table)\\s*\\d+[.:].*"),
        Pattern.compile("(?i)^\\d+\\.?\\s+CC\\s+Attribution.*"),
        Pattern.compile("^[A-Z][a-z]+\\s*:\\s*Jurnal.*"),
        Pattern.compile("^\\s*\\d{1,2}\\*?\\s*$")
    );

    public static String clean(String rawText) {
        if (rawText == null || rawText.isBlank()) return rawText;

        System.out.println("=== TextPreprocessor INPUT: " + rawText.length() + " chars ===");

        String result = rawText;
        result = joinBrokenLines(result);
        result = startFromMainContent(result);
        result = truncateAtEndSection(result);
        result = removeNoiseLines(result);
        result = cleanListPrefixes(result);
        result = removeCitations(result);
        result = result.replaceAll("\\n{3,}", "\n\n").trim();

        System.out.println("=== TextPreprocessor OUTPUT: " + result.length() + " chars ===");
        System.out.println("=== Preview: " + result.substring(0, Math.min(300, result.length())) + " ===");

        return result;
    }

    // Gabungkan baris yang terpotong di tengah kalimat (masalah PDF 2 kolom)
    private static String joinBrokenLines(String text) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                sb.append("\n\n");
                continue;
            }
            sb.append(line);
            if (i < lines.length - 1) {
                String next = lines[i + 1].trim();
                boolean currentEndsWithSentence = line.matches(".*[.!?]$");
                boolean currentEndsWithConnector = line.matches(".*[&\\-,:]$");
                boolean nextStartsLower = !next.isEmpty()
                    && Character.isLowerCase(next.charAt(0));
                boolean nextStartsUpper = !next.isEmpty()
                    && Character.isUpperCase(next.charAt(0));
                boolean nextIsEmpty = next.isEmpty();

                if (!currentEndsWithSentence && !nextIsEmpty
                    && (nextStartsLower || currentEndsWithConnector)) {
                    sb.append(" ");
                } else {
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }

    // Mulai dari bagian PENDAHULUAN, skip abstrak dan header
    private static String startFromMainContent(String text) {
        String lower = text.toLowerCase();
        for (String marker : START_SECTION_MARKERS) {
            int idx = lower.indexOf(marker);
            if (idx == -1) continue;

            int lineStart = lower.lastIndexOf('\n', idx);
            if (lineStart == -1) lineStart = 0; else lineStart++;

            String beforeMarker = lower.substring(lineStart, idx).trim();
            boolean isHeading = beforeMarker.isEmpty()
                || beforeMarker.matches("\\d+\\.?")
                || beforeMarker.matches("[ivxlcdm]+\\.?");

            if (isHeading) {
                return text.substring(lineStart);
            }
        }
        return text;
    }

    private static String truncateAtEndSection(String text) {
        String lower = text.toLowerCase();
        int cutAt = text.length();

        for (String marker : END_SECTION_MARKERS) {
            int searchFrom = 0;
            while (searchFrom < lower.length()) {
                int idx = lower.indexOf(marker, searchFrom);
                if (idx == -1) break;

                int lineStart = lower.lastIndexOf('\n', idx);
                if (lineStart == -1) lineStart = 0; else lineStart++;

                String beforeMarker = lower.substring(lineStart, idx).trim();
                boolean isHeading = beforeMarker.isEmpty()
                    || beforeMarker.matches("\\d+\\.?")
                    || beforeMarker.matches("[ivxlcdm]+\\.?");

                if (isHeading && idx < cutAt) {
                    cutAt = lineStart;
                    break;
                }
                searchFrom = idx + 1;
            }
        }
        return text.substring(0, cutAt).trim();
    }

    private static String removeNoiseLines(String text) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) { sb.append("\n"); continue; }
            boolean isNoise = false;
            for (Pattern p : NOISE_PATTERNS) {
                if (p.matcher(trimmed).matches()) { isNoise = true; break; }
            }
            if (!isNoise) sb.append(line).append("\n");
        }
        return sb.toString();
    }

    // Buang sitasi inline seperti (Gusteti et al., 2022)
    private static String removeCitations(String text) {
        return text.replaceAll("\\([A-Z][^)]*et al\\.?,?\\s*\\d{4}[^)]*\\)", "")
                   .replaceAll("\\([A-Z][a-zA-Z]+\\s*et al\\.?\\)", "")
                   .replaceAll("\\(\\w+\\s+et al\\.,?\\s*\\d{4}\\)", "")
                   .replaceAll("  +", " ")
                   .trim();
    }

    private static String cleanListPrefixes(String text) {
        // buang prefix seperti "1)", "2).", "1.", "2.", "a)", "b)"
        // di awal baris atau awal kalimat
        String result = text.replaceAll("(?m)^\\s*\\d+[).:]\\s+", "");
        result = result.replaceAll("(?m)^\\s*[a-z][).:]\\s+", "");
        // buang superscript angka yang nempel di nama (dari PDF)
        // seperti "Khomarudin1*" -> "Khomarudin"
        result = result.replaceAll("([a-zA-Z])\\d+[*]?\\b", "$1");
        return result;
    }
}

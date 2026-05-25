package app;
import java.util.*;

public class SummaryHistoryManager {
    private List<String> riwayat = new ArrayList<>();

    public void tambahRiwayat(String ringkasan) {
        riwayat.add(ringkasan);
    }

    public String getRiwayat() {
        if (riwayat.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < riwayat.size(); i++) {
            sb.append("--- Ringkasan ").append(i + 1).append(" ---\n");
            sb.append(riwayat.get(i)).append("\n\n");
        }
        return sb.toString();
    }
}
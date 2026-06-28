import java.text.BreakIterator;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class TestBreakIterator {
    public static void main(String[] args) {
        String text = "Dr. Andi pergi ke pasar. PT. Telkom sangat maju. Beliau lahir di Jl. Sudirman no. 5 Jakarta.";
        BreakIterator iterator = BreakIterator.getSentenceInstance(new Locale("id", "ID"));
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            System.out.println("-> " + text.substring(start, end).trim());
        }
    }
}

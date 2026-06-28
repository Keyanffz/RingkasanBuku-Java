import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.util.ULocale;

public class TestIcu {
    public static void main(String[] args) {
        String text = "Dr. Andi pergi ke pasar. PT. Telkom sangat maju. Beliau lahir di Jl. Sudirman no. 5 Jakarta.";
        BreakIterator iterator = BreakIterator.getSentenceInstance(new ULocale("id", "ID"));
        iterator.setText(text);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            System.out.println("-> " + text.substring(start, end).trim());
        }
    }
}

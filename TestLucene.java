import org.apache.lucene.analysis.id.IndonesianAnalyzer;
import org.apache.lucene.analysis.CharArraySet;

public class TestLucene {
    public static void main(String[] args) {
        CharArraySet stops = IndonesianAnalyzer.getDefaultStopSet();
        for (Object stop : stops) {
            System.out.print(new String((char[])stop) + " ");
        }
    }
}

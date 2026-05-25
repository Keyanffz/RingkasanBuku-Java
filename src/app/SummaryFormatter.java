package app;

import java.io.*;
import java.nio.file.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

public class SummaryFormatter {

    public void simpanKeTxt(String hasil) {
        try {
            Files.writeString(Path.of("hasil_ringkasan.txt"), hasil);
        } catch (IOException e) {
            System.out.println("Gagal menyimpan: " + e.getMessage());
        }
    }

    public void simpanKePdf(String hasil) {
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream("hasil_ringkasan.pdf"));
            document.open();
            document.add(new Paragraph("Hasil Ringkasan"));
            document.add(new Paragraph(" ")); // spasi
            document.add(new Paragraph(hasil));
            document.close();
        } catch (Exception e) {
            System.out.println("Gagal menyimpan PDF: " + e.getMessage());
        }
    }
}
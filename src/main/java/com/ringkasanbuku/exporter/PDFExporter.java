package com.ringkasanbuku.exporter;

import com.ringkasanbuku.model.Summary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFExporter implements FileExporter {
    @Override
    public void export(Summary summary, String path) throws IOException {
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(doc, page)) {
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.beginText();
                contentStream.newLineAtOffset(25, 750);
                contentStream.setLeading(14.5f);

                String text = summary.getResult() != null ? summary.getResult() : "";
                
                // Pisahkan string berdasarkan newline
                String[] paragraphs = text.split("\n");
                for (String paragraph : paragraphs) {
                    // Coba menghapus karakter yang ga di-support WinAnsiEncoding
                    String safeText = paragraph.replaceAll("[^\\x00-\\x7F]", "");
                    
                    if (!safeText.trim().isEmpty()) {
                        contentStream.showText(safeText.trim());
                    }
                    contentStream.newLine();
                }

                contentStream.endText();
            }
            doc.save(path);
        }
    }
}

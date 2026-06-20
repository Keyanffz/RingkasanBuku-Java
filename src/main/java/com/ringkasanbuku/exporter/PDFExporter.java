package com.ringkasanbuku.exporter;

import com.ringkasanbuku.model.Summary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PDFExporter implements FileExporter {

    private static final float MARGIN = 50;
    private static final float FONT_SIZE = 11;
    private static final float LEADING = 16;

    @Override
    public void export(Summary summary, String path) throws IOException {
        PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            float width  = page.getMediaBox().getWidth() - 2 * MARGIN;
            float yStart = page.getMediaBox().getHeight() - MARGIN;
            float y = yStart;

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            cs.beginText();
            cs.setFont(font, FONT_SIZE);
            cs.newLineAtOffset(MARGIN, y);

            List<String> lines = wrapText(sanitize(summary.getResult()), font, FONT_SIZE, width);

            for (String line : lines) {
                if (y < MARGIN + LEADING) {
                    cs.endText();
                    cs.close();

                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    cs.beginText();
                    cs.setFont(font, FONT_SIZE);
                    y = yStart;
                    cs.newLineAtOffset(MARGIN, y);
                }
                cs.showText(line);
                cs.newLineAtOffset(0, -LEADING);
                y -= LEADING;
            }

            cs.endText();
            cs.close();
            doc.save(path);
        }
    }

    private String sanitize(String text) {
        return text
            .replace('\u2018', '\'').replace('\u2019', '\'')
            .replace('\u201C', '"').replace('\u201D', '"')
            .replace("\u2013", "-").replace("\u2014", "-")
            .replace("\u2026", "...");
    }

    private List<String> wrapText(String text, PDType1Font font, float fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        for (String paragraph : text.split("\n")) {
            if (paragraph.isBlank()) { lines.add(""); continue; }
            StringBuilder current = new StringBuilder();
            for (String word : paragraph.split(" ")) {
                String candidate = current.isEmpty() ? word : current + " " + word;
                float w = font.getStringWidth(candidate) / 1000 * fontSize;
                if (w > maxWidth && !current.isEmpty()) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(candidate);
                }
            }
            lines.add(current.toString());
        }
        return lines;
    }
}
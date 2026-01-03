package com.app.pdfstation.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfMergeServiceTest {

    private PdfMergeService pdfMergeService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        pdfMergeService = new PdfMergeService();
    }

    @Test
    void testMerge() throws Exception {
        // Create two dummy PDF files
        File pdf1 = createDummyPdf("pdf1.pdf", "Content 1");
        File pdf2 = createDummyPdf("pdf2.pdf", "Content 2");

        String outputPath = tempDir.resolve("merged.pdf").toString();

        // Perform merge
        String result = pdfMergeService.merge(List.of(pdf1.getAbsolutePath(), pdf2.getAbsolutePath()), outputPath);

        // Verify
        assertEquals(outputPath, result);
        File resultFile = new File(result);
        assertTrue(resultFile.exists());

        try (PDDocument doc = org.apache.pdfbox.Loader.loadPDF(resultFile)) {
            assertEquals(2, doc.getNumberOfPages());
        }
    }

    private File createDummyPdf(String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);
            try (PDPageContentStream contents = new PDPageContentStream(doc, page)) {
                contents.beginText();
                contents.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contents.newLineAtOffset(100, 700);
                contents.showText(content);
                contents.endText();
            }
            doc.save(file);
        }
        return file;
    }
}

package com.app.pdfstation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfToWordService {

    public void convertPdfToWord(String inputPath, String outputPath) throws IOException {
        log.info("Starting PDF to Word conversion: {}", inputPath);

        try (PDDocument document = Loader.loadPDF(new File(inputPath));
                XWPFDocument wordDocument = new XWPFDocument();
                FileOutputStream out = new FileOutputStream(outputPath)) {

            // Simple text extraction - in a real world scenario, you'd want more complex
            // layout analysis
            // For now, we'll extract text page by page and write it to the Word doc
            PDFTextStripper stripper = new PDFTextStripper();

            // Extract text from the entire document
            String text = stripper.getText(document);

            // Split into lines to preserve some structure
            String[] lines = text.split("\\r?\\n");

            XWPFParagraph paragraph = wordDocument.createParagraph();
            XWPFRun run = paragraph.createRun();

            for (String line : lines) {
                run.setText(line);
                run.addBreak();
            }

            wordDocument.write(out);
            log.info("PDF to Word conversion completed: {}", outputPath);
        } catch (Exception e) {
            log.error("Error converting PDF to Word", e);
            throw new IOException("Failed to convert PDF to Word", e);
        }
    }
}

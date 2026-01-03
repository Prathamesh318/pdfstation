package com.app.pdfstation.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.multipdf.Splitter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class PdfSplitService {

    public List<File> splitByPages(String inputPath, String range, String outputDir) throws IOException {
        List<File> splitFiles = new ArrayList<>();
        File file = new File(inputPath);
        
        try (PDDocument document = Loader.loadPDF(file)) {
            Splitter splitter = new Splitter();
            List<PDDocument> pages = splitter.split(document);
            
            // Parse ranges: "1-3,5,7-9"
            String[] parts = range.split(",");
            for (String part : parts) {
                if (part.contains("-")) {
                    String[] bounds = part.split("-");
                    int start = Integer.parseInt(bounds[0]) - 1; // 0-based
                    int end = Integer.parseInt(bounds[1]) - 1;
                    
                    for (int i = start; i <= end && i < pages.size(); i++) {
                        savePage(pages.get(i), outputDir, i + 1, splitFiles);
                    }
                } else {
                    int pageNum = Integer.parseInt(part.trim()) - 1;
                    if (pageNum >= 0 && pageNum < pages.size()) {
                        savePage(pages.get(pageNum), outputDir, pageNum + 1, splitFiles);
                    }
                }
            }
            
            // Close all split docs
            for (PDDocument doc : pages) {
                doc.close();
            }
        }
        return splitFiles;
    }

    public List<File> splitByInterval(String inputPath, int interval, String outputDir) throws IOException {
        List<File> splitFiles = new ArrayList<>();
        File file = new File(inputPath);

        try (PDDocument document = Loader.loadPDF(file)) {
            Splitter splitter = new Splitter();
            splitter.setSplitAtPage(interval);
            List<PDDocument> parts = splitter.split(document);

            int i = 1;
            for (PDDocument doc : parts) {
                String fileName = "part_" + i + ".pdf";
                File outFile = new File(outputDir, fileName);
                doc.save(outFile);
                splitFiles.add(outFile);
                doc.close();
                i++;
            }
        }
        return splitFiles;
    }

    public List<File> splitAll(String inputPath, String outputDir) throws IOException {
        return splitByInterval(inputPath, 1, outputDir);
    }

    private void savePage(PDDocument doc, String outputDir, int pageNum, List<File> files) throws IOException {
        String fileName = "page_" + pageNum + ".pdf";
        File outFile = new File(outputDir, fileName);
        doc.save(outFile);
        files.add(outFile);
    }

    public void createZipArchive(List<File> files, String zipPath) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            for (File file : files) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    ZipEntry zipEntry = new ZipEntry(file.getName());
                    zos.putNextEntry(zipEntry);
                    
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        zos.write(bytes, 0, length);
                    }
                }
            }
        }
    }
}

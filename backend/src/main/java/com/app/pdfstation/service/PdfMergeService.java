package com.app.pdfstation.service;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Service
public class PdfMergeService {

    public String merge(List<String> inputPaths, String outputPath) throws Exception {

        PDFMergerUtility merger = new PDFMergerUtility();
        merger.setDestinationFileName(outputPath);

        for (String path : inputPaths) {
            merger.addSource(new File(path));
        }

        merger.mergeDocuments(org.apache.pdfbox.io.IOUtils.createMemoryOnlyStreamCache());

        return outputPath;
    }
}

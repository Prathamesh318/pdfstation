package com.app.pdfstation.service;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.image.JPEGFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.DecimalFormat;

@Service
public class PdfCompressionService {
    private  final Logger logger= LoggerFactory.getLogger(PdfCompressionService.class);
    private long safeSize(Path p) {
        try {
            return Files.size(p);
        } catch (IOException e) {
            logger.info("Failed to get file size for {}: {}", p, e.getMessage());
            return -1;
        }
    }
    private String humanReadable(long bytes) {
        if (bytes < 0) return "unknown";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        double val = bytes / Math.pow(1024, exp);
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(val) + " " + unit + "B";
    }
    public String compressPdf(String inputPath, String outputPath, float imageQuality) throws Exception {

        long beforeSize = safeSize(Path.of(inputPath));
        logger.info("Starting PDF compression. inputPath={} size={} ({})", inputPath, beforeSize, humanReadable(beforeSize));
        System.out.println("PDF compressed successfully, new size: " + beforeSize + " bytes");
        try (PDDocument document = Loader.loadPDF(new File(inputPath))) {

            for (PDPage page : document.getPages()) {
                PDResources resources = page.getResources();
                if (resources == null) continue;

                for (COSName name : resources.getXObjectNames()) {
                    if (resources.isImageXObject(name)) {
                        PDImageXObject image = (PDImageXObject) resources.getXObject(name);
                        BufferedImage bufferedImage = image.getImage();

                        // Downscale image to default max width if needed (e.g., 1024px)
                        int maxWidth = 1024;
                        if (bufferedImage.getWidth() > maxWidth) {
                            int newWidth = maxWidth;
                            int newHeight = (int) (((double) bufferedImage.getHeight() / bufferedImage.getWidth()) * newWidth);
                            BufferedImage resized = new BufferedImage(newWidth, newHeight, bufferedImage.getType());
                            java.awt.Graphics2D g = resized.createGraphics();
                            g.drawImage(bufferedImage, 0, 0, newWidth, newHeight, null);
                            g.dispose();
                            bufferedImage = resized;
                        }

                        try (ByteArrayOutputStream compressedImageStream = new ByteArrayOutputStream();
                             ImageOutputStream ios = ImageIO.createImageOutputStream(compressedImageStream)) {

                            ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
                            ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
                            jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                            jpgWriteParam.setCompressionQuality(imageQuality);

                            jpgWriter.setOutput(ios);
                            jpgWriter.write(null, new IIOImage(bufferedImage, null, null), jpgWriteParam);
                            jpgWriter.dispose();

                            PDImageXObject compressedImage =
                                    JPEGFactory.createFromStream(document,
                                            new ByteArrayInputStream(compressedImageStream.toByteArray()));
                            resources.put(name, compressedImage);
                        }
                    }
                }
            }

            document.save(outputPath);
            // Remove document metadata for further compression
            try (PDDocument savedDoc = Loader.loadPDF(new File(outputPath))) {
                savedDoc.getDocumentInformation().getCOSObject().clear();
                savedDoc.getDocumentCatalog().setMetadata(null);
                savedDoc.save(outputPath);
            }
        }
        long afterSize = safeSize(Path.of(outputPath));
        System.out.println("PDF compressed successfully, new size: " + afterSize + " bytes");
        System.out.println("Compression ratio: " +
                (beforeSize > 0 ? String.format("%.2f", (beforeSize - afterSize) * 100.0 / beforeSize) + "%" : "unknown"));
        logger.info("Starting PDF compression. inputPath={} size={} ({})", inputPath, afterSize, humanReadable(afterSize));

        return outputPath;
    }
}

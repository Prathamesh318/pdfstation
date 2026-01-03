package com.app.pdfstation.infrastructure.storage;

import com.app.pdfstation.constants.PdfStationConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${pdfstation.storage.upload-dir}")
    private String BASE_DIR;

    @Value("${pdfstation.storage.merged-output-dir}")
    private String MERGED_OUTPUT_DIR;

    @Value("${pdfstation.storage.compressed-output-dir}")
    private String COMPRESSED_OUTPUT_DIR;

    public String saveFile(UUID jobId, MultipartFile file) throws IOException {

        String jobDir = BASE_DIR + jobId;
        Files.createDirectories(Paths.get(jobDir));

        Path filePath = Paths.get(jobDir, file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public List<String> saveFiles(UUID jobId, MultipartFile[] files) throws IOException {
        List<String> paths = new ArrayList<>();

        Path jobDir = Paths.get(BASE_DIR + jobId);
        Files.createDirectories(jobDir);

        for (MultipartFile file : files) {
            Path filePath = jobDir.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            paths.add(filePath.toString());
        }
        return paths;
    }

    public String generateMergedOutputPath(UUID jobId) throws IOException {
        Path outputDir = Paths.get(MERGED_OUTPUT_DIR);
        Files.createDirectories(outputDir);
        return outputDir.resolve(jobId + PdfStationConstants.MERGED_SUFFIX).toString();
    }

    public String generateOutputPath(UUID jobId) throws IOException {
        Path outputDir = Paths.get(COMPRESSED_OUTPUT_DIR);
        Files.createDirectories(outputDir);

        return outputDir.resolve(jobId + PdfStationConstants.COMPRESSED_SUFFIX).toString();
    }

}

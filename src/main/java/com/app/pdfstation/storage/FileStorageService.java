package com.app.pdfstation.storage;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String BASE_DIR = "uploads/";

    public String saveFile(UUID jobId, MultipartFile file) throws IOException {

        String jobDir = BASE_DIR + jobId;
        Files.createDirectories(Paths.get(jobDir));

        Path filePath = Paths.get(jobDir, file.getOriginalFilename());
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }

    public String generateOutputPath(UUID jobId) throws IOException {
        Path outputDir = Paths.get("outputs", jobId.toString());
        Files.createDirectories(outputDir);

        return outputDir.resolve("compressed.pdf").toString();
    }

}


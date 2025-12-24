package com.app.pdfstation.service;

import com.app.pdfstation.entity.PdfJob;
import com.app.pdfstation.event.PdfJobCreatedEvent;
import com.app.pdfstation.kafka.PdfJobEventProducer;
import com.app.pdfstation.repository.PdfJobRepository;
import com.app.pdfstation.storage.FileStorageService;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfJobService {

    private final PdfJobRepository jobRepository;
    private final FileStorageService storageService;
    private final PdfJobEventProducer eventProducer;

    @Transactional
    public PdfJob createJob(String operation, MultipartFile file) throws IOException {

        PdfJob job = PdfJob.builder()
                .operation(operation)
                .status("CREATED")
                .retryCount(0)
                .build();

        job = jobRepository.save(job);

        String inputPath = storageService.saveFile(job.getId(), file);
        job.setInputPath(inputPath);
        // 🔥 Kafka publish
        eventProducer.publishJobCreatedEvent(
                new PdfJobCreatedEvent(
                        job.getId(),
                        job.getOperation(),
                        job.getInputPath()
                )
        );

        return jobRepository.save(job);
    }
    public UrlResource loadCompressedPdf(UUID jobId) {

        PdfJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        if (!"COMPLETED".equals(job.getStatus())) {
            throw new RuntimeException("PDF not ready yet");
        }

        try {
            Path filePath = Path.of(job.getOutputPath());
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            
            throw new RuntimeException("Failed to load file", e);
        }
    }
}


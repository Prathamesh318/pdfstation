package com.app.pdfstation.service;

import com.app.pdfstation.entity.PdfJob;
import com.app.pdfstation.repository.PdfJobRepository;
import com.app.pdfstation.storage.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class PdfJobService {

    private final PdfJobRepository jobRepository;
    private final FileStorageService storageService;

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

        return jobRepository.save(job);
    }
}


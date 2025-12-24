package com.app.pdfstation.controller;

import com.app.pdfstation.dto.PdfJob.CreateJobResponse;
import com.app.pdfstation.entity.PdfJob;
import com.app.pdfstation.service.PdfJobService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/pdf/jobs")
@RequiredArgsConstructor
public class PdfJobController {

    private final PdfJobService jobService;

    private  final Logger logger= LoggerFactory.getLogger(PdfJobController.class);

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateJobResponse> createJob(
            @RequestParam("operation") String operation,
            @RequestParam("file") MultipartFile file
    ) throws IOException {

        PdfJob job = jobService.createJob(operation, file);

        return ResponseEntity.ok(
                new CreateJobResponse(job.getId(), job.getStatus())
        );
    }

    @GetMapping("/{jobId}/download")
    public ResponseEntity<UrlResource> downloadPdf(@PathVariable UUID jobId) {

        logger.debug("Download request for job " + jobId);
        UrlResource resource = jobService.loadCompressedPdf(jobId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"compressed_" + jobId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}


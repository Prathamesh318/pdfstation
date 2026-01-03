package com.app.pdfstation.api.controller;

import com.app.pdfstation.api.dto.CreateJobResponse;
import com.app.pdfstation.domain.entity.PdfJob;
import com.app.pdfstation.service.PdfJobService;
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
public class PdfJobController{

        private final PdfJobService jobService;

        private final Logger logger = LoggerFactory.getLogger(PdfJobController.class);

        @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<CreateJobResponse> createJob(
                        @RequestParam("operation") String operation,
                        @RequestParam("file") MultipartFile file,
                        @RequestParam(value = "quality", required = false, defaultValue = "50") Integer quality)
                        throws IOException {

                PdfJob job = jobService.createJob(operation, file, quality);

                return ResponseEntity.ok(
                                new CreateJobResponse(job.getId(), job.getStatus()));
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

        @PostMapping("/merge")
        public ResponseEntity<CreateJobResponse> createMergeJob(
                        @RequestParam("files") MultipartFile[] files) throws IOException {

                logger.info("Received merge request with {} files", files.length);
                for (MultipartFile file : files) {
                        logger.info("File: {}, Size: {}", file.getOriginalFilename(), file.getSize());
                }
                PdfJob job = jobService.createMergeJob(files);

                return ResponseEntity.ok(new CreateJobResponse(job.getId(), job.getStatus()));
        }

        @GetMapping("/estimate-size")
        public ResponseEntity<String> estimateSize(
                        @RequestParam("originalSize") long originalSize,
                        @RequestParam("quality") int quality) {
                long estimatedSize = (long) (originalSize * (quality / 100.0) * 0.85); // simple heuristic
                return ResponseEntity.ok(String.valueOf(estimatedSize));
        }
}

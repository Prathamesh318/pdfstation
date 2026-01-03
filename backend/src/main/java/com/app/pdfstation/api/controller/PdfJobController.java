package com.app.pdfstation.api.controller;

import com.app.pdfstation.api.dto.CreateJobResponse;
import com.app.pdfstation.domain.entity.PdfJob;
import com.app.pdfstation.service.PdfJobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "PDF Jobs", description = "PDF processing operations including compression, merging, and downloading")
@RestController
@RequestMapping("/api/pdf/jobs")
@RequiredArgsConstructor
public class PdfJobController {

        private final PdfJobService jobService;

        private final Logger logger = LoggerFactory.getLogger(PdfJobController.class);

        @Operation(summary = "Create PDF job", description = "Create a new PDF processing job (compress or other operations)")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Job created successfully", content = @Content(schema = @Schema(implementation = CreateJobResponse.class))),
                        @ApiResponse(responseCode = "400", description = "Invalid operation or bad request"),
                        @ApiResponse(responseCode = "413", description = "File too large (max 20MB)")
        })
        @PostMapping(path="/compress",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public ResponseEntity<CreateJobResponse> createJob(
                        @Parameter(description = "Operation type (COMPRESS)", required = true) @RequestParam("operation") String operation,
                        @Parameter(description = "PDF file to process (max 20MB)", required = true) @RequestParam("file") MultipartFile file,
                        @Parameter(description = "Compression quality (0-100, default=50)", required = false) @RequestParam(value = "quality", required = false, defaultValue = "50") Integer quality)
                        throws IOException {

                PdfJob job = jobService.createJob(operation, file, quality);

                return ResponseEntity.ok(
                                new CreateJobResponse(job.getId(), job.getStatus()));
        }

        @Operation(summary = "Download compressed PDF", description = "Download the processed/compressed PDF file")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "PDF file", content = @Content(mediaType = "application/pdf")),
                        @ApiResponse(responseCode = "404", description = "Job not found"),
                        @ApiResponse(responseCode = "400", description = "PDF not ready yet")
        })
        @GetMapping(value = "/{jobId}/download", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<UrlResource> downloadPdf(
                        @Parameter(description = "Job ID", required = true) @PathVariable UUID jobId) {

                logger.debug("Download request for job " + jobId);
                UrlResource resource = jobService.loadCompressedPdf(jobId);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"compressed_" + jobId + ".pdf\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(resource);
        }

        @Operation(summary = "Merge PDF files", description = "Merge multiple PDF files into a single PDF")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Merge job created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid files or no files provided"),
                        @ApiResponse(responseCode = "413", description = "Files too large (max 20MB total)")
        })
        @PostMapping("/merge")
        public ResponseEntity<CreateJobResponse> createMergeJob(
                        @Parameter(description = "PDF files to merge", required = true) @RequestParam("files") MultipartFile[] files)
                        throws IOException {

                logger.info("Received merge request with {} files", files.length);
                for (MultipartFile file : files) {
                        logger.info("File: {}, Size: {}", file.getOriginalFilename(), file.getSize());
                }
                PdfJob job = jobService.createMergeJob(files);

                return ResponseEntity.ok(new CreateJobResponse(job.getId(), job.getStatus()));
        }

        @Operation(summary = "Download merged PDF", description = "Download the merged PDF file")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Merged PDF file", content = @Content(mediaType = "application/pdf")),
                        @ApiResponse(responseCode = "404", description = "Job not found"),
                        @ApiResponse(responseCode = "400", description = "PDF not ready yet")
        })
        @GetMapping(value = "/{jobId}/download-merged", produces = MediaType.APPLICATION_PDF_VALUE)
        public ResponseEntity<UrlResource> downloadMergedPdf(
                        @Parameter(description = "Merge job ID", required = true) @PathVariable UUID jobId) {

                logger.debug("Download merged PDF request for job " + jobId);
                UrlResource resource = jobService.loadMergedPdf(jobId);

                return ResponseEntity.ok()
                                .header(HttpHeaders.CONTENT_DISPOSITION,
                                                "attachment; filename=\"merged_" + jobId + ".pdf\"")
                                .contentType(MediaType.APPLICATION_PDF)
                                .body(resource);
        }

        @Operation(summary = "Estimate compressed size", description = "Get estimated file size after compression")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Estimated size in bytes")
        })
        @GetMapping("/estimate-size")
        public ResponseEntity<String> estimateSize(
                        @Parameter(description = "Original file size in bytes", required = true) @RequestParam("originalSize") long originalSize,
                        @Parameter(description = "Compression quality (0-100)", required = true) @RequestParam("quality") int quality) {
                long estimatedSize = (long) (originalSize * (quality / 100.0) * 0.85); // simple heuristic
                return ResponseEntity.ok(String.valueOf(estimatedSize));
        }
}

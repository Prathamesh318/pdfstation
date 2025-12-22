package com.app.pdfstation.controller;

import com.app.pdfstation.dto.PdfJob.CreateJobResponse;
import com.app.pdfstation.entity.PdfJob;
import com.app.pdfstation.service.PdfJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pdf/jobs")
@RequiredArgsConstructor
public class PdfJobController {

    private final PdfJobService jobService;

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
}


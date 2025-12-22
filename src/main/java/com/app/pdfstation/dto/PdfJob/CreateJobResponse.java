package com.app.pdfstation.dto.PdfJob;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateJobResponse {
    private UUID jobId;

    private String status;
}

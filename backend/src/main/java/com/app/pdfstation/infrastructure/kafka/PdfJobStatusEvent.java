package com.app.pdfstation.infrastructure.kafka;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PdfJobStatusEvent {

    private UUID jobId;
    private String status; // PROCESSING, COMPLETED, FAILED
    private Instant updatedAt;

    public PdfJobStatusEvent(UUID jobId, String status) {
        this.jobId = jobId;
        this.status = status;
        this.updatedAt = Instant.now();
    }

    // getters
}

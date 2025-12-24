package com.app.pdfstation.event;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;


@Getter
public class PdfJobCreatedEvent {

    private UUID jobId;
    private String operation;     // COMPRESS, MERGE, SPLIT
    private String inputPath;
    private Instant createdAt;

    public PdfJobCreatedEvent(UUID jobId, String operation, String inputPath) {
        this.jobId = jobId;
        this.operation = operation;
        this.inputPath = inputPath;
        this.createdAt = Instant.now();
    }

    // Default constructor for Jackson
    public PdfJobCreatedEvent() {
    }

    // getters
}

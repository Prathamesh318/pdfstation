package com.app.pdfstation.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public record CreateJobResponse(UUID id, String status) {
}

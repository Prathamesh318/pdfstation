package com.app.pdfstation.domain.repository;

import com.app.pdfstation.domain.entity.PdfJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PdfJobRepository extends JpaRepository<PdfJob, UUID> {
}

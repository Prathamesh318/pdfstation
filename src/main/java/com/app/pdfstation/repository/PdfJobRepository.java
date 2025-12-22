package com.app.pdfstation.repository;

import com.app.pdfstation.entity.PdfJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PdfJobRepository extends JpaRepository<PdfJob, UUID> {
}


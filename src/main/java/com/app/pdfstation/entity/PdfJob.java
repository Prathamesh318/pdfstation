package com.app.pdfstation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pdf_job")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfJob {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String operation; // COMPRESS, MERGE, SPLIT

    @Column(nullable = false)
    private String status; // CREATED

    @Column(nullable = true)
    private String inputPath;

    private String outputPath;

    private int retryCount;

    private String errorMessage;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}


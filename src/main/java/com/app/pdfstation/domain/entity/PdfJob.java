package com.app.pdfstation.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;
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

    @ElementCollection
    private List<String> inputPaths; // 👈 multiple PDFs

    private String outputPath;

    private int retryCount;

    private String errorMessage;

    @Column(name = "compression_quality")
    private Double compressionQuality = 0.5;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int maxRetries = 3;

}

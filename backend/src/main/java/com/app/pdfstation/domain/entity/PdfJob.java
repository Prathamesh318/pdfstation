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

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> inputPaths; // 👈 multiple PDFs

    private String outputPath;

    private int retryCount;

    private String errorMessage;

    @Column(name = "compression_quality")
    private Double compressionQuality = 0.5;

    @Column(name = "split_type")
    private String splitType; // "pages", "interval", "all"

    @Column(name = "split_ranges")
    private String splitRanges; // "1-3,5,7-10"

    @Column(name = "split_interval")
    private Integer splitInterval; // e.g., 5

    // Protection/Encryption fields
    @Column(name = "user_password")
    private String userPassword; // Password to open PDF

    @Column(name = "owner_password")
    private String ownerPassword; // Password to change permissions

    @Column(name = "allow_printing")
    private Boolean allowPrinting;

    @Column(name = "allow_copying")
    private Boolean allowCopying;

    @Column(name = "allow_modification")
    private Boolean allowModification;

    @Column(name = "allow_assembly")
    private Boolean allowAssembly;

    @Column(name = "protection_action")
    private String protectionAction; // "ADD" or "REMOVE"

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    private int maxRetries = 3;

}

package com.app.pdfstation.constants;

/**
 * Central constants file for PDFStation application.
 * Contains only static strings and non-configurable values.
 * Configurable values (paths, topics, etc.) should be in
 * application.properties.
 */
public final class PdfStationConstants {

    // Prevent instantiation
    private PdfStationConstants() {
        throw new UnsupportedOperationException("This is a constants class and cannot be instantiated");
    }

    // ==================== PDF Operations ====================
    public static final String OPERATION_COMPRESS = "COMPRESS";
    public static final String OPERATION_MERGE = "MERGE";
    public static final String OPERATION_SPLIT = "SPLIT"; // Future feature
    public static final String OPERATION_PROTECT = "PROTECT"; // Future feature

    // ==================== Job Statuses ====================
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_PROCESSING = "PROCESSING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    // ==================== File Extensions ====================
    public static final String PDF_EXTENSION = ".pdf";
    public static final String COMPRESSED_SUFFIX = "_compressed.pdf";
    public static final String MERGED_SUFFIX = "_merged.pdf";

    // ==================== Error Messages ====================
    public static final String ERROR_JOB_NOT_FOUND = "Job not found";
    public static final String ERROR_PDF_NOT_READY = "PDF not ready yet";
    public static final String ERROR_FAILED_TO_LOAD = "Failed to load file";
    public static final String ERROR_INVALID_OPERATION = "Invalid operation";
    public static final String ERROR_FILE_TOO_LARGE = "File too large";
    public static final String ERROR_NO_FILES_PROVIDED = "No files provided";

    // ==================== HTTP Headers ====================
    public static final String CONTENT_DISPOSITION = "Content-Disposition";
    public static final String ATTACHMENT_FILENAME_COMPRESSED = "attachment; filename=\"compressed_%s.pdf\"";
    public static final String ATTACHMENT_FILENAME_MERGED = "attachment; filename=\"merged_%s.pdf\"";

    // ==================== API Documentation ====================
    public static final String API_TAG_PDF_JOBS = "PDF Jobs";
    public static final String API_TAG_DESCRIPTION = "PDF processing operations including compression, merging, and downloading";

    // ==================== Default Values (Non-configurable) ====================
    public static final int DEFAULT_COMPRESSION_QUALITY = 50;
    public static final double DEFAULT_COMPRESSION_QUALITY_DECIMAL = 0.5;
    public static final int DEFAULT_MAX_RETRIES = 3;
}

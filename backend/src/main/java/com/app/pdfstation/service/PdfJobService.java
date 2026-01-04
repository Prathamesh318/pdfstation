package com.app.pdfstation.service;

import com.app.pdfstation.constants.PdfStationConstants;
import com.app.pdfstation.domain.entity.PdfJob;
import com.app.pdfstation.infrastructure.kafka.PdfJobCreatedEvent;
import com.app.pdfstation.infrastructure.kafka.PdfJobEventProducer;
import com.app.pdfstation.domain.repository.PdfJobRepository;
import com.app.pdfstation.infrastructure.storage.FileStorageService;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PdfJobService {

    private final PdfJobRepository jobRepository;
    private final FileStorageService storageService;
    private final PdfJobEventProducer eventProducer;

    @Transactional
    public PdfJob createJob(String operation, MultipartFile file, Integer quality) throws IOException {

        PdfJob job = PdfJob.builder()
                .operation(operation)
                .status("CREATED")
                .retryCount(0)
                .compressionQuality(quality != null ? quality / 100.0 : 0.5)
                .build();

        job = jobRepository.save(job);

        String inputPath = storageService.saveFile(job.getId(), file);
        job.setInputPaths(new java.util.ArrayList<>(Arrays.asList(inputPath)));
        List<String> paths = job.getInputPaths();
        // 🔥 Kafka publish
        PdfJob savedJob = jobRepository.save(job);

        // Publish event AFTER transaction commit to avoid race conditions
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventProducer.publishJobCreatedEvent(
                                new PdfJobCreatedEvent(
                                        savedJob.getId(),
                                        savedJob.getOperation(),
                                        paths.get(0)));
                    }
                });

        return savedJob;
    }

    @Transactional
    public PdfJob createMergeJob(MultipartFile[] files) throws IOException {

        PdfJob job = PdfJob.builder()
                .operation("MERGE")
                .status("CREATED")
                .retryCount(0)
                .build();

        job = jobRepository.save(job);

        List<String> inputPaths = storageService.saveFiles(job.getId(), files);
        job.setInputPaths(inputPaths);
        jobRepository.save(job);

        final PdfJob finalJob = job;
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventProducer.publishJobCreatedEvent(
                                new PdfJobCreatedEvent(finalJob.getId(), "MERGE", null));
                    }
                });

        return job;
    }

    public UrlResource loadCompressedPdf(UUID jobId) {

        PdfJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException(PdfStationConstants.ERROR_JOB_NOT_FOUND));

        if (!PdfStationConstants.STATUS_COMPLETED.equals(job.getStatus())) {
            throw new RuntimeException(PdfStationConstants.ERROR_PDF_NOT_READY);
        }

        try {
            Path filePath = Path.of(job.getOutputPath());
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {

            throw new RuntimeException(PdfStationConstants.ERROR_FAILED_TO_LOAD, e);
        }
    }

    public UrlResource loadMergedPdf(UUID jobId) {

        PdfJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException(PdfStationConstants.ERROR_JOB_NOT_FOUND));

        if (!PdfStationConstants.STATUS_COMPLETED.equals(job.getStatus())) {
            throw new RuntimeException(PdfStationConstants.ERROR_PDF_NOT_READY);
        }

        try {
            Path filePath = Path.of(job.getOutputPath());
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(PdfStationConstants.ERROR_FAILED_TO_LOAD, e);
        }
    }

    @Transactional
    public PdfJob createSplitJob(MultipartFile file, String splitType, String splitRanges, Integer splitInterval) throws IOException {
        PdfJob job = new PdfJob();
        job.setOperation(PdfStationConstants.OPERATION_SPLIT);
        job.setStatus(PdfStationConstants.STATUS_CREATED);
        job.setSplitType(splitType);
        job.setSplitRanges(splitRanges);
        job.setSplitInterval(splitInterval);

        job = jobRepository.save(job);

        String inputPath = storageService.saveFile(job.getId(), file);
        job.setInputPaths(new java.util.ArrayList<>(Arrays.asList(inputPath)));
        
        final PdfJob savedJob = jobRepository.save(job);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventProducer.publishJobCreatedEvent(
                                new PdfJobCreatedEvent(savedJob.getId(), PdfStationConstants.OPERATION_SPLIT, null));
                    }
                });

        return savedJob;
    }

    public UrlResource loadSplitPdfs(UUID jobId) throws MalformedURLException {
        PdfJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException(PdfStationConstants.ERROR_JOB_NOT_FOUND));

        if (!PdfStationConstants.STATUS_COMPLETED.equals(job.getStatus())) {
            throw new RuntimeException(PdfStationConstants.ERROR_PDF_NOT_READY);
        }

        try {
            Path filePath = Path.of(job.getOutputPath());
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(PdfStationConstants.ERROR_FAILED_TO_LOAD, e);
        }
    }

    // Added getJob method per user request
    public PdfJob getJob(UUID jobId) {
        return jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException(PdfStationConstants.ERROR_JOB_NOT_FOUND));
    }

    @Transactional
    public PdfJob createProtectJob(MultipartFile file, String userPassword, String ownerPassword,
                                   Boolean allowPrinting, Boolean allowCopying,
                                   Boolean allowModification, Boolean allowAssembly) throws IOException {
        PdfJob job = new PdfJob();
        job.setOperation(PdfStationConstants.OPERATION_PROTECT);
        job.setProtectionAction(PdfStationConstants.PROTECTION_ACTION_ADD);
        job.setStatus(PdfStationConstants.STATUS_CREATED);
        job.setUserPassword(userPassword);
        job.setOwnerPassword(ownerPassword);
        job.setAllowPrinting(allowPrinting != null ? allowPrinting : true);
        job.setAllowCopying(allowCopying != null ? allowCopying : true);
        job.setAllowModification(allowModification != null ? allowModification : true);
        job.setAllowAssembly(allowAssembly != null ? allowAssembly : true);

        job = jobRepository.save(job);

        String inputPath = storageService.saveFile(job.getId(), file);
        job.setInputPaths(new java.util.ArrayList<>(Arrays.asList(inputPath)));
        final PdfJob savedJob = jobRepository.save(job);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventProducer.publishJobCreatedEvent(
                                new PdfJobCreatedEvent(savedJob.getId(), PdfStationConstants.OPERATION_PROTECT, null));
                    }
                });

        return savedJob;
    }

    @Transactional
    public PdfJob createRemoveProtectionJob(MultipartFile file, String password) throws IOException {
        PdfJob job = new PdfJob();
        job.setOperation(PdfStationConstants.OPERATION_PROTECT);
        job.setProtectionAction(PdfStationConstants.PROTECTION_ACTION_REMOVE);
        job.setStatus(PdfStationConstants.STATUS_CREATED);
        job.setUserPassword(password);

        job = jobRepository.save(job);

        String inputPath = storageService.saveFile(job.getId(), file);
        job.setInputPaths(new java.util.ArrayList<>(Arrays.asList(inputPath)));
        final PdfJob savedJob = jobRepository.save(job);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventProducer.publishJobCreatedEvent(
                                new PdfJobCreatedEvent(savedJob.getId(), PdfStationConstants.OPERATION_PROTECT, null));
                    }
                });

        return savedJob;
    }

    public UrlResource loadProtectedPdf(UUID jobId) throws MalformedURLException {
        PdfJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException(PdfStationConstants.ERROR_JOB_NOT_FOUND));

        if (!PdfStationConstants.STATUS_COMPLETED.equals(job.getStatus())) {
            throw new RuntimeException(PdfStationConstants.ERROR_PDF_NOT_READY);
        }

        try {
            Path filePath = Path.of(job.getOutputPath());
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(PdfStationConstants.ERROR_FAILED_TO_LOAD, e);
        }
    }
    @Transactional
    public PdfJob createPdfToWordJob(MultipartFile file) throws IOException {
        PdfJob job = new PdfJob();
        job.setOperation(PdfStationConstants.OPERATION_PDF_TO_WORD);
        job.setStatus(PdfStationConstants.STATUS_CREATED);
        
        job = jobRepository.save(job);

        String inputPath = storageService.saveFile(job.getId(), file);
        job.setInputPaths(new java.util.ArrayList<>(Arrays.asList(inputPath)));
        final PdfJob savedJob = jobRepository.save(job);

        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        eventProducer.publishJobCreatedEvent(
                                new PdfJobCreatedEvent(savedJob.getId(), PdfStationConstants.OPERATION_PDF_TO_WORD, null));
                    }
                });

        return savedJob;
    }

    public UrlResource loadWordDoc(UUID jobId) {
        PdfJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException(PdfStationConstants.ERROR_JOB_NOT_FOUND));

        if (!PdfStationConstants.STATUS_COMPLETED.equals(job.getStatus())) {
            throw new RuntimeException(PdfStationConstants.ERROR_PDF_NOT_READY);
        }

        try {
            Path filePath = Path.of(job.getOutputPath());
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new RuntimeException(PdfStationConstants.ERROR_FAILED_TO_LOAD, e);
        }
    }
}

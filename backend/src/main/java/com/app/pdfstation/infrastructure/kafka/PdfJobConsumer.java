package com.app.pdfstation.infrastructure.kafka;

import com.app.pdfstation.constants.PdfStationConstants;
import com.app.pdfstation.domain.entity.PdfJob;
import com.app.pdfstation.domain.repository.PdfJobRepository;
import com.app.pdfstation.service.PdfCompressionService;
import com.app.pdfstation.service.PdfMergeService;
import com.app.pdfstation.service.PdfSplitService;
import com.app.pdfstation.service.PdfProtectionService;
import com.app.pdfstation.infrastructure.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfJobConsumer {

    private final PdfJobRepository jobRepository;
    private final PdfJobStatusProducer statusProducer;
    private final PdfJobDlqProducer dlqProducer;
    Logger logger = LoggerFactory.getLogger(PdfJobConsumer.class);
    private final PdfCompressionService compressionService;
    private final FileStorageService storageService;
    private final PdfMergeService mergeService;
    private final PdfSplitService splitService;
    private final PdfProtectionService protectionService;
    private final com.app.pdfstation.service.PdfToWordService pdfToWordService;

    @KafkaListener(topics = "pdf-jobs", groupId = "pdf-processor-group")
    public void consume(PdfJobCreatedEvent event) throws Exception {

        PdfJob job = jobRepository.findById(event.getJobId()).orElseThrow();

        try {
            logger.debug("Processing job " + job.getId());
            job.setStatus(PdfStationConstants.STATUS_PROCESSING);
            jobRepository.save(job);

            statusProducer.publishStatus(
                    new PdfJobStatusEvent(job.getId(), PdfStationConstants.STATUS_PROCESSING));

            String outputPath = null;

            if (PdfStationConstants.OPERATION_COMPRESS.equals(job.getOperation())) {
                outputPath = storageService.generateOutputPath(job.getId());
                List<String> inpiutPaths = job.getInputPaths();
                String inputPath = inpiutPaths.get(0);

                // Use job quality or default
                float quality = job.getCompressionQuality() != null
                        ? job.getCompressionQuality().floatValue()
                        : (float) PdfStationConstants.DEFAULT_COMPRESSION_QUALITY_DECIMAL;

                compressionService.compressPdf(inputPath, outputPath, quality);
            }

            if (PdfStationConstants.OPERATION_MERGE.equals(job.getOperation())) {
                outputPath = storageService.generateMergedOutputPath(job.getId());
                mergeService.merge(job.getInputPaths(), outputPath);
                job.setOutputPath(outputPath);
            }

            if (PdfStationConstants.OPERATION_SPLIT.equals(job.getOperation())) {
                String inputPath = job.getInputPaths().get(0);
                String outputDir = storageService.generateSplitOutputDir(job.getId());
                
                List<java.io.File> splitPdfs;
                
                if ("pages".equals(job.getSplitType())) {
                    splitPdfs = splitService.splitByPages(inputPath, job.getSplitRanges(), outputDir);
                } else if ("interval".equals(job.getSplitType())) {
                    if (job.getSplitInterval() == null) {
                        throw new IllegalArgumentException("Split interval is required for interval split type");
                    }
                    splitPdfs = splitService.splitByInterval(inputPath, job.getSplitInterval(), outputDir);
                } else if ("all".equals(job.getSplitType())) {
                    splitPdfs = splitService.splitAll(inputPath, outputDir);
                } else {
                    throw new IllegalArgumentException("Invalid split type: " + job.getSplitType());
                }
                
                String zipPath = outputDir + "/split.zip";
                splitService.createZipArchive(splitPdfs, zipPath);
                outputPath = zipPath;
            } else if (PdfStationConstants.OPERATION_PROTECT.equals(job.getOperation())) {
                String inputPath = job.getInputPaths().get(0);
                outputPath = storageService.generateProtectedOutputPath(job.getId());
                
                if (PdfStationConstants.PROTECTION_ACTION_ADD.equals(job.getProtectionAction())) {
                    PdfProtectionService.PermissionConfig permissions = new PdfProtectionService.PermissionConfig(
                        job.getAllowPrinting(),
                        job.getAllowCopying(),
                        job.getAllowModification(),
                        job.getAllowAssembly()
                    );
                    
                    protectionService.protectPdf(inputPath, outputPath, 
                        job.getUserPassword(), job.getOwnerPassword(), permissions);
                } else if (PdfStationConstants.PROTECTION_ACTION_REMOVE.equals(job.getProtectionAction())) {
                    protectionService.removeProtection(inputPath, outputPath, job.getUserPassword());
                } else {
                    throw new IllegalArgumentException("Invalid protection action: " + job.getProtectionAction());
                }

            } else if (PdfStationConstants.OPERATION_PDF_TO_WORD.equals(job.getOperation())) {
                String inputPath = job.getInputPaths().get(0);
                outputPath = storageService.generateWordOutputPath(job.getId());
                pdfToWordService.convertPdfToWord(inputPath, outputPath);
            }

            job.setOutputPath(outputPath);
            job.setStatus(PdfStationConstants.STATUS_COMPLETED);
            jobRepository.save(job);

            statusProducer.publishStatus(
                    new PdfJobStatusEvent(job.getId(), PdfStationConstants.STATUS_COMPLETED));
            logger.debug("Completed job " + job.getId());

        } catch (Exception e) {
            logger.error("Error processing job " + event.getJobId(), e);

            PdfJob failedJob = jobRepository.findById(event.getJobId()).orElseThrow();
            failedJob.setRetryCount(failedJob.getRetryCount() + 1);

            if (failedJob.getRetryCount() >= PdfStationConstants.DEFAULT_MAX_RETRIES) {
                // Max retries reached, send to DLQ
                failedJob.setStatus(PdfStationConstants.STATUS_FAILED);
                jobRepository.save(failedJob);
                statusProducer.publishStatus(
                        new PdfJobStatusEvent(failedJob.getId(), PdfStationConstants.STATUS_FAILED));
                dlqProducer.sendToDlq(event);
            } else {
                jobRepository.save(failedJob);
                throw e; // retry
            }
        }
    }

}

package com.app.pdfstation.infrastructure.kafka;

import com.app.pdfstation.constants.PdfStationConstants;
import com.app.pdfstation.domain.entity.PdfJob;
import com.app.pdfstation.domain.repository.PdfJobRepository;
import com.app.pdfstation.service.PdfCompressionService;
import com.app.pdfstation.service.PdfMergeService;
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

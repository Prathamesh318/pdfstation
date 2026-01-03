package com.app.pdfstation.infrastructure.kafka;

import com.app.pdfstation.domain.entity.PdfJob;
import com.app.pdfstation.domain.repository.PdfJobRepository;
import com.app.pdfstation.service.PdfCompressionService;
import com.app.pdfstation.service.PdfMergeService;
import com.app.pdfstation.infrastructure.storage.FileStorageService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfJobConsumer {

    private final PdfJobRepository jobRepository;
    private final PdfJobStatusProducer statusProducer;
    private final PdfJobDlqProducer dlqProducer;
    Logger logger = LoggerFactory.getLogger(PdfJobConsumer.class);
    private final PdfCompressionService compressionService;
    private final FileStorageService storageService;
    private final PdfMergeService mergeService;

    @Transactional
    @KafkaListener(topics = "pdf-jobs", groupId = "pdf-processor-group")
    public void consume(PdfJobCreatedEvent event) throws Exception {

        PdfJob job = jobRepository.findById(event.getJobId()).orElseThrow();

        try {
            logger.debug("Processing job " + job.getId());
            job.setStatus("PROCESSING");
            jobRepository.save(job);

            statusProducer.publishStatus(
                    new PdfJobStatusEvent(job.getId(), "PROCESSING"));

            // 🔥 REAL COMPRESSION
            String outputPath = null;

            if ("COMPRESS".equals(job.getOperation())) {
                // existing compression logic
                // 🔥 REAL COMPRESSION
                outputPath = storageService.generateOutputPath(job.getId());
                List<String> inpiutPaths = job.getInputPaths();
                String inputPath = inpiutPaths.get(0);

                // Use job quality or default to 0.5f if null
                float quality = job.getCompressionQuality() != null ? job.getCompressionQuality().floatValue() : 0.5f;

                compressionService.compressPdf(inputPath, outputPath, quality);
            }

            if ("MERGE".equals(job.getOperation())) {
                outputPath = storageService.generateMergedOutputPath(job.getId());
                mergeService.merge(job.getInputPaths(), outputPath);
                job.setOutputPath(outputPath);
            }

            job.setOutputPath(outputPath);
            job.setStatus("COMPLETED");
            jobRepository.save(job);

            statusProducer.publishStatus(
                    new PdfJobStatusEvent(job.getId(), "COMPLETED"));
            logger.debug("Completed job " + job.getId());

        } catch (Exception ex) {
            logger.warn("Error processing job " + job.getId() + ": " + ex.getMessage());
            job.setRetryCount(job.getRetryCount() + 1);

            if (job.getRetryCount() >= job.getMaxRetries()) {

                job.setStatus("FAILED");
                jobRepository.save(job);

                statusProducer.publishStatus(
                        new PdfJobStatusEvent(job.getId(), "FAILED"));

                dlqProducer.sendToDlq(event);

            } else {
                jobRepository.save(job);
                throw ex; // retry
            }
        }
    }

}

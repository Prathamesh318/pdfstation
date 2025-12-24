package com.app.pdfstation.kafka;

import com.app.pdfstation.entity.PdfJob;
import com.app.pdfstation.event.PdfJobCreatedEvent;
import com.app.pdfstation.event.PdfJobStatusEvent;
import com.app.pdfstation.repository.PdfJobRepository;
import com.app.pdfstation.service.PdfCompressionService;
import com.app.pdfstation.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Slf4j
@Component
@RequiredArgsConstructor
public class PdfJobConsumer {

    private final PdfJobRepository jobRepository;
    private final PdfJobStatusProducer statusProducer;
    private final PdfJobDlqProducer dlqProducer;
    Logger logger= LoggerFactory.getLogger(PdfJobConsumer.class);
    private final PdfCompressionService compressionService;
    private final FileStorageService storageService;

    @KafkaListener(topics = "pdf-jobs", groupId = "pdf-processor-group")
    public void consume(PdfJobCreatedEvent event) throws Exception {

        PdfJob job = jobRepository.findById(event.getJobId()).orElseThrow();

        try {
            logger.debug("Processing job " + job.getId());
            job.setStatus("PROCESSING");
            jobRepository.save(job);

            statusProducer.publishStatus(
                    new PdfJobStatusEvent(job.getId(), "PROCESSING")
            );

            // 🔥 REAL COMPRESSION
            String outputPath = storageService.generateOutputPath(job.getId());
            compressionService.compressPdf(job.getInputPath(), outputPath,0.3f);

            job.setOutputPath(outputPath);
            job.setStatus("COMPLETED");
            jobRepository.save(job);

            statusProducer.publishStatus(
                    new PdfJobStatusEvent(job.getId(), "COMPLETED")
            );
            logger.debug("Completed job " + job.getId());

        } catch (Exception ex) {
            logger.warn("Error processing job " + job.getId() + ": " + ex.getMessage());
            job.setRetryCount(job.getRetryCount() + 1);

            if (job.getRetryCount() >= job.getMaxRetries()) {

                job.setStatus("FAILED");
                jobRepository.save(job);

                statusProducer.publishStatus(
                        new PdfJobStatusEvent(job.getId(), "FAILED")
                );

                dlqProducer.sendToDlq(event);

            } else {
                jobRepository.save(job);
                throw ex; // retry
            }
        }
    }

}

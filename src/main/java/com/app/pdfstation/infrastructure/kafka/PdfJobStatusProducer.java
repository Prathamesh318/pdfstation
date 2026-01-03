package com.app.pdfstation.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfJobStatusProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishStatus(PdfJobStatusEvent event) {
        kafkaTemplate.send("pdf-status", event.getJobId().toString(), event);
    }
}

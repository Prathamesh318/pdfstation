package com.app.pdfstation.infrastructure.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PdfJobDlqProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void sendToDlq(PdfJobCreatedEvent event) {
        kafkaTemplate.send("pdf-jobs-dlq", event.getJobId().toString(), event);
    }
}

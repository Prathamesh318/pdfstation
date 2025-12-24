package com.app.pdfstation.kafka;


import com.app.pdfstation.event.PdfJobCreatedEvent;
//import com.app.pdfstation.events.PdfJobCreatedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PdfJobEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PdfJobEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishJobCreatedEvent(PdfJobCreatedEvent event) {
        kafkaTemplate.send("pdf-jobs", event.getJobId().toString(), event);
    }
}


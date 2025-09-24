package com.example.demo;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumer.class);
    private final List<String> receivedMessages = new CopyOnWriteArrayList<>();
    private final StockService stockService;

    public KafkaConsumer(StockService stockService) {
        this.stockService = stockService;
    }

    @KafkaListener(topics = "my-first-topic", groupId = "my-first-group")
    public void onMessageReceived(ConsumerRecord<String, String> record) {
        String message = record.value();
        LOGGER.info("Message reçu : {}", message);

        receivedMessages.add(message);
        handleReceivedMessage(message);
    }

    private void handleReceivedMessage(String message) {
        if (isValidMessage(message)) {
            String[] messageParts = splitMessage(message);
            String product = messageParts[0].trim();
            int quantity = extractQuantity(messageParts[1].trim(), message);

            if (quantity >= 0) {
                updateStockForProduct(product, quantity);
            } else {
                LOGGER.error("Quantité invalide dans le message : {}", message);
            }
        }
    }

    private boolean isValidMessage(String message) {
        boolean isValid = message != null && message.contains(" : ");
        if (!isValid) {
            LOGGER.warn("Message invalide, format attendu : produit : quantité, message : {}", message);
        }
        return isValid;
    }

    private String[] splitMessage(String message) {
        return message.split(" : ");
    }

    private int extractQuantity(String quantityStr, String originalMessage) {
        try {
            return Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            LOGGER.error("Quantité invalide dans le message : {}", originalMessage, e);
            return -1;
        }
    }

    private void updateStockForProduct(String product, int quantity) {
        if (quantity == 0) {
            LOGGER.warn("La quantité pour le produit {} est nulle, aucun changement effectué.", product);
            return;
        }
        stockService.getStock().addStock(product, -quantity);
        LOGGER.info("Stock mis à jour pour {} : {} unités restantes.", product, stockService.getStock().getStock().get(product));
    }
}

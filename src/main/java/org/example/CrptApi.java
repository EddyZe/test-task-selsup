package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CrptApi {

    private final Semaphore semaphore;
    private final Lock lock = new ReentrantLock();
    private final long timeWindowMillis;
    private final int requestLimit;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private long lastResetTime = System.currentTimeMillis();
    private final String URL = "https://ismp.crpt.ru/api/v3/lk/documents/create";

    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestLimit = requestLimit;
        this.timeWindowMillis = timeUnit.toMillis(1);
        this.semaphore = new Semaphore(requestLimit);
    }

    public static void main(String[] args) {
        CrptApi crptApi = new CrptApi(TimeUnit.MINUTES, 5);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> crptApi.createDocument(new Document(), "signature")).start();
        }
    }

    public void createDocument(Document document, String signature) {

        try {
            lock.lock();
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastResetTime > timeWindowMillis) {
                semaphore.release(this.requestLimit);
                lastResetTime = currentTime;
            }

            if (semaphore.tryAcquire()) {

                String requestBody = objectMapper.writeValueAsString(document);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(URL))
                        .header("Content-Type", "application/json")
                        .header("Signature", signature)
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200)
                    System.out.println("Document created!");
                else
                    System.out.printf("Oooops! Something went wrong! Error: %s\n", response.statusCode());

            } else {
                System.out.println("Request limit exceeded. Request blocked.");
            }
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();
        }
    }


    @Getter
    @Setter
    public static class Document {
        @JsonProperty("description")
        private Description description;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String docStatus;
        @JsonProperty("doc_type")
        private String docType;
        @JsonProperty("import_request")
        private boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("production_type")
        private String productionType;
        @JsonProperty("products")
        private List<Product> products;
        @JsonProperty("reg_date")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate regDate;
        @JsonProperty("reg_number")
        private String regNumber;
    }


    @Getter
    @Setter
    public static class Description {
        @JsonProperty("participantInn")
        private String participantInn;
    }


    @Getter
    @Setter
    public static class Product {
        @JsonProperty("certificate_document")
        private String certificateDocument;
        @JsonProperty("certificate_document_date")
        private String certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("tnved_code")
        private String tnvedCode;
        @JsonProperty("uit_code")
        private String uitCode;
        @JsonProperty("uitu_code")
        private String unituCode;
    }
}
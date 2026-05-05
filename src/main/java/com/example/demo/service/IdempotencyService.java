package com.example.demo.service;



import com.example.demo.model.IdempotencyRecord;
import com.example.demo.util.HashUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class IdempotencyService {

    private final ConcurrentHashMap<String, IdempotencyRecord> store = new ConcurrentHashMap<>();

    public ResponseEntity<String> handleRequest(String key, Map<String, Object> body) throws Exception {

        String requestHash = HashUtil.hash(body);

        IdempotencyRecord existing = store.get(key);


        if (existing != null) {


            if (existing.getFuture() != null) {
                IdempotencyRecord result = existing.getFuture().get();

                return ResponseEntity
                        .status(result.getStatusCode())
                        .header("X-Cache-Hit", "true")
                        .body(result.getResponseBody());
            }


            if (!existing.getRequestHash().equals(requestHash)) {
                return ResponseEntity
                        .status(409)
                        .body("Idempotency key already used for a different request body.");
            }


            return ResponseEntity
                    .status(existing.getStatusCode())
                    .header("X-Cache-Hit", "true")
                    .body(existing.getResponseBody());
        }


        CompletableFuture<IdempotencyRecord> future = new CompletableFuture<>();

        IdempotencyRecord record = new IdempotencyRecord();
        record.setRequestHash(requestHash);
        record.setFuture(future);
        record.setCreatedAt(System.currentTimeMillis());

        store.put(key, record);

        try {

            Thread.sleep(2000);

            String response = "Charged " + body.get("amount") + " " + body.get("currency");

            record.setResponseBody(response);
            record.setStatusCode(201);
            record.setFuture(null);

            future.complete(record);

            return ResponseEntity.status(201).body(response);

        } catch (Exception e) {
            future.completeExceptionally(e);
            store.remove(key);
            throw e;
        }
    }

    public ConcurrentHashMap<String, IdempotencyRecord> getStore() {
        return store;
    }
}
package com.example.demo.cleanup;


import com.example.demo.model.IdempotencyRecord;
import com.example.demo.service.IdempotencyService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.Map;

@Component
public class TTLCleanupTask {

    private final IdempotencyService service;

    public TTLCleanupTask(IdempotencyService service) {
        this.service = service;
    }


    @Scheduled(fixedRate = 60000)
    public void cleanup() {

        long now = System.currentTimeMillis();
        long ttl = 5 * 60 * 1000;

        Iterator<Map.Entry<String, IdempotencyRecord>> iterator =
                service.getStore().entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, IdempotencyRecord> entry = iterator.next();

            if (now - entry.getValue().getCreatedAt() > ttl) {
                iterator.remove();
            }
        }
    }
}
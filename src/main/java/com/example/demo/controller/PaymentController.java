package com.example.demo.controller;

import com.example.demo.service.IdempotencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/process-payment")
public class PaymentController {

    @Autowired
    private IdempotencyService service;

    @PostMapping
    public ResponseEntity<String> processPayment(
            @RequestHeader("Idempotency-Key") String key,
            @RequestBody Map<String, Object> body
    ) throws Exception {

        return service.handleRequest(key, body);
    }
}
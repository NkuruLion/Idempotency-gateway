package com.example.demo.model;



import java.util.concurrent.CompletableFuture;

public class IdempotencyRecord {

    private String requestHash;
    private int statusCode;
    private String responseBody;
    private CompletableFuture<IdempotencyRecord> future;
    private long createdAt;



    public String getRequestHash() { return requestHash; }
    public void setRequestHash(String requestHash) { this.requestHash = requestHash; }

    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }

    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }

    public CompletableFuture<IdempotencyRecord> getFuture() { return future; }
    public void setFuture(CompletableFuture<IdempotencyRecord> future) { this.future = future; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}

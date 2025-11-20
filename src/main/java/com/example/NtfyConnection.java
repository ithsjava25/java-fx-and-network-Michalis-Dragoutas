package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {
    CompletableFuture<Boolean> sendWithId(String message, String localId);
    void receive(Consumer<NtfyMessageDto> handler);
}
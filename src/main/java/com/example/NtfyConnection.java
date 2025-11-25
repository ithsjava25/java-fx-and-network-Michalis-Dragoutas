package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {


    CompletableFuture<Boolean> send(String message);

    void receive(Consumer<NtfyMessageDto> handler);


    String getClientId();
}
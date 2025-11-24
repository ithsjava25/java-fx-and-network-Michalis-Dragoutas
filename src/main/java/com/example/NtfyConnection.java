package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {

    // Just send the text. The implementation handles the ID headers.
    CompletableFuture<Boolean> send(String message);

    void receive(Consumer<NtfyMessageDto> handler);

    // New helper so the Model can ask "Is this message mine?"
    String getClientId();
}
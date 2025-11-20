package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {

    boolean sendWithId(String message, String id);

    void receive(Consumer<NtfyMessageDto> messageHandler);
}
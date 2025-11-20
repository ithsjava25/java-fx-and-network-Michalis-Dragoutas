package com.example;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

```
    private HelloModel model;
    private NtfyConnectionSpy spy;

    // Async spy to capture messages
    static class NtfyConnectionSpy implements NtfyConnection {

        String sentMessage;

        @Override
        public CompletableFuture<Boolean> sendWithId(String message, String localId) {
            sentMessage = message;
            return CompletableFuture.completedFuture(true); // Simulate successful send
        }

        @Override
        public void receive(java.util.function.Consumer<NtfyMessageDto> handler) {
            // No-op for tests
        }
    }

    @BeforeEach
    void setUp() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    @Test
    void sendMessageAddsMessageToList() {
        model.sendMessage("Hello World");

        ObservableList<NtfyMessageDto> messages = model.getMessages();
        assertThat(messages).hasSize(1);
        assertThat(messages.get(0).message()).isEqualTo("Hello World");
        assertThat(messages.get(0).topic()).isEqualTo("me");
        assertThat(messages.get(0).id()).startsWith("local-");
    }

    @Test
    void sendMessageCallsSpySend() {
        model.sendMessage("Test Spy");

        // Wait for async send to complete
        CompletableFuture<Boolean> future = spy.sendWithId("Test Spy", "test-id");
        future.join();

        assertThat(spy.sentMessage).isEqualTo("Test Spy");
    }
```

}

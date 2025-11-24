package com.example;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

    private HelloModel model;
    private NtfyConnectionSpy spy;

    // 1. Spy Implementation
    static class NtfyConnectionSpy implements NtfyConnection {
        String lastSentMessage;
        Consumer<NtfyMessageDto> capturedHandler;

        @Override
        public CompletableFuture<Boolean> sendWithId(String message, String localId) {
            this.lastSentMessage = message;
            return CompletableFuture.completedFuture(true);
        }

        @Override
        public void receive(Consumer<NtfyMessageDto> handler) {
            this.capturedHandler = handler;
        }
    }

    //2. JavaFX Setup for Test
    @BeforeAll
    static void initJfx() {
        // Initialize JavaFX toolkit once for Platform.runLater to work
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Toolkit already initialized, ignore
        }
    }

    @BeforeEach
    void setUp() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    //3. The Tests

    @Test
    void sendMessage_CallsSpyAndDoesNotAddLocally() {
        //Send message
        model.sendMessage("Hello World").join(); // Wait for future to complete


        assertThat(spy.lastSentMessage).isEqualTo("Hello World");


        assertThat(model.getMessages()).isEmpty();
    }

    @Test
    void receiveOwnMessage_ShouldSetTopicToMe() throws InterruptedException {

        String myText = "My Secret Message";
        model.sendMessage(myText).join();


        NtfyMessageDto incoming = new NtfyMessageDto("id1", 100L, "message", "mytopic", myText);


        spy.capturedHandler.accept(incoming);


        waitForFxEvents();

        // 4. Verify List
        ObservableList<NtfyMessageDto> messages = model.getMessages();
        assertThat(messages).hasSize(1);

        // CRITICAL CHECK: The model should have recognized the text and changed topic to "me"
        assertThat(messages.get(0).topic()).isEqualTo("me");
        assertThat(messages.get(0).message()).isEqualTo(myText);
    }

    @Test
    void receiveOtherMessage_ShouldKeepOriginalTopic() throws InterruptedException {

        String otherText = "Hello from Stranger";
        NtfyMessageDto incoming = new NtfyMessageDto("id2", 100L, "message", "stranger_topic", otherText);

        spy.capturedHandler.accept(incoming);
        waitForFxEvents();

        ObservableList<NtfyMessageDto> messages = model.getMessages();
        assertThat(messages).hasSize(1);


        assertThat(messages.get(0).topic()).isEqualTo("stranger_topic");
    }


    private void waitForFxEvents() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(latch::countDown);
        if (!latch.await(2, TimeUnit.SECONDS)) {
            throw new RuntimeException("Timeout waiting for FX Thread");
        }
    }
}
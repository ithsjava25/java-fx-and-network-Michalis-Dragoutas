package com.example;

import javafx.collections.ObservableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloModelTest {

    private HelloModel model;
    private NtfyConnectionSpy spy;

    @BeforeEach
    void setup() {
        spy = new NtfyConnectionSpy();
        model = new HelloModel(spy);
    }

    @Test
    void sendMessageAddsLocalMessage() {

        model.sendMessage("Hello World");

        ObservableList<NtfyMessageDto> list = model.getMessages();
        assertThat(list).hasSize(1);
        assertThat(list.get(0).message()).isEqualTo("Hello World");
        assertThat(list.get(0).topic()).isEqualTo("me");
    }

    @Test
    void sendMessageCallsSpy() {

        model.sendMessage("Test123");

        assertThat(spy.lastMessage).isEqualTo("Test123");
        assertThat(spy.lastId).isNotNull();
    }
}
package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public CompletableFuture<Boolean> sendWithId(String message, String localId) {

        HttpRequest request = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .header("X-Message-ID", localId)   // include ID
                .build();

        return http.sendAsync(request, HttpResponse.BodyHandlers.discarding())
                .thenApply(resp -> true)
                .exceptionally(ex -> {
                    System.out.println("Error sending message: " + ex.getMessage());
                    return false;
                });
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> handler) {

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(hostName + "/mytopic/json"))
                .timeout(Duration.ofSeconds(30))
                .build();

        http.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> response.body()
                        .map(line -> {
                            try {
                                return mapper.readValue(line, NtfyMessageDto.class);
                            } catch (Exception e) {
                                System.err.println("Failed to parse message: " + e.getMessage());
                                return null;
                            }
                        })
                        .filter(msg -> msg != null && "message".equals(msg.event()))
                        .forEach(handler)
                )
                .exceptionally(ex -> {
                    System.err.println("Error receiving messages: " + ex.getMessage());
                    return null;
                });
    }
}
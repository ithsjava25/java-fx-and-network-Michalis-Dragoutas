package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionImpl implements NtfyConnection {

    private final HttpClient http = HttpClient.newHttpClient();
    private final String hostName;
    private final ObjectMapper mapper = new ObjectMapper();


    private final String clientId = UUID.randomUUID().toString();

    public NtfyConnectionImpl() {
        Dotenv dotenv = Dotenv.load();
        hostName = Objects.requireNonNull(dotenv.get("HOST_NAME"));
    }

    public NtfyConnectionImpl(String hostName) {
        this.hostName = hostName;
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public CompletableFuture<Boolean> send(String message) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(message))
                .uri(URI.create(hostName + "/mytopic"))
                .timeout(Duration.ofSeconds(30));


        builder.header("Title", clientId);

        return http.sendAsync(builder.build(), HttpResponse.BodyHandlers.discarding())
                .thenApply(resp -> resp.statusCode() >= 200 && resp.statusCode() < 300)
                .exceptionally(ex -> {
                    System.err.println("Error sending message: " + ex.getMessage());
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
                                return null;
                            }
                        })
                        .filter(msg -> msg != null && "message".equals(msg.event()))
                        .forEach(handler))
                .exceptionally(ex -> {
                    System.err.println("Error receiving messages: " + ex.getMessage());
                    return null;
                });
    }
}
package ru.netology;

import java.io.*;
import java.net.ServerSocket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 9999;
    private static final int THREAD_POOL_SIZE = 64;
    private static final List<String> VALID_PATHS = List.of(
            "/index.html", "/spring.svg", "/spring.png", "/resources.html",
            "/styles.css", "/app.js", "/links.html", "/forms.html",
            "/classic.html", "/events.html", "/events.js"
    );

    private final ExecutorService threadPool;
    private volatile boolean isRunning;

    public Server() {
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        this.isRunning = false;
    }

    public void start() {
        isRunning = true;

        try (final var serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);

            while (isRunning && !Thread.currentThread().isInterrupted()) {
                try {
                    final var socket = serverSocket.accept();
                    threadPool.submit(() -> handleConnection(socket));
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to start server: " + e.getMessage());
        } finally {
            stop();
        }
    }

    private void handleConnection(java.net.Socket socket) {
        try (
                socket;
                final var in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                final var out = new BufferedOutputStream(socket.getOutputStream());
        ) {
            processRequest(in, out);
        } catch (IOException e) {
            System.err.println("Error handling connection: " + e.getMessage());
        }
    }

    private void processRequest(BufferedReader in, BufferedOutputStream out) throws IOException {
        final var requestLine = in.readLine();
        if (requestLine == null) {
            return;
        }

        final var parts = requestLine.split(" ");
        if (parts.length != 3) {
            return;
        }

        final var path = parts[1];
        if (!VALID_PATHS.contains(path)) {
            sendNotFoundResponse(out);
            return;
        }

        serveFile(path, out);
    }

    private void serveFile(String path, BufferedOutputStream out) throws IOException {
        final var filePath = Path.of(".", "public", path);

        if (!Files.exists(filePath)) {
            sendNotFoundResponse(out);
            return;
        }

        final var mimeType = Files.probeContentType(filePath);

        if ("/classic.html".equals(path)) {
            serveClassicHtml(filePath, mimeType, out);
        } else {
            serveStaticFile(filePath, mimeType, out);
        }
    }

    private void serveClassicHtml(Path filePath, String mimeType, BufferedOutputStream out) throws IOException {
        final var template = Files.readString(filePath);
        final var content = template.replace("{time}", LocalDateTime.now().toString()).getBytes();

        final var headers = buildHeaders(200, "OK", mimeType, content.length);
        out.write(headers);
        out.write(content);
        out.flush();
    }

    private void serveStaticFile(Path filePath, String mimeType, BufferedOutputStream out) throws IOException {
        final var length = Files.size(filePath);
        final var headers = buildHeaders(200, "OK", mimeType, length);

        out.write(headers);
        Files.copy(filePath, out);
        out.flush();
    }

    private void sendNotFoundResponse(BufferedOutputStream out) throws IOException {
        final var headers = buildHeaders(404, "Not Found", "text/plain", 0);
        out.write(headers);
        out.flush();
    }

    private byte[] buildHeaders(int statusCode, String statusText, String contentType, long contentLength) {
        return String.format(
                "HTTP/1.1 %d %s\r\n" +
                        "Content-Type: %s\r\n" +
                        "Content-Length: %d\r\n" +
                        "Connection: close\r\n" +
                        "\r\n",
                statusCode, statusText, contentType, contentLength
        ).getBytes();
    }

    public void stop() {
        isRunning = false;
        threadPool.shutdown();
        System.out.println("Server stopped");
    }

    public boolean isRunning() {
        return isRunning;
    }
}
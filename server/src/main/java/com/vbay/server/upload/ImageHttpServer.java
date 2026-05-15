package com.vbay.server.upload;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.vbay.shared.Utils.LoggingUtils;

public class ImageHttpServer {
    private static final Logger LOGGER = LoggingUtils.getLogger(ImageHttpServer.class);
    private static final int DEFAULT_PORT = 8080;
    private static final String URL_PREFIX = "/uploads/products/";

    private final int port;
    private final Path uploadDirectory;
    private HttpServer server;

    public ImageHttpServer() {
        this(DEFAULT_PORT, Paths.get("uploads", "products"));
    }

    public ImageHttpServer(int port, Path uploadDirectory) {
        this.port = port;
        this.uploadDirectory = uploadDirectory.normalize().toAbsolutePath();
    }

    public void start() throws IOException {
        if (server != null) {
            return;
        }

        Files.createDirectories(uploadDirectory);
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext(URL_PREFIX, this::handleImageRequest);
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();
        LOGGER.info(() -> "Image HTTP server started at http://localhost:" + port + URL_PREFIX);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    private void handleImageRequest(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        Path imagePath = resolveImagePath(exchange);
        if (imagePath == null || !Files.isRegularFile(imagePath)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        exchange.getResponseHeaders().set("Content-Type", contentTypeOf(imagePath));
        exchange.getResponseHeaders().set("Cache-Control", "public, max-age=86400");
        byte[] bytes = Files.readAllBytes(imagePath);
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private Path resolveImagePath(HttpExchange exchange) {
        String rawPath = exchange.getRequestURI().getPath();
        String relativePath = rawPath.substring(URL_PREFIX.length());
        String decodedPath = URLDecoder.decode(relativePath, StandardCharsets.UTF_8);
        Path resolvedPath = uploadDirectory.resolve(decodedPath).normalize().toAbsolutePath();
        if (!resolvedPath.startsWith(uploadDirectory)) {
            return null;
        }
        return resolvedPath;
    }

    private String contentTypeOf(Path imagePath) {
        String fileName = imagePath.getFileName().toString().toLowerCase(Locale.US);
        if (fileName.endsWith(".png")) {
            return "image/png";
        }
        if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (fileName.endsWith(".gif")) {
            return "image/gif";
        }
        if (fileName.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }
}

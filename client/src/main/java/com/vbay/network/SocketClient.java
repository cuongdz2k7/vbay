package com.vbay.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.Utils.LoggingUtils;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;

public class SocketClient {
    private static final Logger LOGGER = LoggingUtils.getLogger(SocketClient.class);
    private static SocketClient Client;

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread threadlistener;

    private final Map<String, BlockingQueue<Respond<?>>> pendingResponse = new ConcurrentHashMap<>();

    private SocketClient() {
    }

    public static synchronized SocketClient getClient() {
        if (Client == null) {
            Client = new SocketClient();
        }
        return Client;
    }

    public synchronized boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public synchronized void connect(String host, int port) throws IOException {
        if (isConnected()) {
            LOGGER.info("Socket client is already connected.");
            return;
        }

        try {
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            LOGGER.info(() -> "Connected to server at " + host + ":" + port);
            startListening();
        } catch (ConnectException exception) {
            socket = null;
            in = null;
            out = null;
            throw new ConnectException("Server is not running, check host and port");
        }
    }

    public Respond<?> sendMessage(Request<?> message) throws IOException {
        if (!isConnected()) {
            throw new IOException("Not connected to server");
        }

        BlockingQueue<Respond<?>> queue = new ArrayBlockingQueue<>(1);
        pendingResponse.put(message.getRequestId(), queue);

        String jsonMessage = JsonUtils.toJson(message);
        if (jsonMessage == null) {
            pendingResponse.remove(message.getRequestId());
            throw new IOException("Failed to serialize request to JSON");
        }
        out.println(jsonMessage);
        if (out.checkError()) {
            pendingResponse.remove(message.getRequestId());
            throw new IOException("Failed to send request to server");
        }

        try {
            Respond<?> response = queue.poll(5, TimeUnit.SECONDS);
            if (response == null) {
                pendingResponse.remove(message.getRequestId());
                throw new IOException("Timed out waiting for server response");
            }
            return response;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            pendingResponse.remove(message.getRequestId());
            throw new IOException("Interrupted while waiting for server response", exception);
        }
    }

    public synchronized void disconnect() throws IOException {
        if (out != null) {
            out.println("exit");
            out.flush();
            out.close();
        }
        if (in != null) {
            in.close();
        }
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (threadlistener != null) {
            threadlistener.interrupt();
        }
        pendingResponse.clear();
        in = null;
        out = null;
        socket = null;
        threadlistener = null;
        LOGGER.info("Disconnected from the server.");
    }


    public void startListening() {
        if (threadlistener != null && threadlistener.isAlive()) {
            return;
        }

        threadlistener = new Thread(() -> {
            try {
                String line;
                while (socket != null && !socket.isClosed() && (line = in.readLine()) != null) {
                    Respond<?> respond = JsonUtils.fromJson(line, Respond.class);
                    if (respond == null) {
                        LOGGER.warning("Invalid response: " + line);
                        continue;
                    }

                    BlockingQueue<Respond<?>> queue = pendingResponse.remove(respond.getRequestId());
                    if (queue != null) {
                        queue.offer(respond);
                    } else {
                        LOGGER.warning(() -> "No pending request for requestId: " + respond.getRequestId());
                    }
                }
            } catch (IOException exception) {
                if (socket != null && !socket.isClosed()) {
                    LOGGER.log(Level.WARNING, "Socket listener has stopped.", exception);
                }
            }
        }, "socket-client-listener");

        threadlistener.setDaemon(true);
        threadlistener.start();
    }


}

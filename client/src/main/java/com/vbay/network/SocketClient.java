package com.vbay.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class SocketClient {
    // Keep one shared client instance so the app reuses the same socket connection.
    private static SocketClient Client = new SocketClient();

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;


    private SocketClient() {
    }

    public static SocketClient getClient() {
        if (Client.socket == null) {
            Client = new SocketClient();
        }
        return Client;
    }

    public synchronized void connect(String host, int port) throws IOException {
        if (socket != null) {
            return;
        }

        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
        System.out.println("Connected to server at " + host + ":" + port);
    }



    public synchronized void disconnect() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null && !socket.isClosed()) socket.close();

        in = null;
        out = null;
        socket = null;
    }
}

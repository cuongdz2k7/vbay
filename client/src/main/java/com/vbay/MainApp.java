package com.vbay;

import java.io.IOException;

import com.vbay.network.SocketClient;

public class MainApp {
    public static void main(String[] args) {
        SocketClient client = SocketClient.getClient();
        try {
            System.out.println("Connecting to VBay server...");
            client.connect("localhost", 3618);
            client.sendMessage("Hello, server!");
            System.out.println("Connected to VBay server.");
        } catch (IOException e) {
            System.err.println("Could not connect to server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("Error while disconnecting: " + e.getMessage());
            }
        }
    }
}

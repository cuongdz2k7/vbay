package com.vbay;

import java.io.IOException;

import com.vbay.network.SocketClient;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;

public class MainApp {
    public static void main(String[] args) {
        SocketClient client = SocketClient.getClient();
        try {
            System.out.println("Connecting to VBay server...");
            client.connect("localhost", 3618);
            Request<String> request = new Request<>(RequestType.VERIFY, "Hello, server!");
            client.sendMessage(request);    
            System.out.println("Connected to VBay server.");
        }catch (IOException e) {
            System.err.println("Could not connect to server");
            e.printStackTrace();
        }
        finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("Error while disconnecting: " + e.getMessage());
            }
        }
    }
}

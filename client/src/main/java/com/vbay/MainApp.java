package com.vbay;

import java.io.IOException;

import com.vbay.network.SocketClient;
import com.vbay.shared.dto.RegisterRequest;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.ui.JavaFXApplication;

public class MainApp {

    /**
     * Set up JavaFX Application and enable window rendering.
     */
    public static void initUserInterface(String[] args) {
        JavaFXApplication.launchApplication(args);
    }

    public static void initSocketClient() {
        SocketClient client = SocketClient.getClient();
        try {
            System.out.println("Connecting to VBay server...");
            client.connect("localhost", 3618);
            System.out.println("Connected to VBay server.");
            Request<RegisterRequest> request = new Request<> (
                                    RequestType.REGISTER,
                                    new RegisterRequest("cuongdz2k7", "cuongscp049@gmail.com",
                                                    "cuonglc123".toCharArray(), "0123456789"));
            
            var response = client.sendMessage(request);
            System.out.println(response.getMessage());
        } catch (IOException e) {
            System.err.println("Could not connect to server");
            e.printStackTrace();
        } finally {
            try {
                client.disconnect();
            } catch (IOException e) {
                System.err.println("Error while disconnecting: " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        initSocketClient();
        //initUserInterface(args);
    }

}

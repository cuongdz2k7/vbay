package com.vbay;

import java.io.IOException;
import java.util.List;

import com.vbay.network.SocketClient;
import com.vbay.shared.dto.auctionDTO.CreateAuctionRequest;
import com.vbay.shared.dto.authDTO.LoginRequest;
import com.vbay.shared.dto.authDTO.RegisterRequest;
import com.vbay.shared.dto.productDTO.CreateProductRequest;
import com.vbay.shared.dto.productDTO.ProductImageDTO;
import com.vbay.shared.enums.RequestType;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;
/*
public class MainApp extends Application {

    public static void initSocketClient() {
        SocketClient client = SocketClient.getClient();

        try {
            System.out.println("Connecting to VBay server...");
            client.connect("localhost", 3618);

            Request<String> request = new Request<>(RequestType.VERIFY, "Hello, server!");
            Respond<?> response = client.sendMessage(request);

            if (response != null && response.isStatus()) {
                System.out.println("Connected to VBay server.");
            } else if (response != null) {
                System.err.println("Server verification failed: " + response.getMessage());
            } else {
                System.err.println("Server verification failed: empty response");
            }
        } catch (IOException exception) {
            System.err.println("Could not connect to server");
            exception.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        initSocketClient();
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        SceneManager.setStage(primaryStage);
        primaryStage.setScene(SceneManager.createStyledScene("/jfx/scene/login.fxml"));
        primaryStage.setTitle("VBay");
        primaryStage.setMinWidth(430);
        primaryStage.setMinHeight(720);
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        System.out.println("VBAY shut down ");
        try {
            SocketClient.getClient().disconnect();
        } catch (IOException exception) {
            System.err.println("Error while disconnecting: " + exception.getMessage());
        }
        super.stop();
    }

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("App Shutting Down - Emergency shutdown detected");
            try {
                SocketClient client = SocketClient.getClient();
                if (client.isConnected()) {
                    client.disconnect();
                }
            } catch (Exception e) {
                System.err.println("Error during emergency shutdown: " + e.getMessage());
            }
        }));

        launch(args);
    }
}
 */

public class MainApp {

    public static void initSocketClient() {
        SocketClient client = SocketClient.getClient();

        try {
            System.out.println("Connecting to VBay server...");
            client.connect("localhost", 3618);

            Request<String> request = new Request<>(RequestType.VERIFY, "Hello, server!");
            Respond<?> response = client.sendMessage(request);

            if (response != null && response.isStatus()) {
                System.out.println("Connected to VBay server.");
                RegisterRequest registerRequest = new RegisterRequest("testuser", "testuser@gmail.com", "Password123", "1234");
                Request<RegisterRequest> registerReq = new Request<>(RequestType.REGISTER, registerRequest);
                Respond<?> registerResponse = client.sendMessage(registerReq);
                if (registerResponse != null && registerResponse.isStatus()) {
                    System.out.println("Registration successful: " + registerResponse.getMessage());
                } else if (registerResponse != null) {
                    System.err.println("Registration failed: " + registerResponse.getMessage());
                } else {
                    System.err.println("Registration failed: empty response");
                }
                LoginRequest loginRequest = new LoginRequest("testuser", "testuser@gmail.com", "Password123", "1234");
                
                Request<LoginRequest> loginReq = new Request<>(RequestType.LOGIN, loginRequest);
                Respond<?> loginResponse = client.sendMessage(loginReq);

                ProductImageDTO imageDTO = new ProductImageDTO("https://example.com/image1.jpg", true);
                ProductImageDTO imageDTO2 = new ProductImageDTO("https://example.com/image2.jpg", false);
                List<ProductImageDTO> images = List.of(imageDTO, imageDTO2);
                CreateProductRequest productRequest = new CreateProductRequest("Test Product", images, "This is a test product", "NEW", 1);
                CreateAuctionRequest auctionRequest = new CreateAuctionRequest(productRequest, "Test Auction", "This is a test auction", new java.math.BigDecimal("100.00"), new java.math.BigDecimal("200.00"), new java.math.BigDecimal("150.00"), new java.math.BigDecimal("10.00"), java.time.LocalDateTime.now().plusHours(1), java.time.LocalDateTime.now().plusHours(2));
                Request<CreateAuctionRequest> auctionCreationRequest = new Request<>(RequestType.CREATE_AUCTION, auctionRequest);
                Respond<?> auctionCreationResponse = client.sendMessage(auctionCreationRequest);
                
                if (auctionCreationResponse != null && auctionCreationResponse.isStatus()) {
                    System.out.println("Auction creation successful: " + auctionCreationResponse.getMessage());
                } else if (auctionCreationResponse != null) {
                    System.err.println("Auction creation failed: " + auctionCreationResponse.getMessage());
                } else {
                    System.err.println("Auction creation failed: empty response");
                }
            } else if (response != null) {
                System.err.println("Server verification failed: " + response.getMessage());
            } else {
                System.err.println("Server verification failed: empty response");
            }
        } catch (IOException exception) {
            System.err.println("Could not connect to server");
            exception.printStackTrace();
        }
    }

    public static void test() {
        initSocketClient();
    }
    
    public static void main(String[] args) {
        test();
    }
}

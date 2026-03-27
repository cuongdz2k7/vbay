package com.vbay.server.Network_connection;
//Import
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

// Listening
public class ClientHandler implements Runnable {
    private final Socket socket; 

    //Constructor
    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " +socket.getInetAddress().getHostAddress());
        try (
            Socket clientSocket = socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // client gui request
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true) // client nhan response
        ) {
            String line;
            while ((line = in.readLine()) != null) {
                if (line.trim().isEmpty()){
                    System.out.println("Empty request");
                    continue;
                }
                if ("exit".equalsIgnoreCase(line)){
                    out.println("Exit current socket");
                    break;
                }

                System.out.println("Received line: " + line);

                out.println("Echo: " + line);

                
            }
        } catch (IOException exception) {
            System.err.println("Client connection error: " + exception.getMessage());
        }
        finally{
            System.out.println("Client disconnected: " +socket.getInetAddress().getHostAddress());
        }
    }
}

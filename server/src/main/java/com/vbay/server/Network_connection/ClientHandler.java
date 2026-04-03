package com.vbay.server.Network_connection;
//Import
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.vbay.server.distributor.RequestDistributor;
import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.protocol.Respond;

// Listening
public class ClientHandler implements Runnable {
    private final Socket socket; 
    private final RequestDistributor Distributor;
    
    //Constructor
<<<<<<< HEAD
<<<<<<< HEAD
    public ClientHandler(Socket socket) { // tham so truyen vao la socket tu client
=======
    public ClientHandler(Socket socket, RequestDistributor Distributor) {
>>>>>>> f58302a8af90f2613b97ce6517053645eeb0abd9
=======
    public ClientHandler(Socket socket, RequestDistributor Distributor) {
>>>>>>> 78d200fa82fbf250c23665dc34bcf25218197fb0
        this.socket = socket;
        this.Distributor = Distributor;
    }

    @Override
    public void run() {
        System.out.println("Client connected: " + socket.getInetAddress().getHostAddress());
        try (
            Socket Serversocket = socket;
            BufferedReader in = new BufferedReader(new InputStreamReader(Serversocket.getInputStream())); // client gui request
            PrintWriter out = new PrintWriter(Serversocket.getOutputStream(), true) // client nhan response
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
                Respond<?> response = Distributor.dispatch(line);
                String jsonResponse = JsonUtils.toJson(response);
                out.println(jsonResponse);
            }
        } catch (IOException exception) {
            System.err.println("Client connection error: " + exception.getMessage());
        }
        finally{
            System.out.println("Client disconnected: " +socket.getInetAddress().getHostAddress());
        }
    }
}

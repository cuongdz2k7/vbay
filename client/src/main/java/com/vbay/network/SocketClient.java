package com.vbay.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import com.vbay.shared.Utils.JsonUtils;
import com.vbay.shared.protocol.Request;
import com.vbay.shared.protocol.Respond;

import javafx.application.Platform;

public class SocketClient {
    // Keep one shared client instance so the app reuses the same socket connection.
    private static SocketClient Client;
    private Socket socket;
    // 1 socketclient --> 1 socket ( request response lan luot duoc thuc thi )
    private BufferedReader in;
    private PrintWriter out;
    private Thread threadlistener; // tạo 1 listener ( nghe liên tục)
    private SocketClient() {
    } // dam bao chi có 1 socket duy nhat trong 1 chuong trinh dang chay --> private 
    
    // Dictionary(RequestId, Response tương ứng với id đó)
    private final Map<String, BlockingQueue<Respond<?>>> pendingResponse = new ConcurrentHashMap<>();

    //get client ( duy nhất)
    public static SocketClient getClient() {
        if (Client == null) {
            Client = new SocketClient();
        }
        return Client;
    }

    // connect to server
    public void connect(String host, int port) throws IOException {
        if (socket != null) {
            System.out.println("Already connected");
            return;
        }
        try{
            socket = new Socket(host, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Connected to server at " + host + ":" + port);
            //startListening();
        }
        catch (ConnectException ce){
            System.out.println("Server is not running , checking _host_ and _port_");
            Platform.exit();
        }
    }


    
    public Respond<?> sendMessage (Request<?> message) throws IOException { // send Request to Server --> receive Response
        // Respond<?> --> giúp đa dạng message trả về ( Tuỳ thuộc trường hợp nó ở dạng nào)
        if (socket == null || socket.isClosed()) {
            throw new IOException("Not connected to server");
        }
        String jsonMessage = JsonUtils.toJson(message);
        out.println(jsonMessage);
        String jsonResponse = in.readLine();
        return JsonUtils.fromJson(jsonResponse, Respond.class);
    }    




    // đóng kết nối     
    public synchronized void disconnect() throws IOException {
        if (in != null) in.close();
        if (out != null) out.close();
        if (socket != null && !socket.isClosed()) socket.close();
        if ( threadlistener != null) threadlistener.interrupt();
        in = null;
        out = null;
        socket = null;
        threadlistener = null;
    }
/*
    // Lang nghe server ( lien tuc )
    public void startListening(){
        if (threadlistener != null && threadlistener.isAlive()){
            return;
        
        } // KHONG tao nhieu thread nghe cung luc , neu da ton tai listeningThread thi thoat luong
        // neu chua co thi tao 1 listeningthead 
        threadlistener = new Thread(() -> {
                try { 
                    String line;
                    while (socket != null && !socket.isClosed() && (line = in.readLine()) != null) {
                        Respond<?> respond =  JsonUtils.fromJson(line, Respond.class);

                        if ( respond == null){
                            System.out.println("Invalid response: "  + line);
                            continue;
                        }
                        BlockingQueue<Respond<?>> queue = pendingResponse.remove(respond.getRequestId());
                        if (queue != null){
                            queue.offer(respond);
                        }
                        else { 
                            System.out.println("No pending request for requestId: " + respond.getRequestId());
                        }
                    }
                } catch (IOException IOE){
                    System.out.println("Listener has stopped: " +IOE.getMessage());
                }
        });
    
    threadlistener.setDaemon(true);
    threadlistener.start();
    }

    */
}

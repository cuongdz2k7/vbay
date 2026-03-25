package com.vbay.server;
import com.vbay.server.handler.ClientHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class ServerApplication {
    private static final int PORT = 3618;
    public static void main(String[] args) {

        //Shut down hook ( Truong trinh chuan bi tat)
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server's on the edge of offline, Saving current data into database");
            //RealtimeDatabase.saveAll();
        }));

        try(ServerSocket serverSocket = new ServerSocket(PORT)){ 
            //ServerSocker : dung o phia server -> mo cong + ket noi voi client ( cua chinh cua server)
            System.out.println("Port: " + PORT);
            // DatabaseInitilizer.init() : giao tiep voi database

            while(true){
                Socket socket = serverSocket.accept();
                // socket = dai dien ket noi giua client + server
                System.out.println("Client connected");
                Thread new_Thread = new Thread(new ClientHandler(socket));
                // Tao Thread moi , Thread(<T extend Runnable>) giao cho thread moi phan cong viec la T
                new_Thread.start();

            }
        }
        catch (IOException exception) {
            System.err.println("Server failed to start : " + exception.getMessage());
            exception.printStackTrace();
        }
        //Bao loi I/O -> hien day du thong tin loi
    }
}

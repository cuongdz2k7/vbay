package com.vbay.server;

import java.net.ServerSocket; // tao server + lang nghe ket noi tu client
import java.net.Socket;
import java.sql.DatabaseMetaData;

public class ServerApplication {
    
    public static void main(String[] args) {
        public static final int PORT = 3618;

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


    }
}

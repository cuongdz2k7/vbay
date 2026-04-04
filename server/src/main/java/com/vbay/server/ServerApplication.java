package com.vbay.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.vbay.server.Network_connection.ClientHandler;
import com.vbay.server.databaseManager.DatabaseInitializer;
import com.vbay.server.distributor.RequestDistributor;
import com.vbay.server.repository.JdbcUserRepository;
import com.vbay.server.repository.UserRepository;
import com.vbay.server.security.Argon2PasswordHasher;
import com.vbay.server.security.PasswordHasher;
import com.vbay.server.service.AuthService;

    public class ServerApplication {
        private static final int PORT = 3618;
        public static void main(String[] args) {
            
            
            //Shut down hook ( Truong trinh chuan bi tat)
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Server's on the edge of offline, Saving current data into database");
                //RealtimeDatabase.saveAll();
            }));
            
            try(ServerSocket serverSocket = new ServerSocket(PORT)){ 
                DatabaseInitializer.init();

                ///server là chỗ khởi tạo tất cả các Class cần dùng
                UserRepository userRepository = new JdbcUserRepository();
                PasswordHasher passwordHasher = new Argon2PasswordHasher();///sửa static
                AuthService authService = new AuthService(userRepository, passwordHasher);
                RequestDistributor distributor = new RequestDistributor(authService);
                

                //ServerSocker : dung o phia server -> mo cong + ket noi voi client ( cua chinh cua server)
                System.out.println("Port: " + PORT);
                System.out.println("waiting for clients...");
                while(true){
                    Socket socket = serverSocket.accept();
                    // socket = dai dien ket noi giua client + server
                    System.out.println("Client connected");
                    Thread new_Thread = new Thread(new ClientHandler(socket, distributor));
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

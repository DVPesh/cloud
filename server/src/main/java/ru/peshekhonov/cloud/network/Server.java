package ru.peshekhonov.cloud.network;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {

    public static ExecutorService executorService;

    public static void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server has been started");
//            AuthService.start();
            executorService = Executors.newCachedThreadPool();

            while (true) {
                waitAndProcessClientConnection(serverSocket);
            }

        } catch (IOException e) {
            System.err.println("Failed to bind port " + port);
            e.printStackTrace();
//        } catch (SQLException e) {
//            System.err.println("Database access error occurs");
//            e.printStackTrace();
        } finally {
//            AuthService.stop();
            if (executorService != null) executorService.shutdown();
        }
    }

    private static void waitAndProcessClientConnection(ServerSocket serverSocket) throws IOException {
        System.out.println("Waiting for new client connection");
        Socket clientSocket = serverSocket.accept();
        System.out.println("Client has been connected");
        ApplicationHandler applicationHandler = new ApplicationHandler(clientSocket);
        applicationHandler.handle();
    }
}

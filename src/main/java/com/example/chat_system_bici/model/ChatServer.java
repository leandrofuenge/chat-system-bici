package com.example.chat_system_bici.model;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final int PORT = 12345;
    private static Set<Socket> clientSockets = new HashSet<>();
    private static Set<String> clientNames = new HashSet<>();
    private static ExecutorService executorService = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Chat server started on port " + PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);
                executorService.execute(new ClientHandler(clientSocket, clientSockets, clientNames));
            }
        } catch (IOException e) {
            System.out.println("Error in the server: " + e.getMessage());
        }
    }

    public static synchronized void broadcastMessage(String message) {
        for (Socket clientSocket : clientSockets) {
            try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                System.out.println("Error broadcasting message to client: " + e.getMessage());
            }
        }
    }
}

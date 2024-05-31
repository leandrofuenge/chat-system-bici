package com.example.chat_system_bici.model;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    private String serverName;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executorService;

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.executorService = Executors.newFixedThreadPool(2); // Pool com duas threads: uma para ler e outra para enviar mensagens
    }

    public void start() throws IOException {
        socket = new Socket(serverName, serverPort);
        System.out.println("Connected to chat server");

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        executorService.submit(new ReadMessageTask());
        executorService.submit(new SendMessageTask());
    }

    public void stop() throws IOException {
        executorService.shutdownNow();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    private class ReadMessageTask implements Runnable {
        public void run() {
            try {
                String message;
                while ((message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        }
    }

    private class SendMessageTask implements Runnable {
        public void run() {
            try (Scanner scanner = new Scanner(System.in)) {
                while (true) {
                    String message = scanner.nextLine();
                    sendMessage(message);
                }
            } catch (Exception e) {
                System.out.println("Error sending message: " + e.getMessage());
            }
        }
    }
}

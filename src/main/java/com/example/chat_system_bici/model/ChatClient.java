package com.example.chat_system_bici.model;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatClient {
    private static final Logger logger = LoggerFactory.getLogger(ChatClient.class);
    private final String serverName;
    private final int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private final ExecutorService executorService;
    private volatile boolean running = true;

    public ChatClient(String serverName, int serverPort) {
        this.serverName = serverName;
        this.serverPort = serverPort;
        this.executorService = Executors.newFixedThreadPool(2); // Pool com duas threads: uma para ler e outra para enviar mensagens
    }

    public void start() throws IOException {
        socket = new Socket(serverName, serverPort);
        logger.info("Connected to chat server at {}:{}", serverName, serverPort);

        out = new PrintWriter(socket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        executorService.submit(new ReadMessageTask());
        executorService.submit(new SendMessageTask());
    }

    public void stop() throws IOException {
        running = false;
        executorService.shutdownNow();
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        logger.info("Disconnected from chat server");
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
                while (running && (message = in.readLine()) != null) {
                    System.out.println(message);
                }
            } catch (IOException e) {
                if (running) {
                    logger.error("Error reading from server", e);
                } else {
                    logger.info("Stopped reading from server");
                }
            }
        }
    }

    private class SendMessageTask implements Runnable {
        public void run() {
            try (Scanner scanner = new Scanner(System.in)) {
                while (running) {
                    if (scanner.hasNextLine()) {
                        String message = scanner.nextLine();
                        sendMessage(message);
                    }
                }
            } catch (Exception e) {
                if (running) {
                    logger.error("Error sending message", e);
                } else {
                    logger.info("Stopped sending messages");
                }
            }
        }
    }
}

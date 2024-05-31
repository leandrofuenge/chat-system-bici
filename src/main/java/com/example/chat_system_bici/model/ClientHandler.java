package com.example.chat_system_bici.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Set;

public class ClientHandler extends Thread {
    private Socket socket;
    private String clientName;
    private PrintWriter out;
    private BufferedReader in;
    private Set<Socket> clientSockets;
    private Set<String> clientNames;

    public ClientHandler(Socket socket, Set<Socket> clientSockets, Set<String> clientNames) {
        this.socket = socket;
        this.clientSockets = clientSockets;
        this.clientNames = clientNames;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            out.println("Enter your name: ");
            clientName = in.readLine();
            synchronized (clientNames) {
                while (clientNames.contains(clientName)) {
                    out.println("Name already taken. Enter another name: ");
                    clientName = in.readLine();
                }
                clientNames.add(clientName);
            }
            out.println("Welcome " + clientName);

            broadcastMessage("Server: " + clientName + " has joined the chat.");

            String message;
            while ((message = in.readLine()) != null) {
                broadcastMessage(clientName + ": " + message);
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        } finally {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to close client resources: " + e.getMessage());
            }
            synchronized (clientSockets) {
                clientSockets.remove(socket);
            }
            synchronized (clientNames) {
                clientNames.remove(clientName);
            }
            broadcastMessage("Server: " + clientName + " has left the chat.");
        }
    }

    private void broadcastMessage(String message) {
        System.out.println(message);
        synchronized (clientSockets) {
            for (Socket clientSocket : clientSockets) {
                try {
                    PrintWriter clientOut = new PrintWriter(clientSocket.getOutputStream(), true);
                    clientOut.println(message);
                } catch (IOException e) {
                    System.out.println("Error broadcasting message: " + e.getMessage());
                }
            }
        }
    }
}

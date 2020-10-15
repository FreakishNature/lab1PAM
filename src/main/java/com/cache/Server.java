package com.cache;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Server {
    ServerSocket myServerSocket;
    boolean isServerRunning = true;

    public Server(int port) {
        try {
            myServerSocket = new ServerSocket(port);
        } catch (IOException ioe) {
            System.out.println("Could not create server socket on port" + port + ". Quitting.");
            System.exit(-1);
        }

        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat(
                "E yyyy.MM.dd 'at' hh:mm:ss a zzz");
        System.out.println("Started at: " + formatter.format(now.getTime()));

        while (isServerRunning) {
            try {
                Socket clientSocket = myServerSocket.accept();
                ConnectionHandler cliThread = new ConnectionHandler(clientSocket);
                cliThread.start();
            } catch (IOException ioe) {
                System.out.println("Exception found on accept. Ignoring. Stack Trace :");
                ioe.printStackTrace();
            }
        }
        try {
            myServerSocket.close();
            System.out.println("Server Stopped");
        } catch (Exception ioe) {
            System.out.println("Error Found stopping server socket");
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        new Server(4001);
    }
}
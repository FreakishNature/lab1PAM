package com.cache;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
public class CacheClient extends Thread {
    public Socket getSocket() {
        return socket;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    volatile String message = null;

    public String getReturnedMessage() {
        return returnedMessage;
    }

    volatile String returnedMessage = null;
    private Socket socket;

    public CacheClient(String serverAddress, int serverPort) throws Exception {
        this.socket = new Socket(serverAddress, serverPort);
    }

    public void run() {

        try {
            while (true) {
                if(message != null){
                    String msg;
                    PrintWriter out;
                    InputStream fromServer;
                    try {
                        out = new PrintWriter(this.socket.getOutputStream(), true);
                        fromServer = socket.getInputStream();

                        BufferedReader reader = new BufferedReader(new InputStreamReader(fromServer));

                        String toServer = message;
                        out.println(toServer);
                        out.flush();

                        returnedMessage = reader.readLine();
                        System.out.println("Message: " + returnedMessage);
                        if(message.equals("exit")){
                            break;
                        }

                        message = null;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) throws Exception {
        CacheClient client = new CacheClient("localhost", 3001);
        System.out.println("\r\nConnected to Server: " + client.socket.getInetAddress());
        client.start();

        client.setMessage("auth edik onelove");
        Thread.sleep(100);
        client.setMessage("lpush services 3001");
        Thread.sleep(100);
        client.setMessage("lpush services 3002");
        Thread.sleep(100);
        client.setMessage("get services");

        Thread.sleep(3000);

    }
}
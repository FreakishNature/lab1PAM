package com.cache;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.logging.log4j.ThreadContext.isEmpty;

class ConnectionHandler extends Thread {
    Socket clientSocket;
    boolean isClientConnected = true;
    boolean authenticated = false;

    ConnectionHandler(Socket s) {
        clientSocket = s;
    }

    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;
        String toClient;
        System.out.println(
                "Accepted Client Address - " + clientSocket.getInetAddress().getHostName());
        try {
            in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            out = new PrintWriter(
                    new OutputStreamWriter(clientSocket.getOutputStream()));

            while (isClientConnected) {
                // from client
                String clientCommand = in.readLine();
                System.out.println("Client Says :" + clientCommand);
                toClient = processCommand(clientCommand);

                if (clientCommand.equalsIgnoreCase("quit")) {
                    isClientConnected = false;
                    System.out.print("Stopping client thread for client : ");
                } else {
                    // to client
                    out.println(toClient);
                    out.flush();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
                System.out.println("...Stopped");
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public String authenticate(String username, String password) {
        if (username.equals("edik") && password.equals("onelove")) {
            authenticated = true;
            return "Authenticated";
        } else {
            authenticated = false;
            return "Invalid username or password";
        }

    }

    public String processCommand(String clientString) {
        List<String> args = new ArrayList<>(Arrays.asList(clientString.split(" ")));
        String command = args.remove(0).toUpperCase();
        Object response = null;

        if (!authenticated) {
            if (command.equals("AUTH")){
                return authenticate(args.get(0), args.get(1));
            }
            return "Please authenticate";
        }


        try {
            switch (command) {
                case "SET":
                    Cache.set(args.get(0), args.get(1));
                    break;
                case "GET":
                    response = Cache.get(args.get(0));
                    break;
                case "SETNX":
                    Cache.setnx(args.get(0), args.get(1));
                    break;
                case "MGET":
                    response = Cache.mget(args);
                    break;
                case "DEL":
                    response = Cache.del(args);
                    break;
                case "INCR":
                    response = Cache.incr(args.get(0));
                    break;
                case "LLEN":
                    response = Cache.llen(args.get(0));
                    break;
                case "LREM":
                    response = Cache.lrem(args.get(0), Integer.parseInt(args.get(1)), args.get(2));
                    break;
                case "LPUSH":
                    response = Cache.lpush(args.get(0), args.get(1));
                    break;
                case "RPOPLPUSH":
                    response = Cache.rpoplpush(args.get(0), args.get(1));
                    break;
                case "LRANGE":
                    response = Cache.lrange(args.get(0), Integer.parseInt(args.get(1)), Integer.parseInt(args.get(2)));
                    break;
                default:
                    throw new Exception("Unknown command" + command);
            }
        } catch (
                Exception e) {
            response = e.getMessage();
        }

        String output;

        if (response == null)
            output = "null";
        else if (response instanceof Exception) {
            output = "(error) " + ((Exception) response).getMessage();
        } else if (response instanceof Integer) {
            output = "(integer) " + (Integer) response;
        } else if (response instanceof List) {
            if (((List<Object>) response).isEmpty()) {
                output = "(empty array)";
            } else {
                String joinedString = ((List<Object>) response).stream()
                        .map(object -> Objects.toString(object, null))
                        .collect(Collectors.joining(","));
                output = "(array) " + joinedString;
            }
        } else
            output = (String) response;

        return output;
    }
}
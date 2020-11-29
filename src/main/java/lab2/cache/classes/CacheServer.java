package lab2.cache.classes;

import lab2.saga.classes.Connector;
import lab2.saga.classes.Locker;
import lab2.saga.classes.Reader;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class CacheServer extends Thread {
    Logger logger = Logger.getLogger(CacheServer.class);

    Cache cache = new Cache();
    Locker locker = new Locker();
    int port;
    volatile boolean isRunning = true;

    public void terminate(){
        isRunning = false;
    }

    public CacheServer(int port) {
        this.port = port;
    }

    CopyOnWriteArrayList<Socket> activeClients = new CopyOnWriteArrayList<>();



    public void run(){

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Connector connector = new Connector(activeClients, serverSocket,locker);
            Thread.sleep(1000);
            connector.start();

            logger.info("Server is listening on port " + port);

            MAIN_LOOP: while (isRunning){
                locker.lock();

                String message = "empty";
                for (Reader reader: connector.getReaders()) {
                    String tmp = reader.getLine();
                    if(tmp.equals("EXIT")){
                        break MAIN_LOOP;
                    }
                    if(tmp != null){
                        message = ConnectionHandler.processCommand(cache,tmp);
                    }
                }

                logger.info("sending message : " + message);
                for (Socket s: activeClients ) {
                    OutputStream output = s.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    writer.println(message);
                }
                logger.info("message sent : " + message);
            }

            for (Socket s: activeClients ) {
                OutputStream output = s.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                writer.println("CLOSE");
            }

            for(Socket s: activeClients){
                s.close();
            }

        } catch (IOException | InterruptedException ex) {
            logger.error("Server exception: " + ex.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {

    }
}
package lab2.saga.classes;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCPBroker {
    Logger logger = Logger.getLogger(TCPBroker.class);
    Locker locker = new Locker();

    int port = 4000;
    CopyOnWriteArrayList<Socket> activeClients = new CopyOnWriteArrayList<>();



    public void runServer(){

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Connector connector = new Connector(activeClients, serverSocket,locker);
            Thread.sleep(1000);
            connector.start();

            logger.info("Server is listening on port " + port);

            while (true){
                locker.lock();

                String message = "empty";
                for (Reader reader: connector.getReaders()) {
                    String tmp = reader.getLine();
                    if(tmp != null){
                        message = tmp;
                    }
                }

                logger.info("Broadcasting message : " + message);
                for (Socket s: activeClients ) {
                    OutputStream output = s.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    writer.println(message);
                }
            }

        } catch (IOException | InterruptedException ex) {
            logger.error("Server exception: " + ex.getMessage());
        }
    }

}

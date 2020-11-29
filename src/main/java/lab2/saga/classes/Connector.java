package lab2.saga.classes;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class Connector extends Thread{
    CopyOnWriteArrayList<Socket> activeClients;
    ServerSocket serverSocket;
    Locker locker;
    CopyOnWriteArrayList<Reader> readers = new CopyOnWriteArrayList<>();
    volatile boolean isWorking = true;

    public CopyOnWriteArrayList<Reader> getReaders() {
        return readers;
    }

    public void run(){
        while (isWorking){
            try {
                Socket socket = serverSocket.accept();
                Reader reader = new Reader(socket, locker);
                reader.setPort(socket.getPort());
                readers.add(reader);
                activeClients.add(socket);
                reader.start();

            } catch (IOException e) {
                e.printStackTrace();
                isWorking = false;
                readers.forEach(Reader::disable);
            }
        }
    }

    public Connector(CopyOnWriteArrayList<Socket> activeClients, ServerSocket serverSocket, Locker locker){
        this.activeClients = activeClients;
        this.serverSocket = serverSocket;
        this.locker = locker;
    }
}

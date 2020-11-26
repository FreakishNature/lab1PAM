package lab2.saga;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCPBroker {

    static class Reader extends Thread{
        Socket socket;
        String line;
        Locker locker;

        public String getLine() {
            System.out.println("Get line : " + line);
            String msg = line;
            line = null;
            return msg;
        }

        public void run(){
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true){
                    synchronized (this){
                        line = reader.readLine();
                        System.out.println("received message : " + line);
                    }
                    locker.unlock();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Reader(Socket socket,Locker locker){
            this.socket = socket;
            this.locker = locker;
        }
    }

    Locker locker = new Locker();

    static class Connector extends Thread{
        CopyOnWriteArrayList<Socket> activeClients;
        ServerSocket serverSocket;
        Locker locker;
        CopyOnWriteArrayList<Reader> readers = new CopyOnWriteArrayList<>();

        public CopyOnWriteArrayList<Reader> getReaders() {
            return readers;
        }

        public void run(){
            while (true){
                try {
                    Socket socket = serverSocket.accept();
                    Reader reader = new Reader(socket, locker);
                    readers.add(reader);
                    activeClients.add(socket);
                    reader.start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        Connector(CopyOnWriteArrayList<Socket> activeClients,ServerSocket serverSocket, Locker locker){
            this.activeClients = activeClients;
            this.serverSocket = serverSocket;
            this.locker = locker;
        }
    }



    int port = 4000;
    CopyOnWriteArrayList<Socket> activeClients = new CopyOnWriteArrayList<>();



    void runServer(){

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            Connector connector = new Connector(activeClients, serverSocket,locker);
            Thread.sleep(1000);
            connector.start();

            System.out.println("Server is listening on port " + port);

            while (true){
                locker.lock();

                String message = "empty";
                for (Reader reader: connector.getReaders()) {
                    String tmp = reader.getLine();
                    System.out.println("reader getLine : " + tmp);
                    if(tmp != null){
                        message = tmp;
                    }
                }

                System.out.println("sending message : " + message);
                for (Socket s: activeClients ) {
                    OutputStream output = s.getOutputStream();
                    PrintWriter writer = new PrintWriter(output, true);

                    writer.println(message);
                }
            }

        } catch (IOException | InterruptedException ex) {
            System.out.println("Server exception: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        TCPBroker tcpBroker = new TCPBroker();
        tcpBroker.runServer();
    }

}

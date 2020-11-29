package lab2.saga.classes;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

public class Sender extends Thread {
    Socket socket;
    String message;
    Locker locker = new Locker();

    volatile boolean isRunning = true;

    public void disable(){
        isRunning = false;
        locker.unlock();
    }

    public void setMessage(String message) throws InterruptedException {
        this.message = message;
        locker.unlock();
    }

    public void run(){
        try {
            OutputStream output = socket.getOutputStream();
            while (isRunning){
                locker.lock();

                PrintWriter writer = new PrintWriter(output, true);
                writer.println(message);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public Sender(Socket socket){
        this.socket = socket;
    }
}

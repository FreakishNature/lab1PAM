package lab2.saga.classes;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Reader extends Thread{
    Logger logger = Logger.getLogger(Reader.class);
    Socket socket;
    String line;
    Locker locker;
    volatile boolean isWorking = true;

    public void disable(){
        isWorking = false;
    }

    int port;

    public void setPort(int port) {
        this.port = port;
    }

    public void run(){
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            while (isWorking){
                synchronized (this){
                    line = reader.readLine();
                    logger.info("received message : " + line);
                    logger.info("isWorking : " + isWorking);
                    logger.info("port : " + port);
//                    Thread.sleep(1000);
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


    public String getLine() {
        String msg = line;
        line = null;
        return msg;
    }
}

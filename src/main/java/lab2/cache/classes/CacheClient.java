package lab2.cache.classes;

import lab2.gateway.cache.CacheManager;
import lab2.saga.classes.Locker;
import lab2.saga.classes.Processor;
import lab2.saga.classes.Sender;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class CacheClient extends Thread {

    String host;
    int port;
    Sender sender;
    Locker locker = new Locker();
    Logger logger = Logger.getLogger(CacheClient.class);
    Locker receivingMessageLocker = new Locker();
    String receivedMessage;
    volatile boolean isWorking = true;

    public void disable(){
        isWorking = false;
    }
    ConcurrentHashMap<String, CopyOnWriteArrayList<Processor>> processors = new ConcurrentHashMap<>();
    CopyOnWriteArrayList<Processor> processorsForAnyTopic = new CopyOnWriteArrayList<>();


    public void sendMessage(String msg) throws InterruptedException {
        sender.setMessage(msg);
        logger.info(port+"Locked setMessage");
        Thread.sleep(100); // for loading and showing how cache works
    }
    public String getReceivedMessage() throws IOException {
        String tmp = receivedMessage;
        receivedMessage = null;
        logger.info(port+"unlock received message");
        receivingMessageLocker.unlock();
        if("Threw Exception".equals(receivedMessage)){
            throw new IOException();
        }
        return tmp;
    }

    public void run(){

        try (Socket socket = new Socket(host, port)) {
            sender = new Sender(socket);
            sender.start();

            logger.info(port + "start loop");
            while (isWorking){
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                logger.info(port + "Receiving message");
                receivedMessage = reader.readLine();

                logger.info(port + "unlock set message");
//                locker.unlock();

                logger.info(port + "receivingMessageLocker locked");
                receivingMessageLocker.lock();
            }

            sender.disable();
        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        }  catch (SocketException e){
            logger.error("deleting from cache CacheClient");
            CacheManager cm = CacheManager.getInstance();
            cm.remove(this);
            receivedMessage = "Threw Exception";
        }catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("I/O error: " + ex.getMessage());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public CacheClient(String host, int port){
        this.host = host;
        this.port = port;
    }



    public static void main(String[] args) throws Exception {
        CacheManager client = new CacheManager(2);
        client.start();
        Thread.sleep(100);

//        client.sendMessage("lpush services 3001");
//        System.out.println(client.getReceivedMessage());
////        Thread.sleep(100);
//
//        client.sendMessage("lpush services 3002");
//
//        System.out.println(client.getReceivedMessage());
////        Thread.sleep(100);
//        client.sendMessage("get services");
//
//        System.out.println(client.getReceivedMessage());
//        lpush

        client.auth("asd","dsd");
        client.lpush("services","3001");
        client.lpush("services","3002");

//        client.lpush("services","3002");
        System.out.println("Message : " + client.get("services"));
        System.out.println("finished");

    }
}
package lab2.saga.classes;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class TCPClient extends Thread {
    String host;
    int port;
    Sender sender;
    ConcurrentHashMap<String, CopyOnWriteArrayList<Processor>> processors = new ConcurrentHashMap<>();
    CopyOnWriteArrayList<Processor> processorsForAnyTopic = new CopyOnWriteArrayList<>();
    Locker locker = new Locker();

    Logger logger = Logger.getLogger(TCPClient.class);

    public void sendMessage(String topic,String msg) throws InterruptedException {
        sender.setMessage(topic + "|" + msg);
        Thread.sleep(1000);
//        locker.lock();
        logger.debug("Sending lock unlocked");
    }

    public void addListener(String topic, Processor processor){
        if(topic.equals("*")){
            processorsForAnyTopic.add(processor);
            return;
        }

        if (!processors.containsKey(topic)) {
            processors.put(topic, new CopyOnWriteArrayList<>());
        }
        processors.get(topic).add(processor);
    }

    public void run(){

        try (Socket socket = new Socket(host, port)) {
            sender = new Sender(socket);
            sender.start();

            while (true){
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                logger.debug("before read");
                String receivedMessage = reader.readLine();
                String[] topicAndMessage = receivedMessage.split("\\|");
                String topic = topicAndMessage[0];
                String message = topicAndMessage[1];
                logger.debug("received message from broker : " + receivedMessage);
//                synchronized (this){
                    for(Map.Entry<String,CopyOnWriteArrayList<Processor>> entry : processors.entrySet()){
                        if(entry.getKey().equals(topic)){
                            for(Processor processor: entry.getValue()){
                                processor.process(message);
                            }
                        }
                    }

                    for(Processor processor: processorsForAnyTopic){
                        processor.process(message);
                    }
//                }
//                locker.unlock();
                logger.debug("I tried to unlock sending lock");
            }

        } catch (UnknownHostException ex) {
            logger.error("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            logger.error("I/O error: " + ex.getMessage());
        }

    }

    public TCPClient(String host, int port){
        this.host = host;
        this.port = port;
    }


    public static void main(String[] args) throws InterruptedException {
        TCPClient tcpClient = new TCPClient("localhost", 4000);

        tcpClient.addListener("*", System.out::println);
        tcpClient.addListener("topic", m -> {
            System.out.println("Specific topic with message : " + m);
        });

        tcpClient.start();
        Thread.sleep(3000);
        tcpClient.sendMessage("topic","test");
    }
}

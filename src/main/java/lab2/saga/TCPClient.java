package lab2.saga;

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

    static class Sender extends Thread {
        Socket socket;
        String message;
        Locker locker = new Locker();

        public void setMessage(String message) throws InterruptedException {
            System.out.println("Message set");
            this.message = message;
            locker.unlock();
        }

        public void run(){
            try {
                OutputStream output = socket.getOutputStream();

                while (true){

                    System.out.println("before lock");
                    locker.lock();

                    System.out.println("message is sending");
                    PrintWriter writer = new PrintWriter(output, true);
                    writer.println(message);
                    System.out.println("message sent");
                }

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        Sender(Socket socket){
            this.socket = socket;
        }
    }

    public void sendMessage(String topic,String msg) throws InterruptedException {
        sender.setMessage(topic + "|" + msg);
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

                String receivedMessage = reader.readLine();
                String[] topicAndMessage = receivedMessage.split("\\|");
                String topic = topicAndMessage[0];
                String message = topicAndMessage[1];

                synchronized (this){
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
                }
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
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

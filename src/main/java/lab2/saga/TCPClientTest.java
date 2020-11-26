package lab2.saga;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClientTest {

    void startClient(){
        String hostname = "localhost";
        int port = 4000;

        try (Socket socket = new Socket(hostname, port)) {
            while (true){
                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                String time = reader.readLine();

                System.out.println(time);
            }

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new TCPClientTest().startClient();
    }
}

package lab2.saga;

import lab2.saga.classes.TCPBroker;

public class SagaApp {
    public static void main(String[] args) {
        TCPBroker tcpBroker = new TCPBroker();
        tcpBroker.runServer();
    }
}

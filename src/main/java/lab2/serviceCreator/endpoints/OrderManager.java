package lab2.serviceCreator.endpoints;

import lab2.saga.TCPClient;
import lab2.serviceCreator.model.CreateOrderRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderManager {
    TCPClient tcpClient;
    String host = "localhost";
    int port = 4000;

    OrderManager(){
        tcpClient = new TCPClient(host,port);
        tcpClient.addListener("*",m -> {
            System.out.println("message received : "+ m);
        });
        tcpClient.start();
    }

    @PostMapping("/order")
    void createOrder(CreateOrderRequest orderRequest){
//        tcpClient.sendMessage("create_order",);
    }


}

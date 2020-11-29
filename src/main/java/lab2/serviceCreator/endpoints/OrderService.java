package lab2.serviceCreator.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab2.serviceCreator.OrderApp;
import lab2.serviceCreator.database.DBManager;
import lab2.serviceCreator.database.OrderDB;
import lab2.saga.classes.TCPClient;
import lab2.serviceCreator.model.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class OrderService {
    Logger logger = Logger.getLogger(OrderService.class);
    TCPClient tcpClient;
    String host = "localhost";
    int port = 4000;
    OrderDB sql;
    CopyOnWriteArrayList<String> queue = new CopyOnWriteArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    static public DBManager dbManager;
    String id;

    String createTask() {
        return new Date().getTime() + "";
    }


    OrderService(@Value("${app.id}") String id,@Value("${db.id}") String dbId){
        this.id = id;
        try {
            sql = new OrderDB("orderdb_" + dbId);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        System.out.println("server id : " + id);
        tcpClient = new TCPClient(host,port);
        tcpClient.addListener("change_status_" + id,m -> {
            ChangeQuantityAndStatusRequest request = mapper.readValue(m, ChangeQuantityAndStatusRequest.class);
            logger.debug("Change status for : " + mapper.writeValueAsString(request));
            dbManager.changeQuantity(request.getId(),request.getQuantity());
            dbManager.changeStatus(request.getId(),request.getStatus());
            logger.info("changed status and quantity for id - " + request.getId() + " to " + request.getStatus() + " " + request.getQuantity());

            RestTemplate template = new RestTemplate();
            template.delete(OrderApp.URI + "/keys");
        });
        tcpClient.start();
    }

    @PostMapping("/order")
    ResponseEntity<Object> createOrder(@RequestBody CreateOrderRequest orderRequest) throws InterruptedException, JsonProcessingException {
        String task = createTask();
        logger.debug("POST /order " + mapper.writeValueAsString(orderRequest));

        int id = dbManager.createOrder(orderRequest.getItem(),"BUY",orderRequest.getPrice(),orderRequest.getRemainingQuantity());
        tcpClient.sendMessage("created_order_" + id,id+"");

        queue.remove(task);
        return new ResponseEntity<>(new CreateOrderResponse(id),HttpStatus.ACCEPTED);
    }

    @GetMapping("/order/{id}")
    ResponseEntity<Object> getOrder(@PathVariable int id)  {
        try {
            return new ResponseEntity<>(sql.getOrder(id),HttpStatus.OK);
        } catch (IOException e) {
            return new ResponseEntity<>(new ErrorResponse("Not found"),HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/stock")
    ResponseEntity<Object> createStock(@RequestBody CreateOrderRequest orderRequest) throws InterruptedException, JsonProcessingException {
        logger.debug("POST /stock " + mapper.writeValueAsString(orderRequest));

        int id = dbManager.createOrder(orderRequest.getItem(),"SELL",orderRequest.getPrice(),orderRequest.getRemainingQuantity());

        return new ResponseEntity<>(new CreateOrderResponse(id),HttpStatus.ACCEPTED);
    }


    ServerStatus status = new ServerStatus();

    @GetMapping("/serverStatus")
    Object getServerStatus(){
        return status;
    }

    @DeleteMapping("/shutdown")
    Object shutdownServer(){
        return status;
    }
}

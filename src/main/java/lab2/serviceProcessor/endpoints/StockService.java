package lab2.serviceProcessor.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab2.saga.classes.TCPClient;
import lab2.serviceCreator.database.OrderDB;
import lab2.serviceCreator.model.ChangeQuantityAndStatusRequest;
import lab2.serviceCreator.model.OrderResponse;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
public class StockService {
    Logger logger = Logger.getLogger(StockService.class);
    TCPClient tcpClient;
    String host = "localhost";
    int port = 4000;
    OrderDB sql;
    CopyOnWriteArrayList<String> queue = new CopyOnWriteArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    String createTask() {
        return new Date().getTime() + "";
    }
    String id;

    public void changeStatus(int id,String status,int quantity) throws JsonProcessingException, InterruptedException {
        tcpClient.sendMessage("change_status_" + this.id,mapper.writeValueAsString(new ChangeQuantityAndStatusRequest(id,status,quantity)));

    }
    StockService(@Value("${app.id}") String serverId, @Value("${db.id}") String dbId){
        this.id = serverId;

        try {
            sql = new OrderDB("orderdb_" + dbId);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        tcpClient = new TCPClient(host,port);
        tcpClient.addListener("created_order_" + id,m -> {
            int id = Integer.parseInt(m);
            OrderResponse createdOrder = sql.getOrder(id);
            logger.info("Validating order : " + createdOrder);
            String itemName = createdOrder.getItem();
            int quantity = createdOrder.getRemainingQuantity();

            List<OrderResponse> orders = sql.getOrdersAscByPrice(itemName);
            List<OrderResponse> ordersCopy = orders.stream().map(OrderResponse::new).collect(Collectors.toList());

            for(OrderResponse order: orders){
                if(order.getPrice() < createdOrder.getPrice() && !order.getStatus().equals("FILLED")  && createdOrder.getRemainingQuantity() != 0){
                    if(order.getRemainingQuantity() < createdOrder.getRemainingQuantity()){
                        createdOrder.setRemainingQuantity(createdOrder.getRemainingQuantity() - order.getRemainingQuantity());

                        order.setRemainingQuantity(0);
                    } else {
                        order.setRemainingQuantity(order.getRemainingQuantity() - createdOrder.getRemainingQuantity());

                        createdOrder.setRemainingQuantity(0);
                    }

                    if(order.getRemainingQuantity() == 0){
                        order.setStatus("FILLED");
                    }
                }
            }

            if(createdOrder.getRemainingQuantity() != 0){
                try {
                    changeStatus(id,"REJECTED",quantity);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }

            for(int i = 0; i < orders.size(); i++){
                OrderResponse changedOrder = orders.get(i);
                OrderResponse copyOrder = ordersCopy.get(i);

                if(!changedOrder.getStatus().equals(copyOrder.getStatus())){
                    try {
                        logger.debug("Updating for order with index - " + i);
                        changeStatus(changedOrder.getId(), changedOrder.getStatus(), changedOrder.getRemainingQuantity());
                        logger.debug("Updating for order with index - " + i + " finished");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            logger.info("Order validated");
            try {
                changeStatus(id,"FILLED",0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        tcpClient.start();
    }

}

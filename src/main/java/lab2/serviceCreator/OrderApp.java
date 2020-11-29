package lab2.serviceCreator;

import lab2.serviceCreator.database.DBManager;
import lab2.serviceCreator.database.OrderDB;
import lab2.serviceCreator.endpoints.OrderService;
import lab2.serviceCreator.model.ConnectPortRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class OrderApp {
    static public String URI = "http://localhost:3000";
    public static void main(String[] args) {

        int serverAmount = 2;

        for(int i = 1; i <= serverAmount * 2; i++){
            OrderDB.copyDatabase("orderdb_1","orderdb_" + i,"orders");
        }

        OrderService.dbManager = new DBManager(serverAmount * 2);
        for(int i = 1 ; i <= serverAmount; i++){
            SpringApplication application = new SpringApplication(OrderApp.class);
            Map<String,Object> props = new HashMap<>();
            props.put("server.port", "300" + i);
            props.put("app.id",i+"");
            props.put("db.id", i+"");
            application.setDefaultProperties(props);

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> entity = template.postForEntity(URI + "/createConnection", new ConnectPortRequest("300" + i),String.class);
            // add error handling

            application.run(args);
        }
    }
}

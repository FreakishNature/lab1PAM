package lab2.serviceProcessor;

import lab2.serviceCreator.OrderApp;
import lab2.serviceCreator.model.ConnectPortRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class LogicApp {
    static String URI = "http://localhost:3000";
    public static void main(String[] args) {
        int serverAmount = 2;

        for(int i = 1 ; i <= serverAmount; i++){
            SpringApplication application = new SpringApplication(LogicApp.class);
            Map<String,Object> props = new HashMap<>();
            props.put("server.port", "301" + i);
            props.put("app.id",i+"");
            props.put("db.id",(i + serverAmount)+"");
            application.setDefaultProperties(props);
            // add error handling

            application.run(args);
        }
    }
}

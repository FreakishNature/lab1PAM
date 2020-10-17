package com.service1;

import com.gateway.endpoints.Connection;
import com.model.ConnectPortRequest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SpringBootApplication
public class Application {
    static String URI = "http://localhost:3000";
    public static void main(String[] args) {
        int serverAmount = 4;
        if(args.length > 0){
            serverAmount = Integer.parseInt(args[0]);
            URI =  "http://" + args[1] +":3000";
            Connection.HOST = args[1];
        }
        for(int i = 1 ; i <= serverAmount; i++){
            SpringApplication application = new SpringApplication(Application.class);
            application.setDefaultProperties(Collections
                    .singletonMap("server.port", "300" + i));

            RestTemplate template = new RestTemplate();
            ResponseEntity<String> entity = template.postForEntity(URI + "/createConnection", new ConnectPortRequest("300" + i),String.class);
            // add error handling

            application.run(args);
        }
    }
}

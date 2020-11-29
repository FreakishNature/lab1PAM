package lab2.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class GatewayApp {
    public static void main(String[] args) throws Exception {
//        Connection.HOST = args[0];

        SpringApplication application = new SpringApplication(GatewayApp.class);
        application.setDefaultProperties(Collections
                .singletonMap("server.port", "3000"));
        application.run(args);

    }
}

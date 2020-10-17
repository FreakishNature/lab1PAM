package com.gateway;

import com.cache.CacheClient;
import com.gateway.endpoints.Connection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
//        Connection.HOST = args[0];

        SpringApplication application = new SpringApplication(Application.class);
        application.setDefaultProperties(Collections
                .singletonMap("server.port", "3000"));
        application.run(args);

    }
}

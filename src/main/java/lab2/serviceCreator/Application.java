package lab2.serviceCreator;

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
            SpringApplication application = new SpringApplication(Application.class);
            application.setDefaultProperties(Collections
                    .singletonMap("server.port", "3001"));

//            RestTemplate template = new RestTemplate();
//            ResponseEntity<String> entity = template.postForEntity(URI + "/createConnection", new ConnectPortRequest("300" + i),String.class);
            // add error handling

            application.run(args);

    }
}

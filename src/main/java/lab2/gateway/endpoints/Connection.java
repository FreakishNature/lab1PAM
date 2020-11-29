package lab2.gateway.endpoints;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lab2.gateway.cache.Cache;
import lab2.gateway.cache.CacheManager;
import lab2.serviceCreator.OrderApp;
import lab2.serviceCreator.database.OrderDB;
import lab2.serviceCreator.model.ConnectPortRequest;
import lab2.serviceCreator.model.CreateOrderRequest;
import lab2.serviceCreator.model.ErrorResponse;
import lab2.serviceCreator.model.ServerStatus;
import lab2.serviceProcessor.endpoints.StockService;
import org.apache.log4j.Logger;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@RestController
public class Connection {
    Logger logger = Logger.getLogger(Connection.class);
    public static String HOST = "localhost";
    List<String> ports = new ArrayList<>();
    RestTemplate restTemplate;
    int maxLoad = 3;
    ObjectMapper mapper = new ObjectMapper();

    CacheManager cacheManager = new CacheManager(2);

    {
        restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(30_000))
                .setReadTimeout(Duration.ofMillis(30_000))
                .build();
    }

    Connection() throws Exception {
        cacheManager.start();
        cacheManager.auth("edik","onelove");
    }


    @PostMapping("/createConnection")
    public String createConnection(@RequestBody ConnectPortRequest connectPortRequest) throws JsonProcessingException {
        logger.debug("POST /createConnection " + mapper.writeValueAsString(connectPortRequest));

        try {
            cacheManager.lpush("services",connectPortRequest.getPort());
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }

        ports.add(connectPortRequest.getPort());

        logger.info("Connected with port : " + connectPortRequest.getPort());
        return "connected";
    }

    @GetMapping("/instances")
    public String getInstances(){
        logger.debug("GET /instances ");

        try {
            return cacheManager.get("services");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @PostMapping("/stock")
    public ResponseEntity<Object> create(@RequestBody CreateOrderRequest request) throws InterruptedException, IOException {
        logger.debug("POST /stock " + mapper.writeValueAsString(request));

        String port = selectServer();

        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(request);
        ResponseEntity<Object> responseEntity;
        responseEntity = restTemplate.postForEntity("http://" + HOST +":" + port + "/stock", entity, Object.class);

        clearKeys();

        return responseEntity;
    }

    @PostMapping("/order")
    public ResponseEntity<Object> order(@RequestBody CreateOrderRequest request) throws InterruptedException, IOException {
        logger.debug("POST /order " + mapper.writeValueAsString(request));

        String port = selectServer();

        HttpEntity<CreateOrderRequest> entity = new HttpEntity<>(request);
        ResponseEntity<Object> responseEntity;
        responseEntity = restTemplate.postForEntity("http://" + HOST +":" + port + "/order", entity, Object.class);
        clearKeys();

        return responseEntity;
    }

    @GetMapping("/order/{id}")
    public Object getStatus(@PathVariable String id) throws InterruptedException, IOException {
        logger.debug("GET /order/" + id);

        String port = selectServer();
        String response = "";
        String key = "GET_/order/" + id;
        try {
            if(cacheManager.isInCache(key)){
                response = cacheManager.get(key).replace("(array) ","");
                logger.debug("USED CACHED DATA : " + response);
            } else {
                try{
                    response = mapper.writeValueAsString(restTemplate.getForObject("http://" + HOST +":" + port + "/order/" + id, Object.class));
                }catch (HttpClientErrorException | JsonProcessingException e){
                    try {
                        response = mapper.writeValueAsString(new ErrorResponse("Not found"));
                    } catch (JsonProcessingException jsonProcessingException) {
                        jsonProcessingException.printStackTrace();
                    }
                }

                cacheManager.lpush(key, response);
                cacheManager.lpush("keys",key);
            }
            if(response.contains("error")){
                return new ResponseEntity<>(mapper.readValue(response,Object.class),HttpStatus.NOT_FOUND);
            } else {
                return mapper.readValue(response,Object.class);
            }
        } catch (InterruptedException | JsonProcessingException e) {
            e.printStackTrace();
        }

        return new ResponseEntity<>(new ErrorResponse("Internal server error"),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    void clearKeys() throws InterruptedException {
       logger.debug("CACHE HAS BEEN CLEARED");
       String receivedKeys = cacheManager.get("keys");

       if(receivedKeys.equals("null")){
           return;
       }

       String[] keys = receivedKeys.replace("(array) ","").split(",");

       Arrays.stream(keys).forEach( k ->{
           try {
               cacheManager.lrem("keys",1,k);
               cacheManager.lrem(k,1, cacheManager.get(k).replace("(array) ",""));
           } catch (InterruptedException | IOException e) {
               e.printStackTrace();
           }
       });
    }

    @DeleteMapping("/keys")
    public void clearKeysEndpoint() throws InterruptedException {
        logger.debug("DELETE /keys");
        clearKeys();
    }

    public String selectServer() throws InterruptedException, IOException {
        try {
            ports = Arrays.asList(cacheManager.get("services").split(" ")[1].split(","));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(ports.isEmpty()){
            return  "NO_PORT";
        }
        String selectedPort = ports.get(new Random().nextInt(Integer.MAX_VALUE) % ports.size());
        logger.info("Selected port : " + selectedPort);

        RestTemplate restTemplate = new RestTemplate();

        ServerStatus serverStatus = restTemplate.getForObject("http://" + HOST +":" + selectedPort + "/serverStatus", ServerStatus.class);
        if(!serverStatus.isWorking()){
            cacheManager.lrem("services",1,selectedPort);
            selectServer();
        }
        return selectedPort;
    }

    @DeleteMapping("/cache/{i}")
    public void deleteCache(@PathVariable int i) throws InterruptedException {
        cacheManager.killCache(i);
    }

}

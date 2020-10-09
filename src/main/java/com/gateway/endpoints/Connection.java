package com.gateway.endpoints;

import com.model.ConnectPortRequest;
import com.model.CreateDataRequest;
import com.model.ErrorResponse;
import com.model.LoadResponse;
import org.apache.logging.log4j.util.Strings;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RestController
public class Connection {
    final String HOST = "localhost";
    List<String> ports = new ArrayList<>();
    RestTemplate restTemplate;
    int maxLoad = 3;

    {
        restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(30_000))
                .setReadTimeout(Duration.ofMillis(30_000))
                .build();
    }

    @PostMapping
    public String createConnection(@RequestBody ConnectPortRequest connectPortRequest){
        ports.add(connectPortRequest.getPort());
        System.out.println("Connected with port : " + connectPortRequest.getPort());
        return "connected";
    }

    @GetMapping("/instances")
    public String getInstances(){
        return Strings.join(ports,' ');
    }

    @PutMapping("/changeTimeout")
    public void changeTimeout(@RequestParam int timeout){
        restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofMillis(timeout))
                .setReadTimeout(Duration.ofMillis(timeout))
                .build();
    }


    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody CreateDataRequest request){
        String port = getPortWithMinimalLoad();
        HttpEntity<CreateDataRequest> entity = new HttpEntity<>(request);

        if(port.equals("NO_PORT")){
            return new ResponseEntity<>(new ErrorResponse("Server is overload"),HttpStatus.INTERNAL_SERVER_ERROR);
        }
        ResponseEntity<Object> responseEntity;
        try{
            responseEntity = restTemplate.postForEntity("http://" + HOST +":" + port + "/create", entity, Object.class);
        }catch (Throwable ex){
            ports.remove(port);
            return new ResponseEntity<>(new ErrorResponse("Timeout exception"),HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return responseEntity;
    }
    @GetMapping("/getStatus/{id}")
    public Object getStatus(@PathVariable String id){
        String port = getPortWithMinimalLoad();

        if(port.equals("NO_PORT")){
            return new ResponseEntity<>(new ErrorResponse("Server is overload"),HttpStatus.INTERNAL_SERVER_ERROR);
        }

        try{
            return restTemplate.getForObject("http://" + HOST +":" + port + "/getStatus/" + id, Object.class);
        }catch (HttpClientErrorException e){
            return new ResponseEntity<>(new ErrorResponse("Not found"),HttpStatus.NOT_FOUND);
        }
    }
    @PutMapping("/change/{id}")
    public ResponseEntity<Object> change(@PathVariable String id, @RequestBody CreateDataRequest request){
        try{
            String port = getPortWithMinimalLoad();
            if(port.equals("NO_PORT")){
                return new ResponseEntity<>(new ErrorResponse("Server is overload"),HttpStatus.INTERNAL_SERVER_ERROR);
            }

            HttpEntity<CreateDataRequest> entity = new HttpEntity<>(request);

            restTemplate.put("http://" + HOST +":" + port + "/change/" + id,entity, Object.class);
            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        }catch (HttpClientErrorException e){
            return new ResponseEntity<>(new ErrorResponse("Not found"),HttpStatus.NOT_FOUND);
        }
    }

    public String getPortWithMinimalLoad(){
        String portWithMinimalLoad = ports.get(0);
        int minLoad = restTemplate.getForObject("http://" + HOST +":" + ports.get(0) + "/getLoad", LoadResponse.class).getLoad();

        for (int i = 1; i < ports.size(); i++) {
            LoadResponse loadResponse = restTemplate.getForObject("http://" + HOST +":" + ports.get(i) + "/getLoad", LoadResponse.class);
            if(loadResponse.getLoad() < minLoad){
                minLoad = loadResponse.getLoad();
                portWithMinimalLoad = ports.get(i) ;
            }
        }
        if(minLoad >= maxLoad){
            return "NO_PORT";
        }

        System.out.println("Selected port with minimal load : " + portWithMinimalLoad);
        return portWithMinimalLoad;
    }
}

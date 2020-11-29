package lab2.cache;

import lab2.cache.classes.CacheServer;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CacheApp {
    static Logger logger = Logger.getLogger(CacheApp.class);
    public static void main(String[] args) throws IOException {
        List<CacheServer> cacheServers = new ArrayList<>();
        int serversAmount = 2;

        for(int i = 1; i <= serversAmount; i++){
            cacheServers.add(new CacheServer(4000 + i));
        }

        cacheServers.forEach(CacheServer::start);

        logger.info("Select server to stop with index 1 - " + serversAmount);
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while (true){}
    }
}

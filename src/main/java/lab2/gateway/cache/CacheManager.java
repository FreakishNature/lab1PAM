package lab2.gateway.cache;

import lab2.cache.classes.CacheClient;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CacheManager {
    Logger logger = Logger.getLogger(CacheManager.class);
    List<Cache> caches = new ArrayList<>();
    Random r = new Random();

    static CacheManager cacheManager;

    public CacheManager(int serverAmount) {
        for(int i = 1; i <= serverAmount; i++){
            caches.add(new Cache("localhost",4000+i));
        }
        cacheManager = this;
    }

    public static CacheManager getInstance(){
        return cacheManager;
    }

    public void remove(CacheClient cl){
        logger.info("deleted cache ");
        caches.removeIf(c -> c.client == cl);

    }

    public void killCache(int i) throws InterruptedException {
        if(i < caches.size()){
            logger.info("deleting cache");
            caches.get(i).client.sendMessage("EXIT");

        } else {
            logger.warn("there is no cache with such index");
        }
    }

    public void start(){
        caches.forEach(Cache::start);
    }

    public void lpush(String key, String value) throws InterruptedException, IOException {
        String command = "lpush " + key + " " + value;

        logger.info("Cache : " + command);

        for(Cache c : caches){
            c.lpush(key,value);
        }
    }

    public void auth(String username,String password) throws InterruptedException, IOException {
        String command = "auth " + username + " " + password;
        logger.info("Cache : " + command);

        for(Cache c : caches){
            c.auth(username,password);
        }
    }

    public String get(String key) throws InterruptedException {
        String command = "get " + key;
        logger.info("Cache : " + command);
        int i = r.nextInt(Integer.MAX_VALUE) % caches.size();
        logger.debug("Selected service : " + i);
        return caches.get(i).get(key);
    }

    public boolean isInCache(String key) throws InterruptedException {
        logger.info("Cache : isInCache");
        return !get(key).equals("null") && !get(key).equals("(empty array)");
    }

    public void lrem(String key,int count,String value) throws InterruptedException, IOException {
        String command = "lrem " + key + " " + count + " " + value;
        logger.info("Cache : " + command);

        for(Cache c : caches){
            c.lrem(key,count,value);
        }
    }

}

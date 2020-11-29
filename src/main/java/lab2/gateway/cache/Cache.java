package lab2.gateway.cache;

import lab2.cache.classes.CacheClient;

import java.io.IOException;

public class Cache {
    CacheClient client;

    public CacheClient getClient() {
        return client;
    }

    public Cache(String host, int port){
        this.client = new CacheClient(host,port);
    }

    public void start(){
        client.start();
    }

    public void lpush(String key,String value) throws InterruptedException, IOException {
        String command = "lpush< >" + key + "< >" + value;
        client.sendMessage(command);
        client.getReceivedMessage();
    }

    public void auth(String username,String password) throws InterruptedException, IOException {
        String command = "auth< >" + username + "< >" + password;
        client.sendMessage(command);
        client.getReceivedMessage();
    }

    public String get(String key) throws InterruptedException {
        String command = "get< >" + key;
        client.sendMessage(command);

        try {
            return client.getReceivedMessage();
        } catch (IOException e) {
            return get(key);
        }
    }

    public void lrem(String key,int count,String value) throws InterruptedException, IOException {
        String command = "lrem< >" + key + "< >" + count + "< >" + value;
        client.sendMessage(command);
        client.getReceivedMessage();
    }


}

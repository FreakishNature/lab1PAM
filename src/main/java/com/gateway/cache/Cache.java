package com.gateway.cache;

import com.cache.CacheClient;

public class Cache {
    CacheClient client;
    int sleepTime = 10;
    public Cache(CacheClient client){
        this.client = client;
    }

    public void lpush(String key,String value) throws InterruptedException {
        String command = "lpush " + key + " " + value;
        System.out.println("Cache : " + command);

        client.setMessage(command);
        Thread.sleep(sleepTime);
    }

    public void auth(String username,String password) throws InterruptedException {
        String command = "auth " + username + " " + password;
        System.out.println("Cache : " + command);

        client.setMessage(command);
        Thread.sleep(sleepTime);
    }

    public String get(String key) throws InterruptedException {
        String command = "get " + key;
        System.out.println("Cache : " + command);

        client.setMessage(command);
        Thread.sleep(sleepTime);
        return client.getReturnedMessage();
    }

    public void lrem(String key,int count,String value) throws InterruptedException {
        String command = "lrem " + key + " " + count + " " + value;
        System.out.println("Cache : " + command);

        client.setMessage(command);
        Thread.sleep(sleepTime);
    }


}

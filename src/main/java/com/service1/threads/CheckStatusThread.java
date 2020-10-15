package com.service1.threads;

import com.database.Connection;
import com.model.DataResponse;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;

public class CheckStatusThread extends Thread {
    CopyOnWriteArrayList<String> queue;
    int processingTime;
    Connection sqlConnection;
    String task;
    int id;

    @Override
    public void run() {
        try {
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        if(queue.contains(task)){
            try {
                sqlConnection.changeStatus(id,"FAILED");

            } catch (IOException e) {
                e.printStackTrace();
            }

            queue.remove(task);
        }
    }

    public CheckStatusThread(Connection sqlConnection, CopyOnWriteArrayList<String> queue, int id, String task, int processingTime){
        this.queue = queue;
        this.id = id;
        this.sqlConnection = sqlConnection;
        this.task = task;
        this.processingTime = processingTime;
    }
}

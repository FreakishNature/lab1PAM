package com.service1.threads;

import com.database.Connection;
import com.database.SqlConnection;

import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.CopyOnWriteArrayList;

public class ChangeStatusThread extends Thread {
    CopyOnWriteArrayList<String> queue;
    Connection sqlConnection;
    int id;
    String task;
    int processingTime;

    @Override
    public void run() {
        queue.add(task);

        try {
            Thread.sleep(processingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        if(queue.contains(task)){
            try {
                sqlConnection.changeStatus(id,"COMPLETED");
            } catch (IOException e) {
                e.printStackTrace();
            }
            queue.remove(task);
        }

    }

    public ChangeStatusThread(Connection sqlConnection, CopyOnWriteArrayList<String> queue, int id, String task, int processingTime){
        this.queue = queue;
        this.id = id;
        this.sqlConnection = sqlConnection;
        this.task = task;
        this.processingTime = processingTime;
    }
}

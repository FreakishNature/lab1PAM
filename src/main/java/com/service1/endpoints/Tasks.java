package com.service1.endpoints;

import com.database.Connection;
import com.database.NosqlConnection;
import com.database.SqlConnection;
import com.model.CreateDataRequest;
import com.model.ErrorResponse;
import com.model.LoadResponse;
import com.model.DataResponse;
import com.service1.threads.ChangeStatusThread;
import com.service1.threads.CheckStatusThread;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Timed;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.sql.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
public class Tasks {
    final String HOST = "localhost";

    Connection sqlConnection = new NosqlConnection();

    {
        if (sqlConnection instanceof NosqlConnection) {
            ((NosqlConnection) sqlConnection).establishConnection("localhost:27017");
            ((NosqlConnection) sqlConnection).selectCollection("data");
        }
    }

    int changeProcessingTime = 2_000;
    int checkProcessingTime = 3_000;

    CopyOnWriteArrayList<String> queue = new CopyOnWriteArrayList<>();

    String createTask() {
        return new Date().getTime() + "";
    }


    @GetMapping("/getLoad")
    public LoadResponse getLoad() {
        return new LoadResponse(queue.size());
    }

    @PutMapping("/processingTime")
    public void processingTime(@RequestParam int changeProcessingTime,
                               @RequestParam int checkProcessingTime) {
        this.changeProcessingTime = changeProcessingTime;
        this.checkProcessingTime = checkProcessingTime;
    }

    @PutMapping("/changeDatabase")
    public Object changeDatabase(@RequestParam String db) {
        if(db.equals("nosql")){
            sqlConnection = new NosqlConnection();
            ((NosqlConnection) sqlConnection).establishConnection("localhost:27017");
            ((NosqlConnection) sqlConnection).selectCollection("data");
        } else if(db.equals("sql")){
            sqlConnection = new SqlConnection();
        } else {
            return new ResponseEntity<>(new ErrorResponse("Only available values for db are 'nosql','sql'"),HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }


    @PostMapping("/create")
    public ResponseEntity<Object> create(@RequestBody CreateDataRequest request) throws InterruptedException {

        Thread.sleep(3_000);
        String task = createTask();
        int id = sqlConnection.createData(request.getData());

        new ChangeStatusThread(sqlConnection, queue, id, task, changeProcessingTime).start();
        new CheckStatusThread(sqlConnection, queue, id, task, checkProcessingTime).start();

        return new ResponseEntity<>(new DataResponse(id, request.getData(), "IN PROGRESS"), HttpStatus.CREATED);
    }

    @PutMapping("/change/{id}")
    public ResponseEntity<Object> change(@PathVariable int id, @RequestBody CreateDataRequest request) throws InterruptedException, SQLException, IOException {
        if (sqlConnection.changeData(id, request.getData()) == 1) {

            String task = createTask();
            new ChangeStatusThread(sqlConnection, queue, id, task, changeProcessingTime).start();
            new CheckStatusThread(sqlConnection, queue, id, task, checkProcessingTime).start();

            return new ResponseEntity<>(HttpStatus.ACCEPTED);
        } else {
            return new ResponseEntity<>(new ErrorResponse("Not found"), HttpStatus.NOT_FOUND);
        }


    }

    @GetMapping("/getStatus/{id}")
    public ResponseEntity<Object> getStatus(@PathVariable int id) throws InterruptedException, SQLException {
        DataResponse response = null;
        try {
            response = sqlConnection.getData(id);
            System.out.println(response);
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (IOException e) {
            return new ResponseEntity<>(new ErrorResponse("Not found"), HttpStatus.NOT_FOUND);
        }
    }

}

package com.database;

import com.model.DataResponse;

import java.io.IOException;

public interface Connection {
    int changeData(int id,String data) throws IOException;
    void changeStatus(int id,String status) throws IOException;
    int createData(String data);
    DataResponse getData(int id) throws IOException;
}

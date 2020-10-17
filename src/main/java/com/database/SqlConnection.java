package com.database;

import com.model.DataResponse;

import java.io.IOException;
import java.sql.*;
import java.sql.Connection;

public class SqlConnection implements com.database.Connection {
    private static Connection con;
    private static Statement stmt;
    private static ResultSet rs;
    private static final String url = "jdbc:mysql://" + com.gateway.endpoints.Connection.HOST + "/lab1sql";
    private static final String user = "root";
    private static final String password = "";


    ResultSet executeSql(String sql) throws SQLException {
        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
        rs = stmt.executeQuery(sql);
        return rs;
    }

    int updateSql(String sql) throws SQLException {
        con = DriverManager.getConnection(url, user, password);
        stmt = con.createStatement();
        return stmt.executeUpdate(sql);
    }

    public DataResponse getData(int id) throws IOException {
        try {

            ResultSet rs = executeSql("SELECT * FROM `data` WHERE `id`=" + id);
            if(rs.next()){
                return new DataResponse(rs.getInt(1),rs.getString(2),rs.getString(3));
            }
            throw new IOException();

        } catch (SQLException e) {
            e.printStackTrace();
        }

         return null;
    }

    public int createData(String data)  {
       try {
           updateSql("insert into data (someData, status) values ('" + data + "', 'IN PROGRESS' )");

           ResultSet rs = executeSql("SELECT MAX(id) AS LastID FROM data");

           rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int changeData(int id,String data) throws IOException {

        try {
            return updateSql("update data set someData='" + data + "', status='IN PROGRESS' where id=" + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new IOException();
    }
    public void changeStatus(int id,String status){
        try {
            updateSql("update data set status='" + status + "' where id=" + id);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}

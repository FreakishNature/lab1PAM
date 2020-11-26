package lab2.database;

import com.model.DataResponse;

import java.io.IOException;
import java.sql.*;

public class SqlLab2 {
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    private final String url = "jdbc:mysql://" + com.gateway.endpoints.Connection.HOST + "/lab1sql";
    private final String user = "root";
    private final String password = "";

    String tableName = "Orders";
    SqlLab2() throws SQLException {
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + tableName
                + "  (id int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "name           VARCHAR(10),"
                + "   quantity       INTEGER,"
                + "   price          FLOAT,"
                + "   instruction    VARCHAR(10),"
                + "   status     VARCHAR(15))";

        con = DriverManager.getConnection(url, user, password);
        Statement stmt = con.createStatement();
        stmt.execute(sqlCreate);
    }

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

            ResultSet rs = executeSql("SELECT * FROM `" + tableName + "` WHERE `id`=" + id);
            if(rs.next()){
                return new DataResponse(rs.getInt(1),rs.getString(2),rs.getString(3));
            }
            throw new IOException();

        } catch (SQLException e) {
            e.printStackTrace();
        }

         return null;
    }

    public int createOrder(String name,String instruction,float price, int quantity)  {
       try {
           updateSql("insert into " + tableName + " (name,instruction,price,quantity, status) values ('" + name  + "','" + instruction +"','" + price + "','" + quantity + "', 'IN PROGRESS' )");

           ResultSet rs = executeSql("SELECT MAX(id) AS LastID FROM " + tableName);

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

    public static void main(String[] args) throws SQLException {
        SqlLab2 sql = new SqlLab2();
        sql.createOrder("AAPl","SELL",1000,20);
    }
}

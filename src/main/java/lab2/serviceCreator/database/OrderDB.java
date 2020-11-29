package lab2.serviceCreator.database;

import lab2.serviceCreator.model.OrderResponse;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDB {
    static Logger logger = Logger.getLogger(OrderDB.class);
    private Connection con;
    private Statement stmt;
    private ResultSet rs;
    private static final String url = "jdbc:mysql://" + "localhost" ;
    private static final String user = "root";
    private static final String password = "";
    String dbName;
    String tableName = "Orders";

    public OrderDB(String dbName) throws SQLException {
        this.dbName = dbName;
        createTableIfNotExists(dbName,tableName);
    }

    ResultSet executeSql(String sql) throws SQLException {
        con = DriverManager.getConnection(url + "/" + dbName, user, password);
        stmt = con.createStatement();
        rs = stmt.executeQuery(sql);
        return rs;
    }

    int updateSql(String sql) throws SQLException {
        con = DriverManager.getConnection(url + "/" + dbName, user, password);
        stmt = con.createStatement();
        return stmt.executeUpdate(sql);
    }

    public OrderResponse getOrder(int id) throws IOException {
        try {
            String command = "SELECT id,name,instruction,price,quantity,status FROM `" + tableName + "` WHERE `id`=" + id;
            logger.debug(command);
            ResultSet rs = executeSql(command);
            if(rs.next()){
                return new OrderResponse(rs.getInt("id"),rs.getString("instruction"),rs.getString("name"),rs.getFloat("price"),rs.getInt("quantity"),rs.getString("status"));
            }
            throw new IOException();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
    public List<OrderResponse> getOrdersAscByPrice(String name) throws IOException {
        try {
            String command = "SELECT id,name,instruction,price,quantity,status FROM `" + tableName + "` WHERE name='" + name + "' AND instruction='SELL' ORDER BY price ASC";
            logger.debug(command);
            ResultSet rs = executeSql(command);
            List<OrderResponse> orderResponses = new ArrayList<>();
            while (rs.next()){
                orderResponses.add(new OrderResponse(rs.getInt(1),rs.getString(2),rs.getString(3),rs.getFloat(4),rs.getInt(5),rs.getString(6)));
            }
            return orderResponses;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public int createOrder(String name,String instruction,float price, int quantity)  {
       try {
           String sqlCommand = "insert into " + tableName + " (name,instruction,price,quantity, status) values ('" + name  + "','" + instruction +"','" + price + "','" + quantity + "', 'IN PROGRESS' )";
           logger.debug(sqlCommand);
           updateSql(sqlCommand);

           ResultSet rs = executeSql("SELECT MAX(id) AS LastID FROM " + tableName);

           rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int changeQuantity(int id,int quantity) throws IOException {

        try {
            String command = "update " + tableName + " set quantity='" + quantity + "', status='IN PROGRESS' where id=" + id;
            logger.debug(command);
            return updateSql(command);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new IOException();
    }

    public void changeStatus(int id,String status){
        try {
            String command = "update " + tableName + " set status='" + status + "' where id=" + id;
            logger.debug(command);
            updateSql(command);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void createDatabaseIfNotExist(String db){
        try {
            Connection conn = DriverManager.getConnection(url+"/", user, password);
            Statement s = conn.createStatement();
            String command = "CREATE DATABASE "+db;
            logger.debug(command);
            int Result = s.executeUpdate(command);
            System.out.println("database created successfully");

        } catch (SQLException e) {
            if(e.getMessage().contains("database exists")){
                System.out.println(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
    }

    public static void dropDatabaseIfExists(String db){
        Connection con;
        Statement stmt;

        try{
            con = DriverManager.getConnection(url+"/"+db, user, password);
            try{
                stmt = con.createStatement();
                String query = "DROP DATABASE " + db;
                logger.debug(query);
                stmt.executeUpdate(query);
                logger.debug("DATABASE deleted successfully...");
            } catch(SQLException s){
                logger.error("Database not deleted ");
            }
            // close Connection
            con.close();
        }catch (Exception e){
            if(e.getMessage().contains("Unknown database")){
                logger.error(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
    }
    public static void copyDatabase(String fromDb, String toDb, String tableName){
        logger.debug("Copy data from one database table to another");
        Connection conn = null;
        try {

            dropDatabaseIfExists(toDb);
            createDatabaseIfNotExist(toDb);

            conn = DriverManager.getConnection(url+"/"+toDb,user,password);
            Statement st = conn.createStatement();
            //Copy table
            createTableIfNotExists(toDb,tableName);
            String command = "INSERT INTO " + tableName + " SELECT * FROM " + fromDb + "." + tableName;
            logger.debug(command);
            int rows = st.executeUpdate(command);
            if (rows != 0){
                conn.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createTableIfNotExists(String dbName,String tableName){
        String sqlCreate = "CREATE TABLE IF NOT EXISTS " + tableName
                + "  (id int NOT NULL AUTO_INCREMENT PRIMARY KEY,"
                + "   name           VARCHAR(10),"
                + "   quantity       INTEGER,"
                + "   price          FLOAT,"
                + "   instruction    VARCHAR(10),"
                + "   status     VARCHAR(15))";

        try {
            Statement stmt = DriverManager.getConnection(url + "/" + dbName, user, password).createStatement();
            stmt.execute(sqlCreate);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static List<String> getAllDBs(String startingWith){

        //Registering the Driver
        //Getting the connection
        List<String> orderDBS = new ArrayList<>();

        String mysqlUrl = "jdbc:mysql://localhost/orderdb_1";
        Connection con = null;
        try {
            con = DriverManager.getConnection(mysqlUrl, "root", "");
            //Creating a Statement object
            Statement stmt = con.createStatement();
            //Retrieving the data
            ResultSet rs = stmt.executeQuery("Show Databases");
            while(rs.next()) {
                String dbName = rs.getString(1);
                if(dbName.contains(startingWith + "_")){
                    orderDBS.add(dbName);
                }
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }

        return orderDBS;
    }

    public static void main(String[] args) throws SQLException, IOException {
        System.out.println(getAllDBs("orderdb"));
    }
}

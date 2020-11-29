package lab2.serviceCreator.database;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    List<OrderDB> sqlLab2s = new ArrayList<>();

    public DBManager(int amountOfDBs){
        OrderDB.getAllDBs("orderdb").forEach( s -> {
            try {
                sqlLab2s.add(new OrderDB(s));
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        });
    }

    public int changeQuantity(int id,int quantity) throws IOException {
        int result = 0;
        for(OrderDB db : sqlLab2s) {
            try {
               result = db.changeQuantity(id,quantity);
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        return result;
    }
    public int createOrder(String name,String instruction,float price, int quantity)  {
        int result = 0;
        for(OrderDB db : sqlLab2s) {
            result = db.createOrder(name,instruction,price,quantity);
        }

        return result;
    }

    public void changeStatus(int id,String status){
        sqlLab2s.forEach( db -> {
            db.changeStatus(id,status);
        });
    }
}

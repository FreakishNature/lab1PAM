package lab2.serviceCreator.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Order extends CreateOrderRequest{
    int id;
    String status;
}

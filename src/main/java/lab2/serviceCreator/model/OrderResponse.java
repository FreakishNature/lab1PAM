package lab2.serviceCreator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse extends CreateOrderRequest{
    int id;
    String status;
    String instruction;

    public OrderResponse( int id, String instruction, String item, float price, int quantity,  String status) {
        super(item, price, quantity);
        this.instruction = instruction;
        this.id = id;
        this.status = status;
    }

    public OrderResponse(OrderResponse orderResponse) {
        super(orderResponse.item, orderResponse.price, orderResponse.remainingQuantity);
        this.id = orderResponse.id;
        this.status = orderResponse.status;
        this.instruction = orderResponse.instruction;
    }

    @Override
    public String toString() {
        return "OrderResponse{" +
                "id=" + id +
                ", status='" + status + '\'' +
                ", item='" + item + '\'' +
                ", price=" + price +
                ", quantity=" + remainingQuantity +
                ", instruction='" + instruction + '\'' +
                '}';
    }
}

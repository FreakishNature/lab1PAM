package lab2.serviceCreator.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CreateOrderRequest {
    String item;
    float price;
    int remainingQuantity;
}

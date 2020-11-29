package lab2.serviceCreator.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ChangeQuantityAndStatusRequest {
    int id;
    String status;
    int quantity;
}

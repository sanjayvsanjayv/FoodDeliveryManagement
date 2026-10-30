package com.fooddelivery.payment;
import java.math.BigDecimal;
public class CODPayment implements PaymentMethod {
    public boolean pay(BigDecimal amount) {
        System.out.println("Cash on Delivery payment simulated for Rs. " + amount.toPlainString());
        return true;
    }
    public String getName() { return "Cash on Delivery"; }
}

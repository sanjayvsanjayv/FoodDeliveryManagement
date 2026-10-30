package com.fooddelivery.payment;
import java.math.BigDecimal;
public class UPIPayment implements PaymentMethod {
    public boolean pay(BigDecimal amount) {
        System.out.println("UPI payment simulated for Rs. " + amount.toPlainString());
        return true;
    }
    public String getName() { return "UPI"; }
}

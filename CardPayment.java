package com.fooddelivery.payment;
import java.math.BigDecimal;
public class CardPayment implements PaymentMethod {
    public boolean pay(BigDecimal amount) {
        System.out.println("Card payment simulated for Rs. " + amount.toPlainString());
        return true;
    }
    public String getName() { return "Card"; }
}

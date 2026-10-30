package com.fooddelivery.repository;

import com.fooddelivery.model.*;
import java.util.*;

public class PaymentRepository {
    private final List<Payment> data = new ArrayList<>();

    public void save(Payment payment) {
        data.add(payment);
    }

    public List<Payment> findAll() {
        return new ArrayList<>(data);
    }

    public Payment findByOrderId(int id) {
        for (Payment payment : data) {
            if (payment.getOrderId() == id) {
                return payment;
            }
        }

        return null;
    }
}

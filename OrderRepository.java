package com.fooddelivery.repository;

import com.fooddelivery.model.*;
import java.util.*;

public class OrderRepository {
    private final List<Order> data = new ArrayList<>();

    public void save(Order order) {
        data.add(order);
    }

    public List<Order> findAll() {
        return new ArrayList<>(data);
    }

    public Order findById(int id) {
        for (Order order : data) {
            if (order.getId() == id) {
                return order;
            }
        }

        return null;
    }
}

package com.fooddelivery.repository;

import com.fooddelivery.model.*;
import java.util.*;

public class CustomerRepository {
    private final List<Customer> data = new ArrayList<>();

    public void save(Customer customer) {
        for (Customer existingCustomer : data) {
            if (existingCustomer.getPhone().equals(customer.getPhone())) {
                throw new IllegalArgumentException("Phone number already registered.");
            }
        }

        data.add(customer);
    }

    public Customer findById(int id) {
        for (Customer customer : data) {
            if (customer.getId() == id) {
                return customer;
            }
        }

        return null;
    }

    public List<Customer> findAll() {
        return new ArrayList<>(data);
    }
}

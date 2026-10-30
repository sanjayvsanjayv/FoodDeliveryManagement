package com.fooddelivery.repository;

import com.fooddelivery.model.*;
import java.util.*;

public class RestaurantRepository {
    private final List<Restaurant> data = new ArrayList<>();

    public void save(Restaurant restaurant) {
        data.add(restaurant);
    }

    public List<Restaurant> findAll() {
        return new ArrayList<>(data);
    }

    public Restaurant findById(int id) {
        for (Restaurant restaurant : data) {
            if (restaurant.getId() == id) {
                return restaurant;
            }
        }

        return null;
    }
}

package com.fooddelivery.repository;

import com.fooddelivery.model.*;
import java.util.*;

public class ReviewRepository {
    private final List<Review> data = new ArrayList<>();

    public void save(Review review) {
        for (Review existingReview : data) {
            if (existingReview.getOrderId() == review.getOrderId()) {
                throw new IllegalArgumentException("This order has already been reviewed.");
            }
        }

        data.add(review);
    }

    public List<Review> findAll() {
        return new ArrayList<>(data);
    }
}

package com.fooddelivery.repository;

import com.fooddelivery.model.*;
import java.util.*;

public class DeliveryRepository {
    private final List<DeliveryPartner> data = new ArrayList<>();

    public void save(DeliveryPartner partner) {
        data.add(partner);
    }

    public List<DeliveryPartner> findAll() {
        return new ArrayList<>(data);
    }
}

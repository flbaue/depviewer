package com.example.infra;

import com.example.domain.Order;
import com.example.domain.OrderRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryOrderRepository implements OrderRepository {
    private final List<Order> orders = new ArrayList<>();

    @Override
    public void save(Order order) {
        orders.add(order);
    }
}

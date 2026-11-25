package com.example.service;

import com.example.domain.Order;
import com.example.domain.OrderRepository;
import com.example.domain.Status;

import java.util.List;
import java.util.function.Function;

public class OrderService {
    private final OrderRepository repository;

    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    public Order place(String customer) {
        Order order = new Order(customer);
        order.setStatus(Status.PLACED);
        repository.save(order);
        return order;
    }

    public List<Double> totals(List<Order> orders) {
        Function<Order, Double> f = Order::total;
        return orders.stream().map(f).toList();
    }
}

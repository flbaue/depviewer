package com.example.app;

import com.example.domain.Order;
import com.example.infra.InMemoryOrderRepository;
import com.example.service.OrderService;

public class Main {
    public static void main(String[] args) {
        OrderService service = new OrderService(new InMemoryOrderRepository());
        Order order = service.place("alice");
        System.out.println(order.total());
    }
}

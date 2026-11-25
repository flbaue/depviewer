package com.example.domain;

import com.example.service.PriceCalculator;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private final String customer;
    private final List<Line> lines = new ArrayList<>();
    private Status status = Status.NEW;

    public Order(String customer) {
        this.customer = customer;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // Deliberate upward dependency: domain -> service closes a package cycle.
    public double total() {
        double net = 0;
        for (Line line : lines) net += line.amount;
        return PriceCalculator.gross(net);
    }

    public static class Line {
        final double amount;

        Line(double amount) {
            this.amount = amount;
        }
    }
}

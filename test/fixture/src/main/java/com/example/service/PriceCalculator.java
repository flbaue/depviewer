package com.example.service;

public final class PriceCalculator {
    public static final double TAX = 0.19;

    private PriceCalculator() {
        super(); // explicit call to Object() must not become a dependency
    }

    public static double gross(double net) {
        return net * (1 + TAX);
    }
}

package com.example.domain;

import org.junit.jupiter.api.Test; // deliberately unresolved: no test libraries on the classpath

class OrderTest {
    @Test
    void totalIsZeroForNewOrder() {
        Order order = new Order("bob");
        assert order.total() == 0;
    }
}

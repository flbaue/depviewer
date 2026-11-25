package com.example.domain;

import org.junit.jupiter.api.Test; // deliberately unresolved: no test libraries on the classpath

import static org.junit.jupiter.api.Assertions.assertEquals; // unresolved static import

class OrderTest {
    @Test
    void totalIsZeroForNewOrder() {
        Order order = new Order("bob");
        assertEquals(0.0, order.total());
    }
}

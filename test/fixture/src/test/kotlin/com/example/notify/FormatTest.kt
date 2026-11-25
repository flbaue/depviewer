package com.example.notify

import com.example.domain.Order
import com.example.domain.Status
import org.junit.jupiter.api.Test   // deliberately unresolved, like the Java test

class FormatTest {
    @Test
    fun formatsTotal() {
        val text = format(OrderEvent(Order("carol"), Status.NEW), 12.5)
        check(text.contains("12.5"))
    }
}

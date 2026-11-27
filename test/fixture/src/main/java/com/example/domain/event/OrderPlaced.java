package com.example.domain.event;

import com.example.domain.Order;

/** A sub-package of com.example.domain, for package:com.example.domain.** (FR-027). */
public record OrderPlaced(Order order) {
}

package com.example.app;

import com.example.domain.Order;
import com.example.domain.Status;
import com.example.infra.InMemoryOrderRepository;
import com.example.notify.EmailNotifier;
import com.example.notify.NotificationService;
import com.example.notify.NotificationsKt;
import com.example.notify.OrderEvent;
import com.example.service.OrderService;

public class Main {
    public static void main(String[] args) {
        OrderService service = new OrderService(new InMemoryOrderRepository());
        Order order = service.place("alice");
        System.out.println(order.total());
        // Java -> Kotlin (javac cannot see these; depviewer matches them by name)
        NotificationService.INSTANCE.register(new EmailNotifier("shop"));
        System.out.println(NotificationsKt.format(new OrderEvent(order, Status.PLACED), order.total()));
    }
}

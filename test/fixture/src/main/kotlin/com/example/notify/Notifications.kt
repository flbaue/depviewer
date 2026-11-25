package com.example.notify

import com.example.domain.Order
import com.example.domain.Status as OrderStatus

/** Something that can deliver a message. */
interface Notifier {
    fun send(message: String)
}

class EmailNotifier(private val sender: String) : Notifier {
    override fun send(message: String) {
        println("$sender: $message")
    }
}

data class OrderEvent(val order: Order, val status: OrderStatus)

object NotificationService {
    private val notifiers = mutableListOf<Notifier>()

    fun register(notifier: Notifier) {
        notifiers += notifier
    }

    fun publish(event: OrderEvent) {
        val total = event.order.total()   // Kotlin -> Java member, through a property chain
        notifiers.forEach { it.send(format(event, total)) }
    }
}

// Top-level function: Java sees it as NotificationsKt.format(...)
fun format(event: OrderEvent, total: Double): String = "Order ${event.status}: $total"

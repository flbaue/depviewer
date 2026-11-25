package com.example.notify

enum class Channel(val label: String) {
    EMAIL("e-mail"),
    SMS("text message");

    fun describe(): String = "via $label"

    companion object {
        fun default(): Channel = EMAIL
    }
}

fun Channel.isInstant(): Boolean = this == Channel.SMS

package com.example.model

enum class LogoPreset(val label: String) {
    NONE("None"),
    WIFI("Wi-Fi"),
    LINK("Link"),
    SECURITY("Shield"),
    CONTACT("Contact"),
    EMAIL("Email"),
    STAR("Star"),
    HEART("Heart"),
    SHOPPING("Store"),
    LOCATION("Pin"),
    CUSTOM("Custom Photo")
}

enum class LogoShape(val label: String) {
    ROUNDED_RECT("Rounded"),
    CIRCLE("Circle"),
    SQUARE("Square"),
    NONE("No Frame")
}

enum class LogoSize(val label: String, val ratio: Float) {
    COMPACT("Small (18%)", 0.18f),
    STANDARD("Standard (22%)", 0.22f),
    LARGE("Large (26%)", 0.26f)
}

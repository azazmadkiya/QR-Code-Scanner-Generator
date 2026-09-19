package com.example.model

enum class QrType(val label: String) {
    TEXT("Plain Text"),
    URL("Website URL"),
    WIFI("Wi-Fi Network"),
    CONTACT("Contact Card"),
    PHONE("Phone Number"),
    SMS("SMS Message"),
    EMAIL("Email Address"),
    GEO("Geo Location"),
    ENCRYPTED("Encrypted Data");

    companion object {
        fun fromString(type: String): QrType {
            return entries.firstOrNull { it.name.equals(type, ignoreCase = true) } ?: TEXT
        }
    }
}

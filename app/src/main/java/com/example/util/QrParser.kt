package com.example.util

import com.example.model.ParsedQrResult
import com.example.model.QrType

object QrParser {
    fun parse(raw: String): ParsedQrResult {
        val trimmed = raw.trim()

        if (CryptoManager.isEncryptedPayload(trimmed)) {
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.ENCRYPTED,
                displayTitle = "Encrypted Secret Payload",
                details = mapOf("Algorithm" to "AES-256-GCM", "Status" to "Password Required"),
                isEncrypted = true
            )
        }

        if (trimmed.startsWith("http://", ignoreCase = true) ||
            trimmed.startsWith("https://", ignoreCase = true) ||
            trimmed.startsWith("www.", ignoreCase = true)
        ) {
            val url = if (trimmed.startsWith("www.", ignoreCase = true)) "https://$trimmed" else trimmed
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.URL,
                displayTitle = trimmed.substringBefore("?").take(40),
                details = mapOf("URL" to url)
            )
        }

        if (trimmed.startsWith("WIFI:", ignoreCase = true)) {
            val details = parseWifi(trimmed)
            val ssid = details["SSID"] ?: "Wi-Fi Network"
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.WIFI,
                displayTitle = ssid,
                details = details
            )
        }

        if (trimmed.startsWith("BEGIN:VCARD", ignoreCase = true) || trimmed.startsWith("MECARD:", ignoreCase = true)) {
            val details = parseContact(trimmed)
            val name = details["Name"] ?: "Contact Card"
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.CONTACT,
                displayTitle = name,
                details = details
            )
        }

        if (trimmed.startsWith("tel:", ignoreCase = true)) {
            val phone = trimmed.substring(4)
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.PHONE,
                displayTitle = phone,
                details = mapOf("Phone" to phone)
            )
        }

        if (trimmed.startsWith("smsto:", ignoreCase = true) || trimmed.startsWith("sms:", ignoreCase = true)) {
            val parts = trimmed.substringAfter(":").split(":", limit = 2)
            val number = parts.getOrNull(0) ?: ""
            val message = parts.getOrNull(1) ?: ""
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.SMS,
                displayTitle = "SMS to $number",
                details = mapOf("Number" to number, "Message" to message)
            )
        }

        if (trimmed.startsWith("mailto:", ignoreCase = true)) {
            val email = trimmed.substring(7).substringBefore("?")
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.EMAIL,
                displayTitle = email,
                details = mapOf("Email" to email)
            )
        }

        if (trimmed.startsWith("geo:", ignoreCase = true)) {
            val coords = trimmed.substring(4).substringBefore("?")
            return ParsedQrResult(
                rawContent = trimmed,
                type = QrType.GEO,
                displayTitle = "Location: $coords",
                details = mapOf("Coordinates" to coords)
            )
        }

        val firstLine = trimmed.lines().firstOrNull() ?: trimmed
        return ParsedQrResult(
            rawContent = trimmed,
            type = QrType.TEXT,
            displayTitle = if (firstLine.length > 35) firstLine.take(35) + "…" else firstLine,
            details = mapOf("Text" to trimmed)
        )
    }

    private fun parseWifi(wifiString: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val content = wifiString.removePrefix("WIFI:").removePrefix("wifi:").removeSuffix(";;").removeSuffix(";")
        val tokens = content.split(";")
        for (token in tokens) {
            val colonIndex = token.indexOf(':')
            if (colonIndex != -1) {
                val key = token.substring(0, colonIndex).uppercase()
                val value = token.substring(colonIndex + 1)
                when (key) {
                    "S" -> map["SSID"] = value
                    "P" -> map["Password"] = value
                    "T" -> map["Security"] = if (value.isBlank()) "None" else value
                    "H" -> map["Hidden"] = if (value.equals("true", true)) "Yes" else "No"
                }
            }
        }
        return map
    }

    private fun parseContact(vcard: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        if (vcard.startsWith("MECARD:", ignoreCase = true)) {
            val body = vcard.removePrefix("MECARD:").removeSuffix(";")
            val items = body.split(";")
            for (item in items) {
                val colon = item.indexOf(':')
                if (colon != -1) {
                    val k = item.substring(0, colon).uppercase()
                    val v = item.substring(colon + 1)
                    when (k) {
                        "N" -> map["Name"] = v.replace(",", " ")
                        "TEL" -> map["Phone"] = v
                        "EMAIL" -> map["Email"] = v
                        "ORG" -> map["Organization"] = v
                        "NOTE" -> map["Note"] = v
                    }
                }
            }
            return map
        }

        for (line in vcard.lines()) {
            val trimmed = line.trim()
            if (trimmed.startsWith("FN:", ignoreCase = true)) {
                map["Name"] = trimmed.substring(3)
            } else if (trimmed.startsWith("TEL", ignoreCase = true)) {
                map["Phone"] = trimmed.substringAfter(":")
            } else if (trimmed.startsWith("EMAIL", ignoreCase = true)) {
                map["Email"] = trimmed.substringAfter(":")
            } else if (trimmed.startsWith("ORG:", ignoreCase = true)) {
                map["Organization"] = trimmed.substring(4)
            } else if (trimmed.startsWith("TITLE:", ignoreCase = true)) {
                map["Title"] = trimmed.substring(6)
            }
        }
        return map
    }

    fun buildWifiString(ssid: String, password: String, type: String, hidden: Boolean): String {
        val safeSsid = escapeWifi(ssid)
        val safePass = escapeWifi(password)
        val auth = when (type.uppercase()) {
            "WPA/WPA2", "WPA", "WPA2", "WPA3" -> "WPA"
            "WEP" -> "WEP"
            else -> "nopass"
        }
        return "WIFI:S:$safeSsid;T:$auth;P:$safePass;H:$hidden;;"
    }

    private fun escapeWifi(str: String): String {
        return str.replace("\\", "\\\\")
            .replace(";", "\\;")
            .replace(",", "\\,")
            .replace(":", "\\:")
    }

    fun buildVCard(name: String, phone: String, email: String, org: String): String {
        return buildString {
            appendLine("BEGIN:VCARD")
            appendLine("VERSION:3.0")
            if (name.isNotBlank()) appendLine("FN:$name")
            if (phone.isNotBlank()) appendLine("TEL:$phone")
            if (email.isNotBlank()) appendLine("EMAIL:$email")
            if (org.isNotBlank()) appendLine("ORG:$org")
            append("END:VCARD")
        }
    }
}

package com.example.model

data class ParsedQrResult(
    val rawContent: String,
    val type: QrType,
    val displayTitle: String,
    val details: Map<String, String> = emptyMap(),
    val isEncrypted: Boolean = false
)

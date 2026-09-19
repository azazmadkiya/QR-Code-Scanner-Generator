package com.example.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import com.example.model.LogoShape
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.util.EnumMap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object QrCodeGenerator {

    enum class ContrastLevel {
        HIGH,
        MODERATE,
        LOW
    }

    fun generateBitmap(
        content: String,
        size: Int = 600,
        foregroundColor: Int = Color.BLACK,
        backgroundColor: Int = Color.WHITE,
        errorCorrectionLevel: ErrorCorrectionLevel = ErrorCorrectionLevel.M,
        margin: Int = 2,
        logoBitmap: Bitmap? = null,
        logoSizeRatio: Float = 0.22f,
        logoShape: LogoShape = LogoShape.ROUNDED_RECT,
        logoBadgeColor: Int = backgroundColor,
        logoBorderColor: Int? = null
    ): Bitmap? {
        if (content.isEmpty()) return null
        return try {
            // When logo overlay is present, ensure high error correction so scanning remains reliable
            val effectiveEcLevel = if (logoBitmap != null) {
                if (errorCorrectionLevel == ErrorCorrectionLevel.L || errorCorrectionLevel == ErrorCorrectionLevel.M) {
                    ErrorCorrectionLevel.H
                } else {
                    errorCorrectionLevel
                }
            } else {
                errorCorrectionLevel
            }

            val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java).apply {
                put(EncodeHintType.CHARACTER_SET, "UTF-8")
                put(EncodeHintType.ERROR_CORRECTION, effectiveEcLevel)
                put(EncodeHintType.MARGIN, margin)
            }

            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
            val width = bitMatrix.width
            val height = bitMatrix.height
            val pixels = IntArray(width * height)

            for (y in 0 until height) {
                val offset = y * width
                for (x in 0 until width) {
                    pixels[offset + x] = if (bitMatrix.get(x, y)) foregroundColor else backgroundColor
                }
            }

            val qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                setPixels(pixels, 0, width, 0, 0, width, height)
            }

            if (logoBitmap == null) {
                return qrBitmap
            }

            // Overlay logo onto QR code
            overlayLogo(
                baseQr = qrBitmap,
                logo = logoBitmap,
                size = size,
                logoRatio = logoSizeRatio.coerceIn(0.12f, 0.30f),
                shape = logoShape,
                badgeColor = logoBadgeColor,
                borderColor = logoBorderColor ?: foregroundColor
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun overlayLogo(
        baseQr: Bitmap,
        logo: Bitmap,
        size: Int,
        logoRatio: Float,
        shape: LogoShape,
        badgeColor: Int,
        borderColor: Int
    ): Bitmap {
        val output = baseQr.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(output)

        val targetLogoSize = (size * logoRatio).toInt()
        val badgePadding = (targetLogoSize * 0.16f).toInt()
        val badgeSize = targetLogoSize + badgePadding * 2

        val cx = size / 2f
        val cy = size / 2f

        val badgeRect = RectF(
            cx - badgeSize / 2f,
            cy - badgeSize / 2f,
            cx + badgeSize / 2f,
            cy + badgeSize / 2f
        )

        // 1. Draw badge background if shape is not NONE
        if (shape != LogoShape.NONE) {
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = badgeColor
                style = Paint.Style.FILL
            }

            val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = borderColor
                style = Paint.Style.STROKE
                strokeWidth = max(2f, size * 0.005f)
            }

            when (shape) {
                LogoShape.ROUNDED_RECT -> {
                    val cornerRadius = badgeSize * 0.22f
                    canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, badgePaint)
                    canvas.drawRoundRect(badgeRect, cornerRadius, cornerRadius, borderPaint)
                }
                LogoShape.CIRCLE -> {
                    val radius = badgeSize / 2f
                    canvas.drawCircle(cx, cy, radius, badgePaint)
                    canvas.drawCircle(cx, cy, radius, borderPaint)
                }
                LogoShape.SQUARE -> {
                    canvas.drawRect(badgeRect, badgePaint)
                    canvas.drawRect(badgeRect, borderPaint)
                }
                LogoShape.NONE -> {}
            }
        }

        // 2. Draw scaled logo centered
        val scaledLogo = Bitmap.createScaledBitmap(logo, targetLogoSize, targetLogoSize, true)
        val logoLeft = cx - targetLogoSize / 2f
        val logoTop = cy - targetLogoSize / 2f

        if (shape == LogoShape.CIRCLE) {
            // Clip circle for logo if it's a photo
            val circleLogo = Bitmap.createBitmap(targetLogoSize, targetLogoSize, Bitmap.Config.ARGB_8888)
            val circleCanvas = Canvas(circleLogo)
            val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.BLACK }
            val logoRect = RectF(0f, 0f, targetLogoSize.toFloat(), targetLogoSize.toFloat())
            circleCanvas.drawOval(logoRect, clipPaint)
            clipPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            circleCanvas.drawBitmap(scaledLogo, 0f, 0f, clipPaint)

            canvas.drawBitmap(
                circleLogo,
                logoLeft,
                logoTop,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )
        } else {
            canvas.drawBitmap(
                scaledLogo,
                logoLeft,
                logoTop,
                Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
            )
        }

        return output
    }

    /**
     * Calculates WCAG relative luminance for an sRGB color integer
     */
    fun calculateLuminance(color: Int): Double {
        val r = Color.red(color) / 255.0
        val g = Color.green(color) / 255.0
        val b = Color.blue(color) / 255.0

        val rL = if (r <= 0.03928) r / 12.92 else ((r + 0.055) / 1.055).pow(2.4)
        val gL = if (g <= 0.03928) g / 12.92 else ((g + 0.055) / 1.055).pow(2.4)
        val bL = if (b <= 0.03928) b / 12.92 else ((b + 0.055) / 1.055).pow(2.4)

        return 0.2126 * rL + 0.7152 * gL + 0.0722 * bL
    }

    /**
     * Calculates contrast ratio between two colors (ranges 1:1 to 21:1)
     */
    fun calculateContrastRatio(colorA: Int, colorB: Int): Double {
        val lumA = calculateLuminance(colorA)
        val lumB = calculateLuminance(colorB)
        val lighter = max(lumA, lumB)
        val darker = min(lumA, lumB)
        return (lighter + 0.05) / (darker + 0.05)
    }

    fun getContrastLevel(fgColor: Int, bgColor: Int): ContrastLevel {
        val ratio = calculateContrastRatio(fgColor, bgColor)
        return when {
            ratio >= 4.5 -> ContrastLevel.HIGH
            ratio >= 2.8 -> ContrastLevel.MODERATE
            else -> ContrastLevel.LOW
        }
    }
}


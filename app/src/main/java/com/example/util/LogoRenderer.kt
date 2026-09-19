package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.net.Uri
import com.example.model.LogoPreset
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

object LogoRenderer {

    fun renderPresetBitmap(
        preset: LogoPreset,
        sizePx: Int = 160,
        tintColor: Int = Color.BLACK
    ): Bitmap? {
        if (preset == LogoPreset.NONE || preset == LogoPreset.CUSTOM) return null

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor
            style = Paint.Style.FILL
        }

        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = tintColor
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.09f
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        val pad = sizePx * 0.12f
        val rect = RectF(pad, pad, sizePx - pad, sizePx - pad)
        val cx = sizePx / 2f
        val cy = sizePx / 2f
        val w = rect.width()
        val h = rect.height()

        when (preset) {
            LogoPreset.WIFI -> {
                // Wi-Fi Waves & Dot
                val dotRadius = w * 0.08f
                val baseCy = rect.bottom - dotRadius
                canvas.drawCircle(cx, baseCy, dotRadius, fillPaint)

                val innerR = w * 0.32f
                val innerRect = RectF(cx - innerR, baseCy - innerR, cx + innerR, baseCy + innerR)
                canvas.drawArc(innerRect, 220f, 100f, false, strokePaint)

                val outerR = w * 0.55f
                val outerRect = RectF(cx - outerR, baseCy - outerR, cx + outerR, baseCy + outerR)
                canvas.drawArc(outerRect, 215f, 110f, false, strokePaint)
            }
            LogoPreset.LINK -> {
                // Diagonal Interlocking Link Chains
                canvas.save()
                canvas.rotate(-45f, cx, cy)

                val linkW = w * 0.50f
                val linkH = h * 0.28f
                val r = linkH / 2f

                // Left link
                val leftRect = RectF(cx - linkW * 0.75f, cy - linkH / 2f, cx + linkW * 0.15f, cy + linkH / 2f)
                canvas.drawRoundRect(leftRect, r, r, strokePaint)

                // Right link
                val rightRect = RectF(cx - linkW * 0.15f, cy - linkH / 2f, cx + linkW * 0.75f, cy + linkH / 2f)
                canvas.drawRoundRect(rightRect, r, r, strokePaint)

                canvas.restore()
            }
            LogoPreset.SECURITY -> {
                // Shield Contour
                val path = Path().apply {
                    moveTo(cx, rect.top)
                    lineTo(rect.right, rect.top + h * 0.18f)
                    quadTo(rect.right, cy + h * 0.25f, cx, rect.bottom)
                    quadTo(rect.left, cy + h * 0.25f, rect.left, rect.top + h * 0.18f)
                    close()
                }
                canvas.drawPath(path, fillPaint)

                // Cutout inner keyhole / shield mark in background contrast
                val cutPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(cx, cy - h * 0.04f, w * 0.12f, cutPaint)
                val keyRect = RectF(cx - w * 0.06f, cy - h * 0.04f, cx + w * 0.06f, cy + h * 0.18f)
                canvas.drawRoundRect(keyRect, 4f, 4f, cutPaint)
            }
            LogoPreset.CONTACT -> {
                // User Profile Head + Shoulders
                val headR = w * 0.22f
                canvas.drawCircle(cx, rect.top + headR + 2f, headR, fillPaint)

                val shoulderRect = RectF(rect.left, cy + h * 0.1f, rect.right, rect.bottom + h * 0.3f)
                canvas.drawArc(shoulderRect, 180f, 180f, true, fillPaint)
            }
            LogoPreset.EMAIL -> {
                // Envelope
                val envRect = RectF(rect.left, cy - h * 0.30f, rect.right, cy + h * 0.30f)
                canvas.drawRoundRect(envRect, 10f, 10f, fillPaint)

                val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.STROKE
                    strokeWidth = sizePx * 0.07f
                    strokeCap = Paint.Cap.ROUND
                }
                val flapPath = Path().apply {
                    moveTo(envRect.left + 8f, envRect.top + 8f)
                    lineTo(cx, cy + h * 0.05f)
                    lineTo(envRect.right - 8f, envRect.top + 8f)
                }
                canvas.drawPath(flapPath, linePaint)
            }
            LogoPreset.STAR -> {
                // 5-point Star
                val starPath = Path()
                val outerRadius = w * 0.48f
                val innerRadius = outerRadius * 0.42f
                val startAngle = -Math.PI / 2

                for (i in 0 until 10) {
                    val radius = if (i % 2 == 0) outerRadius else innerRadius
                    val angle = startAngle + i * Math.PI / 5
                    val x = (cx + radius * cos(angle)).toFloat()
                    val y = (cy + radius * sin(angle)).toFloat()
                    if (i == 0) starPath.moveTo(x, y) else starPath.lineTo(x, y)
                }
                starPath.close()
                canvas.drawPath(starPath, fillPaint)
            }
            LogoPreset.HEART -> {
                // Classic Heart
                val heartPath = Path().apply {
                    moveTo(cx, cy + h * 0.42f)
                    cubicTo(
                        rect.left - w * 0.1f, cy,
                        rect.left + w * 0.05f, rect.top - h * 0.05f,
                        cx, cy - h * 0.15f
                    )
                    cubicTo(
                        rect.right - w * 0.05f, rect.top - h * 0.05f,
                        rect.right + w * 0.1f, cy,
                        cx, cy + h * 0.42f
                    )
                    close()
                }
                canvas.drawPath(heartPath, fillPaint)
            }
            LogoPreset.SHOPPING -> {
                // Shopping Bag + Handle
                val bagRect = RectF(rect.left + w * 0.08f, cy - h * 0.18f, rect.right - w * 0.08f, rect.bottom)
                canvas.drawRoundRect(bagRect, 12f, 12f, fillPaint)

                val handleR = w * 0.20f
                val handleRect = RectF(cx - handleR, rect.top + 2f, cx + handleR, rect.top + handleR * 2.2f)
                canvas.drawArc(handleRect, 180f, 180f, false, strokePaint)
            }
            LogoPreset.LOCATION -> {
                // Map Pin
                val pinR = w * 0.28f
                val pinCenterY = rect.top + pinR
                val pinPath = Path().apply {
                    moveTo(cx, rect.bottom)
                    lineTo(cx - pinR * 0.95f, pinCenterY + pinR * 0.25f)
                    arcTo(RectF(cx - pinR, pinCenterY - pinR, cx + pinR, pinCenterY + pinR), 140f, 260f, false)
                    close()
                }
                canvas.drawPath(pinPath, fillPaint)

                val holePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawCircle(cx, pinCenterY, pinR * 0.38f, holePaint)
            }
            else -> {}
        }

        return bitmap
    }

    /**
     * Decodes and scales a user-selected image URI into a high quality square Bitmap for QR overlay
     */
    fun decodeUriToSquareBitmap(context: Context, uri: Uri, targetSize: Int = 300): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val origWidth = options.outWidth
            val origHeight = options.outHeight
            if (origWidth <= 0 || origHeight <= 0) return null

            var sampleSize = 1
            while (origWidth / (sampleSize * 2) >= targetSize && origHeight / (sampleSize * 2) >= targetSize) {
                sampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val loadedBitmap = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

            // Crop to square centered
            val minEdge = min(loadedBitmap.width, loadedBitmap.height)
            val cropX = (loadedBitmap.width - minEdge) / 2
            val cropY = (loadedBitmap.height - minEdge) / 2
            val squareBitmap = Bitmap.createBitmap(loadedBitmap, cropX, cropY, minEdge, minEdge)

            // Scale to target size
            if (squareBitmap.width != targetSize) {
                Bitmap.createScaledBitmap(squareBitmap, targetSize, targetSize, true)
            } else {
                squareBitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

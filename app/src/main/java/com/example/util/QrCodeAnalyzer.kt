package com.example.util

import android.graphics.Bitmap
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.NotFoundException
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.EnumMap

class QrCodeAnalyzer(
    private val onQrCodeDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val reader = MultiFormatReader().apply {
        val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
            put(DecodeHintType.TRY_HARDER, true)
            put(DecodeHintType.CHARACTER_SET, "UTF-8")
        }
        setHints(hints)
    }

    private var isScanning = true

    fun setScanningEnabled(enabled: Boolean) {
        isScanning = enabled
    }

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!isScanning) {
            imageProxy.close()
            return
        }

        val image = imageProxy.image
        if (image != null) {
            val planes = image.planes
            val yBuffer = planes[0].buffer
            val ySize = yBuffer.remaining()
            val yBytes = ByteArray(ySize)
            yBuffer.get(yBytes)

            val width = imageProxy.width
            val height = imageProxy.height

            val source = PlanarYUVLuminanceSource(
                yBytes, width, height, 0, 0, width, height, false
            )
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))

            try {
                val result = reader.decodeWithState(binaryBitmap)
                if (result != null && result.text.isNotBlank()) {
                    onQrCodeDetected(result.text)
                }
            } catch (_: NotFoundException) {
                // Normal when frame has no QR code
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                reader.reset()
            }
        }
        imageProxy.close()
    }

    companion object {
        fun decodeFromBitmap(bitmap: Bitmap): String? {
            return try {
                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                val source = RGBLuminanceSource(width, height, pixels)
                val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
                val reader = MultiFormatReader().apply {
                    val hints = EnumMap<DecodeHintType, Any>(DecodeHintType::class.java).apply {
                        put(DecodeHintType.TRY_HARDER, true)
                        put(DecodeHintType.CHARACTER_SET, "UTF-8")
                    }
                    setHints(hints)
                }
                val result = reader.decode(binaryBitmap)
                result.text
            } catch (e: Exception) {
                null
            }
        }
    }
}

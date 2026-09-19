package com.example.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileExporter {

    private fun getExportDir(context: Context): File {
        val dir = File(context.cacheDir, "qr_exports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    /**
     * Saves QR Bitmap to PNG file and returns the FileProvider Uri
     */
    fun exportToPng(context: Context, bitmap: Bitmap, title: String): Uri? {
        return try {
            val safeTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20).ifEmpty { "qr_code" }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExportDir(context), "${safeTitle}_$timestamp.png")

            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Generates a clean, professional PDF document containing the QR Code and its details
     */
    fun exportToPdf(
        context: Context,
        bitmap: Bitmap,
        title: String,
        type: String,
        content: String,
        isEncrypted: Boolean
    ): Uri? {
        val pdfDocument = PdfDocument()
        return try {
            // A4 size: 595 x 842 points
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val backgroundPaint = Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), backgroundPaint)

            // Header Banner
            val headerPaint = Paint().apply {
                color = Color.parseColor("#1E293B") // Slate 800
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, pageWidth.toFloat(), 90f, headerPaint)

            // Header Title
            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 22f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText("QR Code Master Document", 40f, 48f, titlePaint)

            val subtitlePaint = Paint().apply {
                color = Color.parseColor("#94A3B8") // Slate 400
                textSize = 12f
                isAntiAlias = true
            }
            val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date())
            canvas.drawText("Generated on $dateStr • Offline Privacy Protected", 40f, 70f, subtitlePaint)

            // Document Content Title
            val docTitlePaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 20f
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.drawText(title.take(45), 40f, 130f, docTitlePaint)

            val badgePaint = Paint().apply {
                color = if (isEncrypted) Color.parseColor("#DC2626") else Color.parseColor("#4F46E5")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val badgeRect = RectF(40f, 142f, 40f + (if (isEncrypted) 120f else 90f), 164f)
            canvas.drawRoundRect(badgeRect, 6f, 6f, badgePaint)

            val badgeTextPaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val badgeLabel = if (isEncrypted) "ENCRYPTED (AES-256)" else type.uppercase()
            canvas.drawText(badgeLabel, 48f, 157f, badgeTextPaint)

            // Center QR Code in a stylish frame
            val qrBoxSize = 280f
            val qrLeft = (pageWidth - qrBoxSize) / 2f
            val qrTop = 185f

            val framePaint = Paint().apply {
                color = Color.parseColor("#F1F5F9") // Light slate
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val borderPaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                style = Paint.Style.STROKE
                strokeWidth = 2f
                isAntiAlias = true
            }

            val qrRect = RectF(qrLeft - 15f, qrTop - 15f, qrLeft + qrBoxSize + 15f, qrTop + qrBoxSize + 15f)
            canvas.drawRoundRect(qrRect, 16f, 16f, framePaint)
            canvas.drawRoundRect(qrRect, 16f, 16f, borderPaint)

            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, qrBoxSize.toInt(), qrBoxSize.toInt(), true)
            canvas.drawBitmap(scaledBitmap, qrLeft, qrTop, null)

            // Data Box below QR Code
            val boxTop = qrTop + qrBoxSize + 40f
            val contentBoxRect = RectF(40f, boxTop, pageWidth - 40f, boxTop + 180f)
            val boxBgPaint = Paint().apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
                isAntiAlias = true
            }
            val boxStrokePaint = Paint().apply {
                color = Color.parseColor("#E2E8F0")
                style = Paint.Style.STROKE
                strokeWidth = 1f
                isAntiAlias = true
            }
            canvas.drawRoundRect(contentBoxRect, 12f, 12f, boxBgPaint)
            canvas.drawRoundRect(contentBoxRect, 12f, 12f, boxStrokePaint)

            val labelPaint = Paint().apply {
                color = Color.parseColor("#64748B")
                textSize = 11f
                isFakeBoldText = true
                isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                color = Color.parseColor("#1E293B")
                textSize = 11f
                isAntiAlias = true
            }

            canvas.drawText("PAYLOAD CONTENT", 60f, boxTop + 30f, labelPaint)

            // Word wrap content lines
            var currentY = boxTop + 50f
            val maxCharsPerLine = 65
            val lines = content.chunked(maxCharsPerLine)
            for (line in lines.take(7)) {
                canvas.drawText(line, 60f, currentY, valuePaint)
                currentY += 16f
            }
            if (lines.size > 7) {
                canvas.drawText("... [content truncated for PDF preview]", 60f, currentY, labelPaint)
            }

            // Footer note
            val footerPaint = Paint().apply {
                color = Color.parseColor("#94A3B8")
                textSize = 10f
                isAntiAlias = true
            }
            canvas.drawText("This QR code was generated completely offline. Your data remained local and private.", 40f, pageHeight - 40f, footerPaint)

            pdfDocument.finishPage(page)

            val safeTitle = title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(20).ifEmpty { "qr_code" }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val file = File(getExportDir(context), "${safeTitle}_$timestamp.pdf")

            FileOutputStream(file).use { out ->
                pdfDocument.writeTo(out)
                out.flush()
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            pdfDocument.close()
        }
    }

    fun createShareTextIntent(text: String, subject: String = "QR Code Content"): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, text)
        }
    }

    fun createShareFileIntent(uri: Uri, mimeType: String, subject: String = "Exported QR Code"): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    /**
     * Saves QR Bitmap directly to the user's Gallery (Pictures/QRCodeMaster)
     * using MediaStore for Android 10+ and Pictures directory with MediaScanner for older versions.
     */
    fun saveToGallery(context: Context, bitmap: Bitmap, title: String): Result<Uri> {
        return try {
            val safeTitle = title.replace(Regex("[^a-zA-Z0-9_-]"), "_").trim('_').ifEmpty { "qr_code" }
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "QR_${safeTitle}_$timestamp.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/QRCodeMaster")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: return Result.failure(Exception("Could not create MediaStore entry"))

                resolver.openOutputStream(uri)?.use { outStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                        throw Exception("Failed to write PNG image")
                    }
                    outStream.flush()
                } ?: return Result.failure(Exception("Could not open image stream"))

                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)

                Result.success(uri)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val qrDir = File(picturesDir, "QRCodeMaster")
                if (!qrDir.exists()) {
                    qrDir.mkdirs()
                }
                val imageFile = File(qrDir, fileName)
                FileOutputStream(imageFile).use { outStream ->
                    if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                        throw Exception("Failed to write PNG image")
                    }
                    outStream.flush()
                }

                var scannedUri: Uri? = null
                MediaScannerConnection.scanFile(
                    context,
                    arrayOf(imageFile.absolutePath),
                    arrayOf("image/png")
                ) { _, uri ->
                    scannedUri = uri
                }

                val finalUri = scannedUri ?: Uri.fromFile(imageFile)
                Result.success(finalUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun createViewImageIntent(uri: Uri): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/png")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}

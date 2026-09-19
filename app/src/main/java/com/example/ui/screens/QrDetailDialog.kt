package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.QrEntity
import com.example.util.FileExporter
import com.example.util.QrCodeGenerator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun QrDetailDialog(
    item: QrEntity,
    onDismiss: () -> Unit,
    onToggleFavorite: (QrEntity) -> Unit,
    onDelete: (QrEntity) -> Unit,
    onOpenDecrypt: (String) -> Unit,
    onExportPng: (Context, QrEntity, (Uri) -> Unit) -> Unit,
    onExportPdf: (Context, QrEntity, (Uri) -> Unit) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(item) {
        val bmp = withContext(Dispatchers.Default) {
            QrCodeGenerator.generateBitmap(
                content = item.content,
                size = 600,
                foregroundColor = item.foregroundColor,
                backgroundColor = item.backgroundColor
            )
        }
        qrBitmap = bmp
    }

    val formattedDate = remember(item.timestamp) {
        SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()).format(Date(item.timestamp))
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                if (item.isEncrypted) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isEncrypted) Icons.Default.Lock else Icons.Default.QrCode,
                            contentDescription = null,
                            tint = if (item.isEncrypted) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "${if (item.isGenerated) "Created" else "Scanned"} • $formattedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { onToggleFavorite(item) },
                    modifier = Modifier.testTag("detail_favorite_btn")
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (item.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // QR Code Image Box
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .size(220.dp)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        qrBitmap?.let { bmp ->
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "QR Code Image",
                                modifier = Modifier.size(190.dp)
                            )
                        } ?: run {
                            Text("Rendering...", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Payload text preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (item.isEncrypted) "ENCRYPTED PAYLOAD (AES-256-GCM)" else "PAYLOAD CONTENT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.isEncrypted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 4,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (item.isEncrypted) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenDecrypt(item.content)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("detail_decrypt_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Decrypt with Password")
                        }
                    }

                    // Direct Download to Gallery
                    Button(
                        onClick = {
                            qrBitmap?.let { bmp ->
                                val res = FileExporter.saveToGallery(context, bmp, item.title)
                                res.onSuccess {
                                    onShowMessage("Downloaded to Gallery! (Pictures/QRCodeMaster)")
                                }.onFailure { e ->
                                    onShowMessage("Failed to save to Gallery: ${e.localizedMessage}")
                                }
                            } ?: run {
                                onShowMessage("QR code is still rendering...")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_download_gallery_btn")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Download to Gallery", fontWeight = FontWeight.SemiBold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Export PNG
                        FilledTonalButton(
                            onClick = {
                                onExportPng(context, item) { uri ->
                                    val intent = FileExporter.createShareFileIntent(uri, "image/png", "QR Code - ${item.title}")
                                    context.startActivity(Intent.createChooser(intent, "Share PNG"))
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_png_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PNG")
                        }

                        // Export PDF
                        FilledTonalButton(
                            onClick = {
                                onExportPdf(context, item) { uri ->
                                    val intent = FileExporter.createShareFileIntent(uri, "application/pdf", "QR Document - ${item.title}")
                                    context.startActivity(Intent.createChooser(intent, "Export / Share PDF"))
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("export_pdf_btn")
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("PDF")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Copy Text
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", item.content))
                                onShowMessage("Copied to clipboard!")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_copy_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy")
                        }

                        // Share Text
                        OutlinedButton(
                            onClick = {
                                val intent = FileExporter.createShareTextIntent(item.content, item.title)
                                context.startActivity(Intent.createChooser(intent, "Share QR Code Content"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_share_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share")
                        }
                    }

                    // Delete item button
                    OutlinedButton(
                        onClick = {
                            onDelete(item)
                            onDismiss()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("detail_delete_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete from History")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_detail_dialog_btn")
            ) {
                Text("Close")
            }
        }
    )
}

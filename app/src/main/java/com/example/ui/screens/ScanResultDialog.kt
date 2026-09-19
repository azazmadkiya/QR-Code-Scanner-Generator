package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParsedQrResult
import com.example.model.QrType
import com.example.util.FileExporter

@Composable
fun ScanResultDialog(
    result: ParsedQrResult,
    onDismiss: () -> Unit,
    onOpenDecrypt: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (result.isEncrypted) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (result.isEncrypted) Icons.Default.Lock else Icons.Default.QrCode,
                        contentDescription = null,
                        tint = if (result.isEncrypted) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (result.isEncrypted) "Encrypted QR Code" else result.type.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Saved automatically to local history",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Display Title / Summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = result.displayTitle,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = result.rawContent.take(200) + if (result.rawContent.length > 200) "…" else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons based on type
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (result.isEncrypted) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenDecrypt(result.rawContent)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("decrypt_scanned_btn")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Decrypt with Password")
                        }
                    }

                    if (result.type == QrType.URL) {
                        Button(
                            onClick = {
                                try {
                                    val url = if (!result.rawContent.startsWith("http://") && !result.rawContent.startsWith("https://")) {
                                        "https://${result.rawContent}"
                                    } else result.rawContent
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onShowMessage("Unable to open browser")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("open_url_btn")
                        ) {
                            Icon(Icons.Default.OpenInBrowser, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open in Browser")
                        }
                    }

                    if (result.type == QrType.WIFI) {
                        val pass = result.details["Password"] ?: ""
                        if (pass.isNotEmpty()) {
                            FilledTonalButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Wi-Fi Password", pass))
                                    onShowMessage("Wi-Fi password copied to clipboard!")
                                },
                                modifier = Modifier.fillMaxWidth().testTag("copy_wifi_pass_btn")
                            ) {
                                Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Copy Wi-Fi Password ($pass)")
                            }
                        }
                    }

                    if (result.type == QrType.PHONE) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${result.details["Phone"] ?: result.rawContent}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onShowMessage("Unable to launch dialer")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("dial_phone_btn")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call Phone Number")
                        }
                    }

                    if (result.type == QrType.EMAIL) {
                        Button(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${result.details["Email"] ?: result.rawContent}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onShowMessage("Unable to open email client")
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("send_email_btn")
                        ) {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send Email")
                        }
                    }

                    // Copy to clipboard
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("QR Code Content", result.rawContent))
                            onShowMessage("Content copied to clipboard!")
                        },
                        modifier = Modifier.fillMaxWidth().testTag("copy_content_btn")
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy Raw Content")
                    }

                    // Share
                    OutlinedButton(
                        onClick = {
                            val intent = FileExporter.createShareTextIntent(result.rawContent, "Scanned QR: ${result.displayTitle}")
                            context.startActivity(Intent.createChooser(intent, "Share QR Content"))
                        },
                        modifier = Modifier.fillMaxWidth().testTag("share_content_btn")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share Text")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_scan_dialog_btn")
            ) {
                Text("Close")
            }
        }
    )
}

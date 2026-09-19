package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContactPage
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.model.LogoPreset
import com.example.model.LogoShape
import com.example.model.LogoSize
import com.example.model.QrType
import com.example.ui.components.ColorPickerSection
import com.example.ui.components.LogoOverlaySection
import com.example.util.FileExporter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@Composable
fun GenerateScreen(
    genType: QrType,
    onGenTypeChange: (QrType) -> Unit,

    text: String,
    onTextChange: (String) -> Unit,

    url: String,
    onUrlChange: (String) -> Unit,

    wifiSsid: String,
    onWifiSsidChange: (String) -> Unit,
    wifiPass: String,
    onWifiPassChange: (String) -> Unit,
    wifiType: String,
    onWifiTypeChange: (String) -> Unit,
    wifiHidden: Boolean,
    onWifiHiddenChange: (Boolean) -> Unit,

    contactName: String,
    onContactNameChange: (String) -> Unit,
    contactPhone: String,
    onContactPhoneChange: (String) -> Unit,
    contactEmail: String,
    onContactEmailChange: (String) -> Unit,
    contactOrg: String,
    onContactOrgChange: (String) -> Unit,

    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,

    smsMessage: String,
    onSmsMessageChange: (String) -> Unit,

    emailTo: String,
    onEmailToChange: (String) -> Unit,
    emailSubject: String,
    onEmailSubjectChange: (String) -> Unit,
    emailBody: String,
    onEmailBodyChange: (String) -> Unit,

    isEncrypted: Boolean,
    onIsEncryptedChange: (Boolean) -> Unit,
    passphrase: String,
    onPassphraseChange: (String) -> Unit,

    foregroundColor: Int,
    onForegroundColorChange: (Int) -> Unit,
    backgroundColor: Int,
    onBackgroundColorChange: (Int) -> Unit,
    onSwapColors: () -> Unit,
    onResetColors: () -> Unit,
    errorCorrection: ErrorCorrectionLevel,
    onErrorCorrectionChange: (ErrorCorrectionLevel) -> Unit,

    selectedLogoPreset: LogoPreset,
    customLogoBitmap: Bitmap?,
    selectedLogoShape: LogoShape,
    selectedLogoSize: LogoSize,
    onLogoPresetChange: (LogoPreset) -> Unit,
    onLogoShapeChange: (LogoShape) -> Unit,
    onLogoSizeChange: (LogoSize) -> Unit,
    onPickCustomImage: () -> Unit,
    onClearCustomImage: () -> Unit,

    generatedBitmap: Bitmap?,
    generatedRawContent: String,
    generatedTitle: String,

    onGenerateClick: () -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var savedToGalleryUri by remember(generatedBitmap) { mutableStateOf<Uri?>(null) }

    val qrTypes = listOf(
        Pair(QrType.TEXT, "Text"),
        Pair(QrType.URL, "URL"),
        Pair(QrType.WIFI, "Wi-Fi"),
        Pair(QrType.CONTACT, "Contact"),
        Pair(QrType.PHONE, "Phone"),
        Pair(QrType.SMS, "SMS"),
        Pair(QrType.EMAIL, "Email")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("generate_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Types Selector Row
        item {
            Text(
                text = "Select Content Type",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                qrTypes.forEach { (type, label) ->
                    val selected = genType == type
                    FilterChip(
                        selected = selected,
                        onClick = { onGenTypeChange(type) },
                        label = { Text(label) },
                        leadingIcon = {
                            val icon = when (type) {
                                QrType.TEXT -> Icons.Default.TextFields
                                QrType.URL -> Icons.Default.Link
                                QrType.WIFI -> Icons.Default.Wifi
                                QrType.CONTACT -> Icons.Default.ContactPage
                                QrType.PHONE -> Icons.Default.Call
                                QrType.SMS -> Icons.AutoMirrored.Filled.Message
                                QrType.EMAIL -> Icons.Default.Email
                                else -> Icons.Default.QrCode
                            }
                            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ),
                        modifier = Modifier.testTag("type_chip_${type.name}")
                    )
                }
            }
        }

        // Dynamic Form Fields
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (genType) {
                        QrType.TEXT -> {
                            OutlinedTextField(
                                value = text,
                                onValueChange = onTextChange,
                                label = { Text("Enter plain text or note") },
                                minLines = 3,
                                maxLines = 6,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_gen_text")
                            )
                        }
                        QrType.URL -> {
                            OutlinedTextField(
                                value = url,
                                onValueChange = onUrlChange,
                                label = { Text("Website Address (URL)") },
                                placeholder = { Text("https://example.com") },
                                leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_gen_url")
                            )
                        }
                        QrType.WIFI -> {
                            OutlinedTextField(
                                value = wifiSsid,
                                onValueChange = onWifiSsidChange,
                                label = { Text("Network Name (SSID)") },
                                leadingIcon = { Icon(Icons.Default.Wifi, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_wifi_ssid")
                            )
                            OutlinedTextField(
                                value = wifiPass,
                                onValueChange = onWifiPassChange,
                                label = { Text("Wi-Fi Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_wifi_pass")
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onWifiHiddenChange(!wifiHidden) }
                            ) {
                                Checkbox(
                                    checked = wifiHidden,
                                    onCheckedChange = onWifiHiddenChange,
                                    modifier = Modifier.testTag("input_wifi_hidden")
                                )
                                Text("Hidden Network", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        QrType.CONTACT -> {
                            OutlinedTextField(
                                value = contactName,
                                onValueChange = onContactNameChange,
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_contact_name")
                            )
                            OutlinedTextField(
                                value = contactPhone,
                                onValueChange = onContactPhoneChange,
                                label = { Text("Phone Number") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_contact_phone")
                            )
                            OutlinedTextField(
                                value = contactEmail,
                                onValueChange = onContactEmailChange,
                                label = { Text("Email Address") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_contact_email")
                            )
                            OutlinedTextField(
                                value = contactOrg,
                                onValueChange = onContactOrgChange,
                                label = { Text("Company / Organization") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_contact_org")
                            )
                        }
                        QrType.PHONE -> {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = onPhoneNumberChange,
                                label = { Text("Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_phone_number")
                            )
                        }
                        QrType.SMS -> {
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = onPhoneNumberChange,
                                label = { Text("Recipient Phone Number") },
                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_sms_number")
                            )
                            OutlinedTextField(
                                value = smsMessage,
                                onValueChange = onSmsMessageChange,
                                label = { Text("SMS Message Body") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth().testTag("input_sms_body")
                            )
                        }
                        QrType.EMAIL -> {
                            OutlinedTextField(
                                value = emailTo,
                                onValueChange = onEmailToChange,
                                label = { Text("Recipient Email") },
                                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_email_to")
                            )
                            OutlinedTextField(
                                value = emailSubject,
                                onValueChange = onEmailSubjectChange,
                                label = { Text("Subject") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("input_email_subject")
                            )
                            OutlinedTextField(
                                value = emailBody,
                                onValueChange = onEmailBodyChange,
                                label = { Text("Message Body") },
                                minLines = 2,
                                maxLines = 4,
                                modifier = Modifier.fillMaxWidth().testTag("input_email_body")
                            )
                        }
                        else -> {}
                    }
                }
            }
        }

        // AES-256 Encryption Feature Section
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isEncrypted) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        if (isEncrypted) MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                        if (isEncrypted) MaterialTheme.colorScheme.errorContainer
                                        else MaterialTheme.colorScheme.primaryContainer
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = if (isEncrypted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Secure AES-256 Encryption",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Requires password to decrypt when scanned",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isEncrypted,
                            onCheckedChange = onIsEncryptedChange,
                            modifier = Modifier.testTag("toggle_encryption_switch")
                        )
                    }

                    AnimatedVisibility(visible = isEncrypted) {
                        Column(modifier = Modifier.padding(top = 14.dp)) {
                            OutlinedTextField(
                                value = passphrase,
                                onValueChange = onPassphraseChange,
                                label = { Text("Encryption Passphrase") },
                                leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = null
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_encryption_passphrase")
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Keep this passphrase safe. Decryption is performed completely offline on-device.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Color & Contrast Customization Section
        item {
            ColorPickerSection(
                foregroundColor = foregroundColor,
                backgroundColor = backgroundColor,
                onForegroundColorChange = onForegroundColorChange,
                onBackgroundColorChange = onBackgroundColorChange,
                onSwapColors = onSwapColors,
                onResetColors = onResetColors
            )
        }

        // Center Logo Overlay Section
        item {
            LogoOverlaySection(
                selectedPreset = selectedLogoPreset,
                customLogoBitmap = customLogoBitmap,
                selectedShape = selectedLogoShape,
                selectedSize = selectedLogoSize,
                onPresetChange = onLogoPresetChange,
                onShapeChange = onLogoShapeChange,
                onSizeChange = onLogoSizeChange,
                onPickCustomImage = onPickCustomImage,
                onClearCustomImage = onClearCustomImage
            )
        }

        // Generate Button
        item {
            Button(
                onClick = onGenerateClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("generate_qr_btn")
            ) {
                Icon(Icons.Default.QrCode, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isEncrypted) "Generate Encrypted QR Code" else "Generate QR Code",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Generated QR Code Preview Section
        if (generatedBitmap != null) {
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                        .testTag("generated_preview_card")
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Preview: $generatedTitle",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (selectedLogoPreset != LogoPreset.NONE) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            text = "Logo: ${selectedLogoPreset.label}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                if (isEncrypted) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            text = "AES-256",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // QR Code Bitmap Image (Using selected background color)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(backgroundColor),
                            tonalElevation = 2.dp,
                            modifier = Modifier
                                .size(240.dp)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    bitmap = generatedBitmap.asImageBitmap(),
                                    contentDescription = "Generated QR Code with Logo and Color styling",
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Primary Action: Download to Device Gallery
                        Button(
                            onClick = {
                                val result = FileExporter.saveToGallery(context, generatedBitmap, generatedTitle)
                                result.onSuccess { uri ->
                                    savedToGalleryUri = uri
                                    onShowMessage("Downloaded to Gallery! (Pictures/QRCodeMaster)")
                                }.onFailure { e ->
                                    onShowMessage("Failed to save to Gallery: ${e.localizedMessage}")
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("gen_download_gallery_btn")
                        ) {
                            Icon(
                                imageVector = if (savedToGalleryUri != null) Icons.Default.Check else Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (savedToGalleryUri != null) "Saved to Gallery (Pictures/QRCodeMaster)" else "Download to Gallery",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Instant "Open in Gallery" shortcut when saved
                        AnimatedVisibility(visible = savedToGalleryUri != null) {
                            Column {
                                Spacer(modifier = Modifier.height(8.dp))
                                FilledTonalButton(
                                    onClick = {
                                        try {
                                            savedToGalleryUri?.let { uri ->
                                                val viewIntent = FileExporter.createViewImageIntent(uri)
                                                context.startActivity(viewIntent)
                                            }
                                        } catch (_: Exception) {
                                            onShowMessage("Unable to open image viewer")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("gen_open_gallery_btn")
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Open in Photos / Gallery")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Secondary Sharing & Document Export
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // PNG Share
                            FilledTonalButton(
                                onClick = {
                                    val uri = FileExporter.exportToPng(context, generatedBitmap, generatedTitle)
                                    if (uri != null) {
                                        val intent = FileExporter.createShareFileIntent(uri, "image/png", "QR Code - $generatedTitle")
                                        context.startActivity(Intent.createChooser(intent, "Share PNG"))
                                    } else {
                                        onShowMessage("Failed to export PNG")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gen_export_png_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share PNG")
                            }

                            // PDF Document Export
                            FilledTonalButton(
                                onClick = {
                                    val uri = FileExporter.exportToPdf(
                                        context = context,
                                        bitmap = generatedBitmap,
                                        title = generatedTitle,
                                        type = genType.name,
                                        content = generatedRawContent,
                                        isEncrypted = isEncrypted
                                    )
                                    if (uri != null) {
                                        val intent = FileExporter.createShareFileIntent(uri, "application/pdf", "QR Document - $generatedTitle")
                                        context.startActivity(Intent.createChooser(intent, "Export / Share PDF"))
                                    } else {
                                        onShowMessage("Failed to export PDF")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gen_export_pdf_btn")
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PDF Doc")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Copy Raw String
                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("QR Code", generatedRawContent))
                                    onShowMessage("Payload copied to clipboard!")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gen_copy_text_btn")
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy")
                            }

                            // Share Text
                            OutlinedButton(
                                onClick = {
                                    val intent = FileExporter.createShareTextIntent(generatedRawContent, generatedTitle)
                                    context.startActivity(Intent.createChooser(intent, "Share QR Code"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gen_share_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }
                        }
                    }
                }
            }
        }
    }
}

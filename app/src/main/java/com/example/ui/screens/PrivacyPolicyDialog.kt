package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Text(
                    text = "Privacy Policy & Legal",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
                    .verticalScroll(scrollState)
                    .testTag("privacy_policy_scroll_content"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Last updated: September 2026",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Text(
                    text = "1. Introduction & Overview",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Welcome to QR Code Master. We respect your privacy and are committed to protecting your personal data. This application is designed to operate 100% offline, ensuring that your scanned codes, generated QR contents, and history remain completely private and secure on your device.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "2. Permissions & Data Access",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "• Camera Permission: Required solely for real-time QR code and barcode scanning through the device camera viewfinder. Camera image buffers are processed locally in real time and are never recorded, cached, or transmitted to any remote server.\n\n• Storage / Gallery Access: Used strictly when you choose to save generated QR codes directly to your device gallery or export them as PNG/PDF files.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "3. Local Data Storage & Security",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "All scan history, favorites, and custom configurations are stored locally on your device using a secure local Room database. If you use AES-256 encryption, your data is cryptographically secured with your user-defined passphrase locally on your device.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "4. Third-Party Services & Tracking",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "QR Code Master does not integrate third-party analytics trackers, advertising networks, or cloud synchronization servers. Your data never leaves your device.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "5. Children's Privacy",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Our app does not knowingly collect or solicit personal information from children under 13. Since all data is stored locally, no personal data is collected by us whatsoever.",
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = "6. Contact Us",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "If you have any questions or concerns regarding our privacy policy or data practices, please contact support through our Play Store developer contact page.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_privacy_dialog_btn")
            ) {
                Text("Close")
            }
        }
    )
}

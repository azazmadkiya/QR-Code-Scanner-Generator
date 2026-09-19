package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.LogoPreset
import com.example.model.LogoShape
import com.example.model.LogoSize

@Composable
fun LogoOverlaySection(
    selectedPreset: LogoPreset,
    customLogoBitmap: Bitmap?,
    selectedShape: LogoShape,
    selectedSize: LogoSize,
    onPresetChange: (LogoPreset) -> Unit,
    onShapeChange: (LogoShape) -> Unit,
    onSizeChange: (LogoSize) -> Unit,
    onPickCustomImage: () -> Unit,
    onClearCustomImage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLogoActive = selectedPreset != LogoPreset.NONE

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .testTag("logo_overlay_section")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Center Logo Overlay",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Embed brand badge or custom icon",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isLogoActive) {
                    IconButton(
                        onClick = {
                            onPresetChange(LogoPreset.NONE)
                            onClearCustomImage()
                        },
                        modifier = Modifier.testTag("btn_clear_logo")
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Clear Logo",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Presets Horizontal Row
            Column {
                Text(
                    text = "Choose Icon or Upload Image",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LogoPreset.values().forEach { preset ->
                        val selected = selectedPreset == preset
                        FilterChip(
                            selected = selected,
                            onClick = {
                                if (preset == LogoPreset.CUSTOM) {
                                    onPresetChange(LogoPreset.CUSTOM)
                                    if (customLogoBitmap == null) {
                                        onPickCustomImage()
                                    }
                                } else {
                                    onPresetChange(preset)
                                }
                            },
                            label = { Text(preset.label) },
                            leadingIcon = {
                                val icon = when (preset) {
                                    LogoPreset.NONE -> Icons.Default.Close
                                    LogoPreset.WIFI -> Icons.Default.Wifi
                                    LogoPreset.LINK -> Icons.Default.Link
                                    LogoPreset.SECURITY -> Icons.Default.Security
                                    LogoPreset.CONTACT -> Icons.Default.Person
                                    LogoPreset.EMAIL -> Icons.Default.Email
                                    LogoPreset.STAR -> Icons.Default.Star
                                    LogoPreset.HEART -> Icons.Default.Favorite
                                    LogoPreset.SHOPPING -> Icons.Default.ShoppingBag
                                    LogoPreset.LOCATION -> Icons.Default.LocationOn
                                    LogoPreset.CUSTOM -> Icons.Default.AddPhotoAlternate
                                }
                                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("chip_logo_${preset.name.lowercase()}")
                        )
                    }
                }
            }

            // Custom Image Upload Box if CUSTOM selected
            AnimatedVisibility(visible = selectedPreset == LogoPreset.CUSTOM) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (customLogoBitmap != null) {
                                Image(
                                    bitmap = customLogoBitmap.asImageBitmap(),
                                    contentDescription = "Custom Logo Preview",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (customLogoBitmap != null) "Custom Image Selected" else "No image chosen yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Tap to pick from device gallery",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onPickCustomImage,
                            modifier = Modifier.testTag("btn_select_custom_logo")
                        ) {
                            Text(if (customLogoBitmap != null) "Change" else "Select")
                        }
                    }
                }
            }

            // Controls for Badge Shape & Logo Size (Visible when a logo is active)
            AnimatedVisibility(visible = isLogoActive) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Badge Shape Selector
                    Column {
                        Text(
                            text = "Badge Frame Shape",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LogoShape.values().forEach { shape ->
                                val selected = selectedShape == shape
                                FilterChip(
                                    selected = selected,
                                    onClick = { onShapeChange(shape) },
                                    label = { Text(shape.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_shape_${shape.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Logo Size Selector
                    Column {
                        Text(
                            text = "Logo Size",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LogoSize.values().forEach { sizeOption ->
                                val selected = selectedSize == sizeOption
                                FilterChip(
                                    selected = selected,
                                    onClick = { onSizeChange(sizeOption) },
                                    label = { Text(sizeOption.label) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onTertiaryContainer
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("chip_size_${sizeOption.name.lowercase()}")
                                )
                            }
                        }
                    }

                    // Scanability Notice
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Error correction is automatically upgraded to Level H (High 30%) so your customized QR code remains reliable when scanned.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

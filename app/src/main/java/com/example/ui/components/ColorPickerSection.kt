package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FormatColorReset
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.QrCodeGenerator
import java.util.Locale

@Composable
fun ColorPickerSection(
    foregroundColor: Int,
    backgroundColor: Int,
    onForegroundColorChange: (Int) -> Unit,
    onBackgroundColorChange: (Int) -> Unit,
    onSwapColors: () -> Unit,
    onResetColors: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedColorTarget by remember { mutableIntStateOf(0) } // 0 = Foreground, 1 = Background
    var showCustomSliders by remember { mutableStateOf(false) }

    val activeColor = if (selectedColorTarget == 0) foregroundColor else backgroundColor
    val onActiveColorChange = if (selectedColorTarget == 0) onForegroundColorChange else onBackgroundColorChange

    val fgPresets = listOf(
        0xFF000000.toInt() to "Black",
        0xFF0F172A.toInt() to "Slate",
        0xFF1E293B.toInt() to "Navy",
        0xFF4F46E5.toInt() to "Indigo",
        0xFF1D4ED8.toInt() to "Blue",
        0xFF0284C7.toInt() to "Cyan",
        0xFF059669.toInt() to "Emerald",
        0xFF0D9488.toInt() to "Teal",
        0xFFDC2626.toInt() to "Red",
        0xFFE11D48.toInt() to "Rose",
        0xFFD97706.toInt() to "Amber",
        0xFF7C3AED.toInt() to "Violet",
        0xFF78350F.toInt() to "Coffee"
    )

    val bgPresets = listOf(
        0xFFFFFFFF.toInt() to "White",
        0xFFFFFBEB.toInt() to "Cream",
        0xFFF8FAFC.toInt() to "Slate Light",
        0xFFF0F9FF.toInt() to "Ice",
        0xFFECFDF5.toInt() to "Mint",
        0xFFF5F3FF.toInt() to "Lavender",
        0xFFFEF2F2.toInt() to "Rose Tint",
        0xFF0F172A.toInt() to "Charcoal"
    )

    val contrastRatio = remember(foregroundColor, backgroundColor) {
        QrCodeGenerator.calculateContrastRatio(foregroundColor, backgroundColor)
    }
    val contrastLevel = remember(foregroundColor, backgroundColor) {
        QrCodeGenerator.getContrastLevel(foregroundColor, backgroundColor)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
            .testTag("color_picker_section")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row
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
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Color & Contrast Studio",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Personalize modules and background",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Quick tools: Invert & Reset
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onSwapColors,
                        modifier = Modifier.testTag("btn_swap_colors")
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = "Swap Colors",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onResetColors,
                        modifier = Modifier.testTag("btn_reset_colors")
                    ) {
                        Icon(
                            Icons.Default.FormatColorReset,
                            contentDescription = "Reset to Default",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Target selector: Foreground (QR Dots) vs Background
            TabRow(
                selectedTabIndex = selectedColorTarget,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("color_target_tab_row")
            ) {
                Tab(
                    selected = selectedColorTarget == 0,
                    onClick = { selectedColorTarget = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(foregroundColor))
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("QR Code Dots", fontWeight = if (selectedColorTarget == 0) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    modifier = Modifier.testTag("tab_color_foreground")
                )
                Tab(
                    selected = selectedColorTarget == 1,
                    onClick = { selectedColorTarget = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(backgroundColor))
                                    .border(1.dp, Color.Gray, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Background", fontWeight = if (selectedColorTarget == 1) FontWeight.Bold else FontWeight.Normal)
                        }
                    },
                    modifier = Modifier.testTag("tab_color_background")
                )
            }

            // Swatches Row
            val activePresets = if (selectedColorTarget == 0) fgPresets else bgPresets
            Column {
                Text(
                    text = if (selectedColorTarget == 0) "Preset Colors for Dots" else "Preset Background Colors",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activePresets.forEach { (colorVal, name) ->
                        val isSelected = activeColor == colorVal
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                                .clickable { onActiveColorChange(colorVal) }
                                .testTag("swatch_${name.lowercase().replace(" ", "_")}"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                val isDark = android.graphics.Color.red(colorVal) +
                                        android.graphics.Color.green(colorVal) +
                                        android.graphics.Color.blue(colorVal) < 380
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isDark) Color.White else Color.Black,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Toggle Custom Fine-Tuning Sliders / Hex Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Color preview chip with Hex
                val hexString = String.format(Locale.ROOT, "#%06X", 0xFFFFFF and activeColor)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.testTag("color_hex_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(Color(activeColor))
                                .border(1.dp, Color.Gray, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = hexString,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                FilterChip(
                    selected = showCustomSliders,
                    onClick = { showCustomSliders = !showCustomSliders },
                    label = { Text(if (showCustomSliders) "Hide RGB Sliders" else "Custom RGB / Hex") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    modifier = Modifier.testTag("btn_toggle_custom_sliders")
                )
            }

            // Expandable RGB Sliders & Hex Input
            AnimatedVisibility(visible = showCustomSliders) {
                CustomRgbSliders(
                    currentColor = activeColor,
                    onColorChange = onActiveColorChange
                )
            }

            // Contrast & Readability Indicator
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = when (contrastLevel) {
                    QrCodeGenerator.ContrastLevel.HIGH -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    QrCodeGenerator.ContrastLevel.MODERATE -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    QrCodeGenerator.ContrastLevel.LOW -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("contrast_indicator_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (contrastLevel) {
                            QrCodeGenerator.ContrastLevel.HIGH -> Icons.Default.CheckCircle
                            QrCodeGenerator.ContrastLevel.MODERATE -> Icons.Default.CheckCircle
                            QrCodeGenerator.ContrastLevel.LOW -> Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = when (contrastLevel) {
                            QrCodeGenerator.ContrastLevel.HIGH -> MaterialTheme.colorScheme.primary
                            QrCodeGenerator.ContrastLevel.MODERATE -> MaterialTheme.colorScheme.tertiary
                            QrCodeGenerator.ContrastLevel.LOW -> MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        val ratioFormatted = String.format(Locale.ROOT, "%.1f:1", contrastRatio)
                        Text(
                            text = when (contrastLevel) {
                                QrCodeGenerator.ContrastLevel.HIGH -> "High Contrast ($ratioFormatted) • Optimal Scanning"
                                QrCodeGenerator.ContrastLevel.MODERATE -> "Moderate Contrast ($ratioFormatted) • Scannable"
                                QrCodeGenerator.ContrastLevel.LOW -> "Low Contrast ($ratioFormatted) • May be hard to scan"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = when (contrastLevel) {
                                QrCodeGenerator.ContrastLevel.HIGH -> MaterialTheme.colorScheme.onPrimaryContainer
                                QrCodeGenerator.ContrastLevel.MODERATE -> MaterialTheme.colorScheme.onTertiaryContainer
                                QrCodeGenerator.ContrastLevel.LOW -> MaterialTheme.colorScheme.onErrorContainer
                            }
                        )
                        if (contrastLevel == QrCodeGenerator.ContrastLevel.LOW) {
                            Text(
                                text = "Increase difference between QR dots and background for camera reliability.",
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomRgbSliders(
    currentColor: Int,
    onColorChange: (Int) -> Unit
) {
    val r = android.graphics.Color.red(currentColor)
    val g = android.graphics.Color.green(currentColor)
    val b = android.graphics.Color.blue(currentColor)

    var hexText by remember(currentColor) {
        mutableStateOf(String.format(Locale.ROOT, "%06X", 0xFFFFFF and currentColor))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Direct Hex input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Hex Color Code", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = hexText,
                onValueChange = { input ->
                    val clean = input.filter { it.isLetterOrDigit() }.take(6)
                    hexText = clean
                    if (clean.length == 6) {
                        try {
                            val parsed = android.graphics.Color.parseColor("#$clean")
                            onColorChange(parsed)
                        } catch (e: Exception) {
                            // ignore invalid hex
                        }
                    }
                },
                prefix = { Text("#") },
                singleLine = true,
                modifier = Modifier
                    .width(140.dp)
                    .testTag("input_hex_color")
            )
        }

        // Red Slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("R", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
            Slider(
                value = r.toFloat(),
                onValueChange = { newR ->
                    onColorChange(android.graphics.Color.rgb(newR.toInt(), g, b))
                },
                valueRange = 0f..255f,
                modifier = Modifier.weight(1f).testTag("slider_red")
            )
            Text("$r", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(36.dp))
        }

        // Green Slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("G", color = Color(0xFF059669), fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
            Slider(
                value = g.toFloat(),
                onValueChange = { newG ->
                    onColorChange(android.graphics.Color.rgb(r, newG.toInt(), b))
                },
                valueRange = 0f..255f,
                modifier = Modifier.weight(1f).testTag("slider_green")
            )
            Text("$g", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(36.dp))
        }

        // Blue Slider
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("B", color = Color(0xFF2563EB), fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
            Slider(
                value = b.toFloat(),
                onValueChange = { newB ->
                    onColorChange(android.graphics.Color.rgb(r, g, newB.toInt()))
                },
                valueRange = 0f..255f,
                modifier = Modifier.weight(1f).testTag("slider_blue")
            )
            Text("$b", style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(36.dp))
        }
    }
}

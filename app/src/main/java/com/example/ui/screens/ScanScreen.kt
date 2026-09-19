package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Wifi
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.util.QrCodeAnalyzer
import java.util.concurrent.Executors

@Composable
fun ScanScreen(
    onQrDetected: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            onShowMessage("Camera permission is needed for live scanner")
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (bitmap != null) {
                    val text = QrCodeAnalyzer.decodeFromBitmap(bitmap)
                    if (text != null && text.isNotBlank()) {
                        onQrDetected(text)
                    } else {
                        onShowMessage("No QR code found in selected image")
                    }
                } else {
                    onShowMessage("Unable to load selected image")
                }
            } catch (e: Exception) {
                onShowMessage("Error reading image: ${e.localizedMessage}")
            }
        }
    }

    var isTorchOn by remember { mutableStateOf(false) }
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.PERFORMANCE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    LaunchedEffect(hasCameraPermission, lensFacing, lifecycleOwner) {
        if (!hasCameraPermission) return@LaunchedEffect

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, QrCodeAnalyzer { qrText ->
                            onQrDetected(qrText)
                        })
                    }

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(lensFacing)
                    .build()

                cameraProvider.unbindAll()
                val boundCamera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
                camera = boundCamera
                if (isTorchOn) {
                    boundCamera.cameraControl.enableTorch(true)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    DisposableEffect(lifecycleOwner) {
        onDispose {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                if (cameraProviderFuture.isDone) {
                    cameraProviderFuture.get().unbindAll()
                }
            } catch (e: Exception) {
                // Ignore if unbind fails during disposal
            }
            camera = null
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("scan_screen")
    ) {
        if (hasCameraPermission) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize(),
                onRelease = {
                    try {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        if (cameraProviderFuture.isDone) {
                            cameraProviderFuture.get().unbindAll()
                        }
                    } catch (e: Exception) {
                        // ignore
                    }
                }
            )
        } else {
            // Permission Denied / Fallback View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.QrCode,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Camera Permission Needed",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Camera access is needed to scan QR codes in real-time. You can also pick an image from your gallery or test samples below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    modifier = Modifier.testTag("request_camera_perm_btn")
                ) {
                    Text("Grant Camera Access")
                }
            }
        }

        // Animated Scanner Reticle Overlay
        val infiniteTransition = rememberInfiniteTransition(label = "laser")
        val laserPosition by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "laser_pos"
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val boxSize = (canvasWidth * 0.75f).coerceAtMost(320.dp.toPx())
            val left = (canvasWidth - boxSize) / 2f
            val top = (canvasHeight - boxSize) / 2f - 40.dp.toPx()

            // Dim outer area
            drawRect(
                color = Color.Black.copy(alpha = 0.5f),
                size = Size(canvasWidth, canvasHeight)
            )

            // Transparent cut-out frame with rounded corners
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(boxSize, boxSize),
                cornerRadius = CornerRadius(20.dp.toPx(), 20.dp.toPx()),
                blendMode = androidx.compose.ui.graphics.BlendMode.Clear
            )

            // Target corners
            val cornerLength = 32.dp.toPx()
            val stroke = 4.dp.toPx()
            val primaryColor = Color(0xFF38BDF8) // Electric Cyan

            // Top-Left
            drawLine(primaryColor, Offset(left, top), Offset(left + cornerLength, top), stroke)
            drawLine(primaryColor, Offset(left, top), Offset(left, top + cornerLength), stroke)

            // Top-Right
            drawLine(primaryColor, Offset(left + boxSize, top), Offset(left + boxSize - cornerLength, top), stroke)
            drawLine(primaryColor, Offset(left + boxSize, top), Offset(left + boxSize, top + cornerLength), stroke)

            // Bottom-Left
            drawLine(primaryColor, Offset(left, top + boxSize), Offset(left + cornerLength, top + boxSize), stroke)
            drawLine(primaryColor, Offset(left, top + boxSize), Offset(left, top + boxSize - cornerLength), stroke)

            // Bottom-Right
            drawLine(primaryColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize - cornerLength, top + boxSize), stroke)
            drawLine(primaryColor, Offset(left + boxSize, top + boxSize), Offset(left + boxSize, top + boxSize - cornerLength), stroke)

            // Laser Scan Line
            val laserY = top + (boxSize * laserPosition)
            drawLine(
                color = primaryColor.copy(alpha = 0.85f),
                start = Offset(left + 10.dp.toPx(), laserY),
                end = Offset(left + boxSize - 10.dp.toPx(), laserY),
                strokeWidth = 3.dp.toPx()
            )
        }

        // Top Controls: Flashlight & Camera Switch
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp, start = 20.dp, end = 20.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(
                    onClick = {
                        val newTorch = !isTorchOn
                        isTorchOn = newTorch
                        camera?.cameraControl?.enableTorch(newTorch)
                    },
                    modifier = Modifier.testTag("toggle_flashlight_btn")
                ) {
                    Icon(
                        if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = "Torch",
                        tint = if (isTorchOn) Color(0xFFFBBF24) else Color.White
                    )
                }
            }

            Text(
                text = "Align QR code within frame",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )

            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.6f),
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(
                    onClick = {
                        isTorchOn = false
                        lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                            CameraSelector.LENS_FACING_FRONT
                        } else {
                            CameraSelector.LENS_FACING_BACK
                        }
                    },
                    modifier = Modifier.testTag("switch_camera_btn")
                ) {
                    Icon(
                        Icons.Default.Cameraswitch,
                        contentDescription = "Switch Camera",
                        tint = Color.White
                    )
                }
            }
        }

        // Bottom Controls Bar: Pick Image
        Surface(
            color = Color.Black.copy(alpha = 0.75f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pick from Gallery
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pick_gallery_image_btn")
                ) {
                    Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pick Image", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.example.ui.screens

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.QrCode
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.HistoryFilter
import com.example.ui.viewmodel.QrViewModel
import com.example.ui.viewmodel.ThemeMode
import com.example.util.QrCodeAnalyzer
import kotlinx.coroutines.flow.collectLatest

enum class ScreenTab {
    DASHBOARD, SCAN, GENERATE, HISTORY
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: QrViewModel,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableStateOf(ScreenTab.DASHBOARD) }

    // Listen for toast/feedback messages
    LaunchedEffect(Unit) {
        viewModel.userMessage.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Photo picker launcher accessible from dashboard or any tab
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
                        viewModel.onQrScanned(text)
                    } else {
                        viewModel.showMessage("No QR code found in selected image")
                    }
                } else {
                    viewModel.showMessage("Unable to load selected image")
                }
            } catch (e: Exception) {
                viewModel.showMessage("Error reading image: ${e.localizedMessage}")
            }
        }
    }

    // Photo picker for Custom Logo Overlay
    val logoPhotoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.setCustomLogoUri(context, uri)
        }
    }

    // Active payload to decrypt in DecryptDialog
    var decryptPayloadTarget by remember { mutableStateOf<String?>(null) }

    // State collections
    val scannedCount by viewModel.scannedCount.collectAsStateWithLifecycle()
    val generatedCount by viewModel.generatedCount.collectAsStateWithLifecycle()
    val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()
    val encryptedCount by viewModel.encryptedCount.collectAsStateWithLifecycle()
    val recentItems by viewModel.recentItems.collectAsStateWithLifecycle()
    val filteredHistory by viewModel.filteredHistory.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val scanResultDialog by viewModel.scanResultDialog.collectAsStateWithLifecycle()
    val selectedDetailItem by viewModel.selectedDetailItem.collectAsStateWithLifecycle()

    // Generator states
    val genType by viewModel.genType.collectAsStateWithLifecycle()
    val genText by viewModel.genText.collectAsStateWithLifecycle()
    val genUrl by viewModel.genUrl.collectAsStateWithLifecycle()
    val wifiSsid by viewModel.wifiSsid.collectAsStateWithLifecycle()
    val wifiPass by viewModel.wifiPassword.collectAsStateWithLifecycle()
    val wifiType by viewModel.wifiType.collectAsStateWithLifecycle()
    val wifiHidden by viewModel.wifiHidden.collectAsStateWithLifecycle()
    val contactName by viewModel.contactName.collectAsStateWithLifecycle()
    val contactPhone by viewModel.contactPhone.collectAsStateWithLifecycle()
    val contactEmail by viewModel.contactEmail.collectAsStateWithLifecycle()
    val contactOrg by viewModel.contactOrg.collectAsStateWithLifecycle()
    val phoneNumber by viewModel.phoneNumber.collectAsStateWithLifecycle()
    val smsMessage by viewModel.smsMessage.collectAsStateWithLifecycle()
    val emailTo by viewModel.emailTo.collectAsStateWithLifecycle()
    val emailSubject by viewModel.emailSubject.collectAsStateWithLifecycle()
    val emailBody by viewModel.emailBody.collectAsStateWithLifecycle()
    val isEncrypted by viewModel.genIsEncrypted.collectAsStateWithLifecycle()
    val passphrase by viewModel.genPassphrase.collectAsStateWithLifecycle()
    val fgColor by viewModel.genForegroundColor.collectAsStateWithLifecycle()
    val bgColor by viewModel.genBackgroundColor.collectAsStateWithLifecycle()
    val errorCorrection by viewModel.genErrorCorrection.collectAsStateWithLifecycle()

    val selectedLogoPreset by viewModel.selectedLogoPreset.collectAsStateWithLifecycle()
    val customLogoBitmap by viewModel.customLogoBitmap.collectAsStateWithLifecycle()
    val selectedLogoShape by viewModel.selectedLogoShape.collectAsStateWithLifecycle()
    val selectedLogoSize by viewModel.selectedLogoSize.collectAsStateWithLifecycle()

    val generatedBitmap by viewModel.generatedBitmap.collectAsStateWithLifecycle()
    val generatedRawContent by viewModel.generatedRawContent.collectAsStateWithLifecycle()
    val generatedTitle by viewModel.generatedTitle.collectAsStateWithLifecycle()

    val decryptedResult by viewModel.decryptedResult.collectAsStateWithLifecycle()
    val decryptError by viewModel.decryptError.collectAsStateWithLifecycle()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            ScreenTab.DASHBOARD -> "QR Code Studio"
                            ScreenTab.SCAN -> "Scanner"
                            ScreenTab.GENERATE -> "Create QR Code"
                            ScreenTab.HISTORY -> "History & Vault"
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(
                        onClick = onToggleTheme,
                        modifier = Modifier.testTag("theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = currentTab == ScreenTab.DASHBOARD,
                    onClick = { currentTab = ScreenTab.DASHBOARD },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.DASHBOARD) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard") },
                    modifier = Modifier.testTag("nav_tab_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.SCAN,
                    onClick = { currentTab = ScreenTab.SCAN },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.SCAN) Icons.Default.QrCodeScanner else Icons.Outlined.QrCodeScanner,
                            contentDescription = "Scan"
                        )
                    },
                    label = { Text("Scan") },
                    modifier = Modifier.testTag("nav_tab_scan")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.GENERATE,
                    onClick = { currentTab = ScreenTab.GENERATE },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.GENERATE) Icons.Default.QrCode else Icons.Outlined.QrCode,
                            contentDescription = "Generate"
                        )
                    },
                    label = { Text("Generate") },
                    modifier = Modifier.testTag("nav_tab_generate")
                )
                NavigationBarItem(
                    selected = currentTab == ScreenTab.HISTORY,
                    onClick = { currentTab = ScreenTab.HISTORY },
                    icon = {
                        Icon(
                            if (currentTab == ScreenTab.HISTORY) Icons.Default.History else Icons.Outlined.History,
                            contentDescription = "History"
                        )
                    },
                    label = { Text("History") },
                    modifier = Modifier.testTag("nav_tab_history")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                ScreenTab.DASHBOARD -> {
                    DashboardScreen(
                        scannedCount = scannedCount,
                        generatedCount = generatedCount,
                        favoriteCount = favoriteCount,
                        encryptedCount = encryptedCount,
                        recentItems = recentItems,
                        onNavigateToScan = { currentTab = ScreenTab.SCAN },
                        onNavigateToGenerate = { currentTab = ScreenTab.GENERATE },
                        onNavigateToHistory = { filter ->
                            viewModel.setHistoryFilter(filter)
                            currentTab = ScreenTab.HISTORY
                        },
                        onSelectItem = { item -> viewModel.selectDetailItem(item) },
                        onPickImageToScan = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onShowMessage = { viewModel.showMessage(it) }
                    )
                }
                ScreenTab.SCAN -> {
                    ScanScreen(
                        onQrDetected = { raw ->
                            viewModel.onQrScanned(raw)
                        },
                        onShowMessage = { viewModel.showMessage(it) }
                    )
                }
                ScreenTab.GENERATE -> {
                    GenerateScreen(
                        genType = genType,
                        onGenTypeChange = { viewModel.setGenType(it) },
                        text = genText,
                        onTextChange = { viewModel.setGenText(it) },
                        url = genUrl,
                        onUrlChange = { viewModel.setGenUrl(it) },
                        wifiSsid = wifiSsid,
                        onWifiSsidChange = { viewModel.setWifiSsid(it) },
                        wifiPass = wifiPass,
                        onWifiPassChange = { viewModel.setWifiPassword(it) },
                        wifiType = wifiType,
                        onWifiTypeChange = { viewModel.setWifiType(it) },
                        wifiHidden = wifiHidden,
                        onWifiHiddenChange = { viewModel.setWifiHidden(it) },
                        contactName = contactName,
                        onContactNameChange = { viewModel.setContactName(it) },
                        contactPhone = contactPhone,
                        onContactPhoneChange = { viewModel.setContactPhone(it) },
                        contactEmail = contactEmail,
                        onContactEmailChange = { viewModel.setContactEmail(it) },
                        contactOrg = contactOrg,
                        onContactOrgChange = { viewModel.setContactOrg(it) },
                        phoneNumber = phoneNumber,
                        onPhoneNumberChange = { viewModel.setPhoneNumber(it) },
                        smsMessage = smsMessage,
                        onSmsMessageChange = { viewModel.setSmsMessage(it) },
                        emailTo = emailTo,
                        onEmailToChange = { viewModel.setEmailTo(it) },
                        emailSubject = emailSubject,
                        onEmailSubjectChange = { viewModel.setEmailSubject(it) },
                        emailBody = emailBody,
                        onEmailBodyChange = { viewModel.setEmailBody(it) },
                        isEncrypted = isEncrypted,
                        onIsEncryptedChange = { viewModel.setGenIsEncrypted(it) },
                        passphrase = passphrase,
                        onPassphraseChange = { viewModel.setGenPassphrase(it) },
                        foregroundColor = fgColor,
                        onForegroundColorChange = { viewModel.setGenForegroundColor(it) },
                        backgroundColor = bgColor,
                        onBackgroundColorChange = { viewModel.setGenBackgroundColor(it) },
                        onSwapColors = { viewModel.swapColors() },
                        onResetColors = { viewModel.resetColors() },
                        errorCorrection = errorCorrection,
                        onErrorCorrectionChange = { viewModel.setGenErrorCorrection(it) },
                        selectedLogoPreset = selectedLogoPreset,
                        customLogoBitmap = customLogoBitmap,
                        selectedLogoShape = selectedLogoShape,
                        selectedLogoSize = selectedLogoSize,
                        onLogoPresetChange = { viewModel.setLogoPreset(it) },
                        onLogoShapeChange = { viewModel.setLogoShape(it) },
                        onLogoSizeChange = { viewModel.setLogoSize(it) },
                        onPickCustomImage = {
                            logoPhotoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onClearCustomImage = { viewModel.clearCustomLogo() },
                        generatedBitmap = generatedBitmap,
                        generatedRawContent = generatedRawContent,
                        generatedTitle = generatedTitle,
                        onGenerateClick = { viewModel.generateQrCode() },
                        onShowMessage = { viewModel.showMessage(it) }
                    )
                }
                ScreenTab.HISTORY -> {
                    HistoryScreen(
                        items = filteredHistory,
                        selectedFilter = selectedFilter,
                        onFilterChange = { viewModel.setHistoryFilter(it) },
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        onSelectItem = { item -> viewModel.selectDetailItem(item) },
                        onToggleFavorite = { item -> viewModel.toggleFavorite(item) },
                        onDeleteItem = { item -> viewModel.deleteItem(item) },
                        onDeleteAll = { viewModel.deleteAllHistory() },
                        onShowMessage = { viewModel.showMessage(it) }
                    )
                }
            }
        }
    }

    // Active Result Dialog after a QR code is scanned
    scanResultDialog?.let { result ->
        ScanResultDialog(
            result = result,
            onDismiss = { viewModel.dismissScanResult() },
            onOpenDecrypt = { payload ->
                viewModel.clearDecryption()
                decryptPayloadTarget = payload
            },
            onShowMessage = { viewModel.showMessage(it) }
        )
    }

    // Active QrDetailDialog when clicking on any QR code
    selectedDetailItem?.let { item ->
        QrDetailDialog(
            item = item,
            onDismiss = { viewModel.selectDetailItem(null) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDelete = { viewModel.deleteItem(it) },
            onOpenDecrypt = { payload ->
                viewModel.clearDecryption()
                decryptPayloadTarget = payload
            },
            onExportPng = { ctx, itm, onExported ->
                viewModel.exportPng(ctx, itm, onExported)
            },
            onExportPdf = { ctx, itm, onExported ->
                viewModel.exportPdf(ctx, itm, onExported)
            },
            onShowMessage = { viewModel.showMessage(it) }
        )
    }

    // Active Decrypt Dialog when unlocking an encrypted QR code
    decryptPayloadTarget?.let { payload ->
        DecryptDialog(
            payload = payload,
            decryptedResult = decryptedResult,
            errorMessage = decryptError,
            onDecrypt = { pass ->
                viewModel.decryptPayload(payload, pass)
            },
            onDismiss = {
                decryptPayloadTarget = null
                viewModel.clearDecryption()
            },
            onShowMessage = { viewModel.showMessage(it) }
        )
    }
}

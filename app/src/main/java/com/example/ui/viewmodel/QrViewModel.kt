package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.QrDatabase
import com.example.data.QrEntity
import com.example.data.QrRepository
import com.example.model.LogoPreset
import com.example.model.LogoShape
import com.example.model.LogoSize
import com.example.model.ParsedQrResult
import com.example.model.QrType
import com.example.util.CryptoManager
import com.example.util.FileExporter
import com.example.util.LogoRenderer
import com.example.util.QrCodeGenerator
import com.example.util.QrParser
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

enum class HistoryFilter {
    ALL, SCANNED, GENERATED, FAVORITES, ENCRYPTED
}

class QrViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QrRepository

    init {
        val database = QrDatabase.getDatabase(application)
        repository = QrRepository(database.qrDao())
    }

    // Theme Mode
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    // Dashboard Stats
    val scannedCount: StateFlow<Int> = repository.scannedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val generatedCount: StateFlow<Int> = repository.generatedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val favoriteCount: StateFlow<Int> = repository.favoriteCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val encryptedCount: StateFlow<Int> = repository.encryptedCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // History filter and search
    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allHistory: StateFlow<List<QrEntity>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredHistory: StateFlow<List<QrEntity>> = combine(
        repository.allItems,
        _historyFilter,
        _searchQuery
    ) { items, filter, query ->
        val byFilter = when (filter) {
            HistoryFilter.ALL -> items
            HistoryFilter.SCANNED -> items.filter { !it.isGenerated }
            HistoryFilter.GENERATED -> items.filter { it.isGenerated }
            HistoryFilter.FAVORITES -> items.filter { it.isFavorite }
            HistoryFilter.ENCRYPTED -> items.filter { it.isEncrypted }
        }
        if (query.isBlank()) {
            byFilter
        } else {
            val q = query.trim().lowercase()
            byFilter.filter {
                it.title.lowercase().contains(q) ||
                it.content.lowercase().contains(q) ||
                it.qrType.lowercase().contains(q)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentItems: StateFlow<List<QrEntity>> = repository.allItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Feedback channel
    private val _userMessage = MutableSharedFlow<String>()
    val userMessage = _userMessage.asSharedFlow()

    fun showMessage(msg: String) {
        viewModelScope.launch {
            _userMessage.emit(msg)
        }
    }

    // Active detail item
    private val _selectedDetailItem = MutableStateFlow<QrEntity?>(null)
    val selectedDetailItem: StateFlow<QrEntity?> = _selectedDetailItem.asStateFlow()

    fun selectDetailItem(item: QrEntity?) {
        _selectedDetailItem.value = item
        _decryptedResult.value = null
        _decryptError.value = null
    }

    // Scanner state
    private val _scanResultDialog = MutableStateFlow<ParsedQrResult?>(null)
    val scanResultDialog: StateFlow<ParsedQrResult?> = _scanResultDialog.asStateFlow()

    fun dismissScanResult() {
        _scanResultDialog.value = null
    }

    fun onQrScanned(rawContent: String) {
        if (rawContent.isBlank()) return
        vibratePhone()
        val parsed = QrParser.parse(rawContent)
        _scanResultDialog.value = parsed

        // Automatically store scanned item in Room database for offline access
        viewModelScope.launch {
            val entity = QrEntity(
                content = rawContent,
                title = parsed.displayTitle,
                qrType = parsed.type.name,
                isGenerated = false,
                isEncrypted = parsed.isEncrypted,
                isFavorite = false,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(entity)
        }
    }

    private fun vibratePhone() {
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(80, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(80)
                }
            }
        } catch (_: Exception) {}
    }

    // Decryption State
    private val _decryptedResult = MutableStateFlow<String?>(null)
    val decryptedResult: StateFlow<String?> = _decryptedResult.asStateFlow()

    private val _decryptError = MutableStateFlow<String?>(null)
    val decryptError: StateFlow<String?> = _decryptError.asStateFlow()

    fun decryptPayload(payload: String, passphrase: String) {
        if (passphrase.isEmpty()) {
            _decryptError.value = "Please enter a passphrase"
            return
        }
        val result = CryptoManager.decrypt(payload, passphrase.toCharArray())
        if (result.isSuccess) {
            _decryptedResult.value = result.getOrNull()
            _decryptError.value = null
            showMessage("Successfully decrypted!")
        } else {
            _decryptedResult.value = null
            _decryptError.value = "Incorrect password or invalid payload"
        }
    }

    fun clearDecryption() {
        _decryptedResult.value = null
        _decryptError.value = null
    }

    // Generator State
    private val _genType = MutableStateFlow(QrType.TEXT)
    val genType: StateFlow<QrType> = _genType.asStateFlow()

    private val _genText = MutableStateFlow("")
    val genText: StateFlow<String> = _genText.asStateFlow()

    private val _genUrl = MutableStateFlow("https://")
    val genUrl: StateFlow<String> = _genUrl.asStateFlow()

    // Wifi fields
    private val _wifiSsid = MutableStateFlow("")
    val wifiSsid: StateFlow<String> = _wifiSsid.asStateFlow()
    private val _wifiPassword = MutableStateFlow("")
    val wifiPassword: StateFlow<String> = _wifiPassword.asStateFlow()
    private val _wifiType = MutableStateFlow("WPA") // WPA, WEP, None
    val wifiType: StateFlow<String> = _wifiType.asStateFlow()
    private val _wifiHidden = MutableStateFlow(false)
    val wifiHidden: StateFlow<Boolean> = _wifiHidden.asStateFlow()

    // Contact fields
    private val _contactName = MutableStateFlow("")
    val contactName: StateFlow<String> = _contactName.asStateFlow()
    private val _contactPhone = MutableStateFlow("")
    val contactPhone: StateFlow<String> = _contactPhone.asStateFlow()
    private val _contactEmail = MutableStateFlow("")
    val contactEmail: StateFlow<String> = _contactEmail.asStateFlow()
    private val _contactOrg = MutableStateFlow("")
    val contactOrg: StateFlow<String> = _contactOrg.asStateFlow()

    // Phone / SMS / Email fields
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber: StateFlow<String> = _phoneNumber.asStateFlow()
    private val _smsMessage = MutableStateFlow("")
    val smsMessage: StateFlow<String> = _smsMessage.asStateFlow()
    private val _emailTo = MutableStateFlow("")
    val emailTo: StateFlow<String> = _emailTo.asStateFlow()
    private val _emailSubject = MutableStateFlow("")
    val emailSubject: StateFlow<String> = _emailSubject.asStateFlow()
    private val _emailBody = MutableStateFlow("")
    val emailBody: StateFlow<String> = _emailBody.asStateFlow()

    // Encryption toggle & passphrase for generator
    private val _genIsEncrypted = MutableStateFlow(false)
    val genIsEncrypted: StateFlow<Boolean> = _genIsEncrypted.asStateFlow()
    private val _genPassphrase = MutableStateFlow("")
    val genPassphrase: StateFlow<String> = _genPassphrase.asStateFlow()

    // Styling & Personalization
    private val _genForegroundColor = MutableStateFlow(0xFF000000.toInt())
    val genForegroundColor: StateFlow<Int> = _genForegroundColor.asStateFlow()

    private val _genBackgroundColor = MutableStateFlow(0xFFFFFFFF.toInt())
    val genBackgroundColor: StateFlow<Int> = _genBackgroundColor.asStateFlow()

    private val _genErrorCorrection = MutableStateFlow(ErrorCorrectionLevel.M)
    val genErrorCorrection: StateFlow<ErrorCorrectionLevel> = _genErrorCorrection.asStateFlow()

    // Logo Overlay States
    private val _selectedLogoPreset = MutableStateFlow(LogoPreset.NONE)
    val selectedLogoPreset: StateFlow<LogoPreset> = _selectedLogoPreset.asStateFlow()

    private val _customLogoBitmap = MutableStateFlow<Bitmap?>(null)
    val customLogoBitmap: StateFlow<Bitmap?> = _customLogoBitmap.asStateFlow()

    private val _selectedLogoShape = MutableStateFlow(LogoShape.ROUNDED_RECT)
    val selectedLogoShape: StateFlow<LogoShape> = _selectedLogoShape.asStateFlow()

    private val _selectedLogoSize = MutableStateFlow(LogoSize.STANDARD)
    val selectedLogoSize: StateFlow<LogoSize> = _selectedLogoSize.asStateFlow()

    // Generated QR Preview
    private val _generatedBitmap = MutableStateFlow<Bitmap?>(null)
    val generatedBitmap: StateFlow<Bitmap?> = _generatedBitmap.asStateFlow()

    private val _generatedRawContent = MutableStateFlow("")
    val generatedRawContent: StateFlow<String> = _generatedRawContent.asStateFlow()

    private val _generatedTitle = MutableStateFlow("")
    val generatedTitle: StateFlow<String> = _generatedTitle.asStateFlow()

    fun setGenType(type: QrType) {
        _genType.value = type
    }

    fun setGenText(text: String) { _genText.value = text }
    fun setGenUrl(url: String) { _genUrl.value = url }
    fun setWifiSsid(ssid: String) { _wifiSsid.value = ssid }
    fun setWifiPassword(pass: String) { _wifiPassword.value = pass }
    fun setWifiType(type: String) { _wifiType.value = type }
    fun setWifiHidden(hidden: Boolean) { _wifiHidden.value = hidden }

    fun setContactName(name: String) { _contactName.value = name }
    fun setContactPhone(phone: String) { _contactPhone.value = phone }
    fun setContactEmail(email: String) { _contactEmail.value = email }
    fun setContactOrg(org: String) { _contactOrg.value = org }

    fun setPhoneNumber(num: String) { _phoneNumber.value = num }
    fun setSmsMessage(msg: String) { _smsMessage.value = msg }
    fun setEmailTo(to: String) { _emailTo.value = to }
    fun setEmailSubject(sub: String) { _emailSubject.value = sub }
    fun setEmailBody(body: String) { _emailBody.value = body }

    fun setGenIsEncrypted(enabled: Boolean) { _genIsEncrypted.value = enabled }
    fun setGenPassphrase(pass: String) { _genPassphrase.value = pass }

    fun setGenForegroundColor(color: Int) {
        _genForegroundColor.value = color
        refreshGeneratedPreviewIfActive()
    }

    fun setGenBackgroundColor(color: Int) {
        _genBackgroundColor.value = color
        refreshGeneratedPreviewIfActive()
    }

    fun swapColors() {
        val temp = _genForegroundColor.value
        _genForegroundColor.value = _genBackgroundColor.value
        _genBackgroundColor.value = temp
        refreshGeneratedPreviewIfActive()
    }

    fun resetColors() {
        _genForegroundColor.value = 0xFF000000.toInt()
        _genBackgroundColor.value = 0xFFFFFFFF.toInt()
        refreshGeneratedPreviewIfActive()
    }

    fun setGenErrorCorrection(level: ErrorCorrectionLevel) {
        _genErrorCorrection.value = level
        refreshGeneratedPreviewIfActive()
    }

    fun setLogoPreset(preset: LogoPreset) {
        _selectedLogoPreset.value = preset
        refreshGeneratedPreviewIfActive()
    }

    fun setCustomLogoBitmap(bitmap: Bitmap?) {
        _customLogoBitmap.value = bitmap
        if (bitmap != null) {
            _selectedLogoPreset.value = LogoPreset.CUSTOM
        }
        refreshGeneratedPreviewIfActive()
    }

    fun setCustomLogoUri(context: Context, uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bmp = LogoRenderer.decodeUriToSquareBitmap(context, uri, targetSize = 300)
            withContext(Dispatchers.Main) {
                if (bmp != null) {
                    _customLogoBitmap.value = bmp
                    _selectedLogoPreset.value = LogoPreset.CUSTOM
                    refreshGeneratedPreviewIfActive()
                    showMessage("Custom logo loaded successfully")
                } else {
                    showMessage("Failed to load image as logo")
                }
            }
        }
    }

    fun clearCustomLogo() {
        _customLogoBitmap.value = null
        if (_selectedLogoPreset.value == LogoPreset.CUSTOM) {
            _selectedLogoPreset.value = LogoPreset.NONE
        }
        refreshGeneratedPreviewIfActive()
    }

    fun setLogoShape(shape: LogoShape) {
        _selectedLogoShape.value = shape
        refreshGeneratedPreviewIfActive()
    }

    fun setLogoSize(size: LogoSize) {
        _selectedLogoSize.value = size
        refreshGeneratedPreviewIfActive()
    }

    private fun resolveActiveLogoBitmap(): Bitmap? {
        return when (_selectedLogoPreset.value) {
            LogoPreset.NONE -> null
            LogoPreset.CUSTOM -> _customLogoBitmap.value
            else -> LogoRenderer.renderPresetBitmap(
                preset = _selectedLogoPreset.value,
                sizePx = 180,
                tintColor = _genForegroundColor.value
            )
        }
    }

    private fun refreshGeneratedPreviewIfActive() {
        val payload = _generatedRawContent.value
        if (payload.isNotEmpty()) {
            val activeLogo = resolveActiveLogoBitmap()
            val bmp = QrCodeGenerator.generateBitmap(
                content = payload,
                size = 700,
                foregroundColor = _genForegroundColor.value,
                backgroundColor = _genBackgroundColor.value,
                errorCorrectionLevel = if (activeLogo != null) ErrorCorrectionLevel.H else _genErrorCorrection.value,
                logoBitmap = activeLogo,
                logoSizeRatio = _selectedLogoSize.value.ratio,
                logoShape = _selectedLogoShape.value,
                logoBadgeColor = _genBackgroundColor.value
            )
            if (bmp != null) {
                _generatedBitmap.value = bmp
            }
        }
    }

    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun generateQrCode() {
        val (raw, title) = buildRawPayload()
        if (raw.isBlank()) {
            showMessage("Please fill in the required fields")
            return
        }

        val finalPayload = if (_genIsEncrypted.value) {
            if (_genPassphrase.value.isBlank()) {
                showMessage("Passphrase is required for encryption")
                return
            }
            CryptoManager.encrypt(raw, _genPassphrase.value.toCharArray())
        } else {
            raw
        }

        val activeLogo = resolveActiveLogoBitmap()
        val bmp = QrCodeGenerator.generateBitmap(
            content = finalPayload,
            size = 700,
            foregroundColor = _genForegroundColor.value,
            backgroundColor = _genBackgroundColor.value,
            errorCorrectionLevel = if (activeLogo != null) ErrorCorrectionLevel.H else _genErrorCorrection.value,
            logoBitmap = activeLogo,
            logoSizeRatio = _selectedLogoSize.value.ratio,
            logoShape = _selectedLogoShape.value,
            logoBadgeColor = _genBackgroundColor.value
        )

        if (bmp != null) {
            _generatedBitmap.value = bmp
            _generatedRawContent.value = finalPayload
            _generatedTitle.value = if (_genIsEncrypted.value) "[Encrypted] $title" else title

            // Save to Room DB as generated item
            viewModelScope.launch {
                val entity = QrEntity(
                    content = finalPayload,
                    title = _generatedTitle.value,
                    qrType = if (_genIsEncrypted.value) QrType.ENCRYPTED.name else _genType.value.name,
                    isGenerated = true,
                    isEncrypted = _genIsEncrypted.value,
                    isFavorite = false,
                    timestamp = System.currentTimeMillis(),
                    foregroundColor = _genForegroundColor.value,
                    backgroundColor = _genBackgroundColor.value
                )
                repository.insert(entity)
                showMessage("QR Code created & saved to history!")
            }
        } else {
            showMessage("Failed to generate QR Code. Text may be too large.")
        }
    }

    private fun buildRawPayload(): Pair<String, String> {
        return when (_genType.value) {
            QrType.TEXT -> {
                val text = _genText.value.trim()
                val title = text.lines().firstOrNull()?.take(30) ?: "Text Note"
                Pair(text, title.ifEmpty { "Text Note" })
            }
            QrType.URL -> {
                val url = _genUrl.value.trim()
                Pair(url, url.substringBefore("?").take(35).ifEmpty { "Website Link" })
            }
            QrType.WIFI -> {
                val wifiStr = QrParser.buildWifiString(
                    _wifiSsid.value.trim(),
                    _wifiPassword.value.trim(),
                    _wifiType.value,
                    _wifiHidden.value
                )
                Pair(wifiStr, "Wi-Fi: ${_wifiSsid.value.ifEmpty { "Network" }}")
            }
            QrType.CONTACT -> {
                val vcard = QrParser.buildVCard(
                    _contactName.value.trim(),
                    _contactPhone.value.trim(),
                    _contactEmail.value.trim(),
                    _contactOrg.value.trim()
                )
                Pair(vcard, "Contact: ${_contactName.value.ifEmpty { "Card" }}")
            }
            QrType.PHONE -> {
                val phone = "tel:${_phoneNumber.value.trim()}"
                Pair(phone, "Call ${_phoneNumber.value.ifEmpty { "Phone" }}")
            }
            QrType.SMS -> {
                val sms = "smsto:${_phoneNumber.value.trim()}:${_smsMessage.value.trim()}"
                Pair(sms, "SMS to ${_phoneNumber.value.ifEmpty { "Number" }}")
            }
            QrType.EMAIL -> {
                val mail = "mailto:${_emailTo.value.trim()}?subject=${Uri.encode(_emailSubject.value)}&body=${Uri.encode(_emailBody.value)}"
                Pair(mail, "Email: ${_emailTo.value.ifEmpty { "Message" }}")
            }
            QrType.GEO -> {
                val geo = "geo:${_genText.value.trim()}"
                Pair(geo, "Location Coordinates")
            }
            QrType.ENCRYPTED -> {
                val text = _genText.value.trim()
                Pair(text, "Secret Note")
            }
        }
    }

    // Database Actions
    fun toggleFavorite(item: QrEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(item.id, !item.isFavorite)
        }
    }

    fun deleteItem(item: QrEntity) {
        viewModelScope.launch {
            repository.delete(item)
            if (_selectedDetailItem.value?.id == item.id) {
                _selectedDetailItem.value = null
            }
            showMessage("Item deleted")
        }
    }

    fun deleteAllHistory() {
        viewModelScope.launch {
            repository.deleteAll()
            _selectedDetailItem.value = null
            showMessage("History cleared")
        }
    }

    // Export PNG
    fun exportPng(context: Context, item: QrEntity, onExported: (Uri) -> Unit) {
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.Default) {
                QrCodeGenerator.generateBitmap(
                    content = item.content,
                    size = 1000,
                    foregroundColor = item.foregroundColor,
                    backgroundColor = item.backgroundColor
                )
            }
            if (bmp != null) {
                val uri = FileExporter.exportToPng(context, bmp, item.title)
                if (uri != null) {
                    onExported(uri)
                } else {
                    showMessage("Failed to export PNG")
                }
            }
        }
    }

    // Export PDF
    fun exportPdf(context: Context, item: QrEntity, onExported: (Uri) -> Unit) {
        viewModelScope.launch {
            val bmp = withContext(Dispatchers.Default) {
                QrCodeGenerator.generateBitmap(
                    content = item.content,
                    size = 800,
                    foregroundColor = item.foregroundColor,
                    backgroundColor = item.backgroundColor
                )
            }
            if (bmp != null) {
                val uri = FileExporter.exportToPdf(
                    context = context,
                    bitmap = bmp,
                    title = item.title,
                    type = item.qrType,
                    content = item.content,
                    isEncrypted = item.isEncrypted
                )
                if (uri != null) {
                    onExported(uri)
                } else {
                    showMessage("Failed to export PDF")
                }
            }
        }
    }
}

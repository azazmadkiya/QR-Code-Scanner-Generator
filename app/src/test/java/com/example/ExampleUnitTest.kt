package com.example

import android.graphics.Color
import com.example.model.LogoPreset
import com.example.model.LogoShape
import com.example.model.LogoSize
import com.example.util.CryptoManager
import com.example.util.LogoRenderer
import com.example.util.QrCodeGenerator
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testContrastCalculation() {
        val black = Color.BLACK
        val white = Color.WHITE
        val ratio = QrCodeGenerator.calculateContrastRatio(black, white)
        assertTrue("Black and white contrast ratio should be high (~21:1)", ratio > 15.0)

        val contrastLevel = QrCodeGenerator.getContrastLevel(black, white)
        assertEquals(QrCodeGenerator.ContrastLevel.HIGH, contrastLevel)
    }

    @Test
    fun testQrGenerationWithColorsAndLogoPreset() {
        val preset = LogoPreset.WIFI
        val logoBmp = LogoRenderer.renderPresetBitmap(preset, sizePx = 100, tintColor = Color.BLACK)
        assertNotNull("Logo preset bitmap should be generated", logoBmp)

        val qrBitmap = QrCodeGenerator.generateBitmap(
            content = "https://example.com/test",
            size = 400,
            foregroundColor = Color.BLUE,
            backgroundColor = Color.WHITE,
            logoBitmap = logoBmp,
            logoShape = LogoShape.ROUNDED_RECT,
            logoSizeRatio = LogoSize.STANDARD.ratio
        )
        assertNotNull("QR code with logo should be generated", qrBitmap)
        assertEquals(400, qrBitmap?.width)
        assertEquals(400, qrBitmap?.height)
    }

    @Test
    fun testEncryptionRoundTrip() {
        val original = "Secret Wi-Fi Password 1234"
        val password = "StrongPassphrase!".toCharArray()

        val encrypted = CryptoManager.encrypt(original, password)
        assertNotEquals(original, encrypted)
        assertTrue(encrypted.startsWith(CryptoManager.PREFIX))

        val decryptedResult = CryptoManager.decrypt(encrypted, password)
        assertTrue(decryptedResult.isSuccess)
        assertEquals(original, decryptedResult.getOrNull())
    }
}


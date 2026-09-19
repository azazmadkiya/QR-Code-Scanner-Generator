package com.example

import android.content.Context
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import com.example.model.QrType
import com.example.util.CryptoManager
import com.example.util.FileExporter
import com.example.util.QrCodeGenerator
import com.example.util.QrParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("QR Code", appName)
  }

  @Test
  fun `crypto manager encrypts and decrypts accurately`() {
    val secretData = "Super secret wifi password: MySecretPass2026!"
    val passphrase = "correct_horse_battery".toCharArray()

    val encrypted = CryptoManager.encrypt(secretData, passphrase)
    assertTrue(encrypted.startsWith(CryptoManager.PREFIX))
    assertTrue(CryptoManager.isEncryptedPayload(encrypted))

    // Decrypt with matching passphrase
    val decryptedResult = CryptoManager.decrypt(encrypted, passphrase)
    assertTrue(decryptedResult.isSuccess)
    assertEquals(secretData, decryptedResult.getOrNull())

    // Decrypt with incorrect passphrase must fail
    val failedResult = CryptoManager.decrypt(encrypted, "wrong_passphrase".toCharArray())
    assertTrue(failedResult.isFailure)
  }

  @Test
  fun `qr parser parses wifi payload`() {
    val wifiString = "WIFI:S:HomeNetwork;T:WPA;P:MyPassword123;H:false;;"
    val result = QrParser.parse(wifiString)

    assertEquals(QrType.WIFI, result.type)
    assertEquals("HomeNetwork", result.details["SSID"])
    assertEquals("MyPassword123", result.details["Password"])
    assertEquals("WPA", result.details["Security"])
    assertFalse(result.isEncrypted)
  }

  @Test
  fun `qr parser parses web url`() {
    val urlString = "https://ai.google.dev/android"
    val result = QrParser.parse(urlString)

    assertEquals(QrType.URL, result.type)
    assertEquals("https://ai.google.dev/android", result.rawContent)
    assertFalse(result.isEncrypted)
  }

  @Test
  fun `qr code generator produces valid bitmap`() {
    val bitmap = QrCodeGenerator.generateBitmap(
      content = "https://example.com",
      size = 200
    )
    assertNotNull(bitmap)
    assertEquals(200, bitmap?.width)
    assertEquals(200, bitmap?.height)
  }

  @Test
  fun `file exporter saves bitmap to gallery`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val bitmap = Bitmap.createBitmap(120, 120, Bitmap.Config.ARGB_8888)
    val result = FileExporter.saveToGallery(context, bitmap, "TestQR")
    assertTrue(result.isSuccess)
    assertNotNull(result.getOrNull())
  }
}

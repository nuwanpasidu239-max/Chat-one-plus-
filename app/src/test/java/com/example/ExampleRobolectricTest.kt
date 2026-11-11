package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.telechat.data.ChatRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("TeleChat", appName)
  }

  @Test
  fun `test spam auto block after 5 messages from unknown number`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repository = ChatRepository(context)

    val unknownPhone = "+94 77 999 0000"
    for (i in 1..5) {
      repository.receiveIncomingMessage(unknownPhone, "Spam msg $i")
    }

    // Still not blocked at 5 messages
    assertFalse(repository.myBlockedPhones.value.contains(unknownPhone))

    // 6th message triggers spam auto-block!
    repository.receiveIncomingMessage(unknownPhone, "Spam msg 6")
    assertTrue(repository.myBlockedPhones.value.contains(unknownPhone))
    assertTrue(repository.spamBlockedPhones.value.contains(unknownPhone))
  }
}

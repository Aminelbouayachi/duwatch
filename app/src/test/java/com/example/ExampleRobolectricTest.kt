package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.WatchRepository
import com.example.model.VibrationPattern
import com.example.model.WatchAlarm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
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
        assertEquals("DuWatch", appName)
    }

    @Test
    fun `watch repository manages dials and alarms`() {
        val repo = WatchRepository()
        assertNotNull(repo.device.value)
        assertTrue(repo.watchFaces.value.isNotEmpty())

        val initialCount = repo.alarms.value.size
        val newAlarm = WatchAlarm(
            id = "test_alarm",
            hour = 7,
            minute = 30,
            label = "Morning Run",
            isEnabled = true,
            repeatDays = setOf(1, 2, 3),
            smartWakeup = true,
            vibrationPattern = VibrationPattern.PULSE
        )
        repo.addAlarm(newAlarm)
        assertEquals(initialCount + 1, repo.alarms.value.size)

        repo.toggleAlarm("test_alarm")
        assertEquals(false, repo.alarms.value.find { it.id == "test_alarm" }?.isEnabled)
    }
}

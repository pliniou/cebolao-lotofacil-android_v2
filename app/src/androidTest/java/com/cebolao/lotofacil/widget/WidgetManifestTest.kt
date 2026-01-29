package com.cebolao.lotofacil.widget

import android.content.ComponentName
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetManifestTest {
    @Test
    fun widget_providers_are_declared_in_manifest() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val pm = context.packageManager
        val classes = listOf(
            LastDrawWidgetProvider::class.java,
            NextContestWidgetProvider::class.java,
            PinnedGameWidgetProvider::class.java
        )

        for (cls in classes) {
            try {
                val cn = ComponentName(context, cls.name)
                val info = pm.getReceiverInfo(cn, 0)
                assertNotNull(info)
            } catch (e: Exception) {
                throw AssertionError("Widget provider ${cls.name} not found in manifest: ${e.message}")
            }
        }
    }
}

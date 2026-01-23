package com.cebolao.lotofacil.widget

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetUtilsInstrumentedTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Test
    fun getLayoutIdFor_returns_nonzero_for_all_variants() {
        val small = WidgetUtils.getLayoutIdFor(LastDrawWidgetProvider::class.java, WidgetSizeVariant.SMALL)
        val medium = WidgetUtils.getLayoutIdFor(LastDrawWidgetProvider::class.java, WidgetSizeVariant.MEDIUM)
        val large = WidgetUtils.getLayoutIdFor(LastDrawWidgetProvider::class.java, WidgetSizeVariant.LARGE)

        assertNotEquals(0, small)
        assertNotEquals(0, medium)
        assertNotEquals(0, large)
    }

    @Test
    fun ballLayoutIds_are_valid() {
        val small = WidgetUtils.getBallLayoutIdFor(WidgetSizeVariant.SMALL)
        val medium = WidgetUtils.getBallLayoutIdFor(WidgetSizeVariant.MEDIUM)
        val large = WidgetUtils.getBallLayoutIdFor(WidgetSizeVariant.LARGE)

        assertNotEquals(0, small)
        assertNotEquals(0, medium)
        assertNotEquals(0, large)
    }

    @Test
    fun pending_intents_are_constructed() {
        val pi = WidgetUtils.getRefreshPendingIntent(context, LastDrawWidgetProvider::class.java, 42)
        val open = WidgetUtils.getOpenAppPendingIntent(context)

        assertNotNull(pi)
        assertNotNull(open)
    }
}

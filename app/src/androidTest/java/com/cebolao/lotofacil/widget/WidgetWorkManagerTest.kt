package com.cebolao.lotofacil.widget

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.Configuration
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WidgetWorkManagerTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()

    @Before
    fun setup() {
        val config = Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()

        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun enqueueOneTimeWidgetUpdate_enqueues_work() {
        WidgetUtils.enqueueOneTimeWidgetUpdate(context)

        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WidgetUtils.UNIQUE_WIDGET_UPDATE_ON_DEMAND)
            .get()

        assertTrue(workInfos.isNotEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }

    @Test
    fun schedulePeriodicWidgetUpdate_enqueues_periodic_work() {
        WidgetUtils.schedulePeriodicWidgetUpdate(context)

        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WidgetUtils.UNIQUE_WIDGET_UPDATE_PERIODIC)
            .get()

        assertTrue(workInfos.isNotEmpty())
        assertEquals(WorkInfo.State.ENQUEUED, workInfos.first().state)
    }
}

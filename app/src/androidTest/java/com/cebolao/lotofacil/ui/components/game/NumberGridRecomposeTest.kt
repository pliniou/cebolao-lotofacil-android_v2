package com.cebolao.lotofacil.ui.components.game

import androidx.activity.ComponentActivity
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumberGridRecomposeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selecting_a_number_only_recomposes_that_item() {
        val recomposeCounts = mutableMapOf<Int, Int>()
        val selectedState = mutableStateOf(setOf<Int>())

        composeTestRule.setContent {
            NumberGrid(
                selectedNumbers = selectedState.value,
                onNumberClick = { selectedState.value = selectedState.value + it },
                onItemRecompose = { number, count -> recomposeCounts[number] = count }
            )
        }

        // Initial composition populates counts; clear to observe subsequent recompositions only
        composeTestRule.runOnIdle { recomposeCounts.clear() }

        // Click number 1 to select it
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.runOnIdle {}

        // Number 1 should have been recomposed at least once; other numbers should not be present
        val countForOne = recomposeCounts[1]
        assertTrue("Expected number 1 to be recomposed at least once, was $countForOne", countForOne != null && countForOne > 0)

        // Check a different number (e.g., 2) was not recomposed
        assertNull("Expected number 2 not to be recomposed", recomposeCounts[2])
    }
}

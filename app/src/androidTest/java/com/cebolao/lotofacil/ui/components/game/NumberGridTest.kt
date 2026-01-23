package com.cebolao.lotofacil.ui.components.game

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NumberGridTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clicking_selected_number_calls_onClick_even_when_maxReached() {
        var clicked: Int? = null
        composeTestRule.setContent {
            NumberGrid(selectedNumbers = setOf(1), onNumberClick = { clicked = it }, maxSelection = 1)
        }

        // Clicking the selected number (1) should succeed
        composeTestRule.onNodeWithText("1").performClick()
        assert(clicked == 1)
    }

    @Test
    fun clicking_unselected_number_does_not_call_onClick_when_maxReached() {
        var clicked: Int? = null
        composeTestRule.setContent {
            NumberGrid(selectedNumbers = setOf(1), onNumberClick = { clicked = it }, maxSelection = 1)
        }

        // Clicking unselected number (2) should either be a no-op or fail to click; ensure not called
        try {
            composeTestRule.onNodeWithText("2").performClick()
        } catch (ignored: AssertionError) {
            // Expected if node is not clickable
        }

        assert(clicked == null)
    }
}

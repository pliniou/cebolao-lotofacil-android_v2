package com.cebolao.lotofacil.ui.components.layout

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class AppCardTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun appCard_displays_title_and_is_clickable_when_onClick_provided() {
        val title = "Test Title"
        var clicked = false
        composeTestRule.setContent {
            AppCard(title = title, onClick = { clicked = true }) {
                // content
            }
        }

        composeTestRule.onNodeWithText(title).assertIsDisplayed()
        composeTestRule.onNodeWithText(title).performClick()
        // Small assertion via side-effect; ensure click happened
        assert(clicked)
    }
}

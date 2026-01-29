package com.cebolao.lotofacil.ui.components.game

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import org.junit.Rule
import org.junit.Test

class NumberBallTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun numberBall_shows_selected_status_in_contentDescription() {
        val number = 5
        composeTestRule.setContent {
            NumberBall(number = number, isSelected = true)
        }

        val expected = "$number"
        composeTestRule.onNodeWithContentDescription(expected).assertExists()
    }

    @Test
    fun numberBall_shows_hit_and_miss_statuses_in_contentDescription() {
        val numberHit = 15
        composeTestRule.setContent {
            NumberBall(number = numberHit, variant = NumberBallVariant.Hit)
        }

        val expectedHit = "$numberHit"
        composeTestRule.onNodeWithContentDescription(expectedHit).assertExists()

        val numberMiss = 25
        composeTestRule.setContent {
            NumberBall(number = numberMiss, variant = NumberBallVariant.Miss)
        }

        val expectedMiss = "$numberMiss"
        composeTestRule.onNodeWithContentDescription(expectedMiss).assertExists()
    }
}

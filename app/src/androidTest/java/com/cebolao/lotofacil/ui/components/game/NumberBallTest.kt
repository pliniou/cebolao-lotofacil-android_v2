package com.cebolao.lotofacil.ui.components.game

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.platform.app.InstrumentationRegistry
import com.cebolao.lotofacil.R
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

        val status = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.general_selected)
        val expected = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.number_ball_content_description, number, status)

        composeTestRule.onNodeWithContentDescription(expected).assertExists()
    }

    @Test
    fun numberBall_shows_hit_and_miss_statuses_in_contentDescription() {
        val numberHit = 15
        composeTestRule.setContent {
            NumberBall(number = numberHit, variant = NumberBallVariant.Hit)
        }

        val statusHit = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.number_ball_status_hit)
        val expectedHit = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.number_ball_content_description, numberHit, statusHit)

        composeTestRule.onNodeWithContentDescription(expectedHit).assertExists()

        val numberMiss = 25
        composeTestRule.setContent {
            NumberBall(number = numberMiss, variant = NumberBallVariant.Miss)
        }

        val statusMiss = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.number_ball_status_miss)
        val expectedMiss = InstrumentationRegistry.getInstrumentation()
            .targetContext.getString(R.string.number_ball_content_description, numberMiss, statusMiss)

        composeTestRule.onNodeWithContentDescription(expectedMiss).assertExists()
    }
}

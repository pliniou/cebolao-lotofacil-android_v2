package com.cebolao.lotofacil.ui.components.game

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.cebolao.lotofacil.domain.model.UiLotofacilGame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GameCardRecomposeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun selecting_a_number_in_gamecard_only_recomposes_that_item() {
        val recomposeCounts = mutableMapOf<Int, Int>()
        val game = UiLotofacilGame(numbers = setOf(1,2,3), isPinned = false, creationTimestamp = 0L, mask = 1L)

        composeTestRule.setContent {
            GameCard(
                game = game,
                index = 1,
                onAction = {},
                onNumberItemRecompose = { number, count -> recomposeCounts[number] = count }
            )
        }

        // clear initial composition counts
        composeTestRule.runOnIdle { recomposeCounts.clear() }

        // Click number 1 inside the card
        composeTestRule.onNodeWithText("1").performClick()
        composeTestRule.runOnIdle {}

        val countForOne = recomposeCounts[1]
        assertTrue("Expected number 1 to be recomposed at least once, was $countForOne", countForOne != null && countForOne > 0)
        assertNull("Expected number 2 not to be recomposed", recomposeCounts[2])
    }
}

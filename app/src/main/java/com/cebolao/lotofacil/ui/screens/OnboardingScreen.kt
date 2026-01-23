package com.cebolao.lotofacil.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cebolao.lotofacil.R
import com.cebolao.lotofacil.ui.components.common.PagerIndicator
import com.cebolao.lotofacil.ui.components.common.PrimaryActionButton
import com.cebolao.lotofacil.ui.theme.Dimen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private data class Page(val img: Int, val title: Int, val desc: Int)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    val pages = rememberOnboardingPages()
    val pagerState = rememberPagerState { pages.size }
    val scope = rememberCoroutineScope()
    val isLast = pagerState.currentPage == pages.lastIndex

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
        ) {
            OnboardingPager(pages = pages, pagerState = pagerState)
            OnboardingBottomControls(
                isLast = isLast,
                pages = pages,
                pagerState = pagerState,
                scope = scope,
                onComplete = onComplete
            )
        }
    }
}

@Composable
private fun rememberOnboardingPages(): List<Page> {
    return remember {
        listOf(
            Page(
                R.drawable.img_onboarding_step_1,
                R.string.onboarding_title_1,
                R.string.onboarding_desc_1
            ),
            Page(
                R.drawable.img_onboarding_step_2,
                R.string.onboarding_title_2,
                R.string.onboarding_desc_2
            ),
            Page(
                R.drawable.img_onboarding_step_3,
                R.string.onboarding_title_3,
                R.string.onboarding_desc_3
            ),
            Page(
                R.drawable.img_onboarding_step_4,
                R.string.onboarding_title_4,
                R.string.onboarding_desc_4
            )
        )
    }
}

@Composable
private fun ColumnScope.OnboardingPager(
    pages: List<Page>,
    pagerState: PagerState
) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.weight(1f)
    ) { idx ->
        OnboardingContent(pages[idx])
    }
}

@Composable
private fun OnboardingBottomControls(
    isLast: Boolean,
    pages: List<Page>,
    pagerState: PagerState,
    scope: CoroutineScope,
    onComplete: () -> Unit
) {
    Row(
        Modifier
            .navigationBarsPadding()
            .padding(Dimen.Spacing24)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        OnboardingSkipButton(isLast = isLast, onComplete = onComplete)
        PagerIndicator(
            pages.size,
            pagerState.currentPage,
            Modifier.weight(1f)
        )
        OnboardingNextButton(
            isLast = isLast,
            pagerState = pagerState,
            scope = scope,
            onComplete = onComplete
        )
    }
}

@Composable
private fun RowScope.OnboardingSkipButton(
    isLast: Boolean,
    onComplete: () -> Unit
) {
    Row(Modifier.weight(1f)) {
        androidx.compose.animation.AnimatedVisibility(
            visible = !isLast,
            enter = androidx.compose.animation.fadeIn(),
            exit = androidx.compose.animation.fadeOut()
        ) {
            TextButton(onClick = onComplete) {
                Text(stringResource(R.string.onboarding_skip))
            }
        }
    }
}

@Composable
private fun RowScope.OnboardingNextButton(
    isLast: Boolean,
    pagerState: PagerState,
    scope: CoroutineScope,
    onComplete: () -> Unit
) {
    Box(
        Modifier
            .weight(1f),
        contentAlignment = Alignment.CenterEnd
    ) {
        PrimaryActionButton(
            text = stringResource(
                if (isLast) R.string.onboarding_start else R.string.onboarding_next
            ),
            onClick = {
                if (isLast) onComplete() else scope.launch {
                    pagerState.animateScrollToPage(pagerState.currentPage + 1)
                }
            },
            modifier = Modifier.widthIn(min = Dimen.ControlWidthMedium),
            isFullWidth = false
        )
    }
}

@Composable
private fun OnboardingContent(page: Page) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(horizontal = Dimen.Spacing24),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(page.img),
            contentDescription = stringResource(page.title),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .padding(bottom = Dimen.Spacing16),
            contentScale = ContentScale.Fit
        )
        Text(
            stringResource(page.title),
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(Dimen.Spacing8))
        Text(
            stringResource(page.desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

# Design System Specification

## 1. Tokens Inventory

### Colors (`ui/theme/Color.kt`)
- **Neutral (Slate System):** `Slate50` to `Slate950`.
- **Accents:** `VividPurple`, `VividPink`, `VividAmber`, `VividGreen`, `VividBlue`.
- **Brand:** `BrandAzulCaixa`, `BrandLaranjaCaixa` + aliases.
- **Semantic:** `SuccessColor`, `ErrorColor`.
- **Backgrounds:** `DarkBackground`, `LightBackground`, `LightSurface`, `DarkSurface`.
- **Glassmorphism:** `GlassSurfaceLight`, `GlassSurfaceDark`.

### Spacing & Dimensions (`ui/theme/Dimen.kt`)
- **System:** 8pt grid (`Spacing8`, `Spacing16`, `Spacing24`, `Spacing32`).
- **Tiny:** `SpacingTiny` (2dp), `Spacing4` (4dp).
- **Layout:** `ScreenPadding` (16dp), `CardContentPadding` (20dp).
- **Components:** `ActionButtonHeight` (48dp), `ActionButtonHeightLarge` (56dp).
- **Shapes:** `CardCornerRadius` (24dp), `ButtonCornerRadius` (16dp).
- **Icons:** `IconSmall` (16dp) to `IconExtraLarge` (48dp).

### Typography (`ui/theme/Typography.kt`)
- **Display:** Large/Medium/Small (Bold).
- **Headline:** Large/Medium/Small (Bold/SemiBold).
- **Title:** Large/Medium/Small (SemiBold/Medium).
- **Body:** Large/Medium/Small (Normal).
- **Label:** Large/Medium/Small (Numeric font, Medium/SemiBold).

### Motion (`ui/theme/Motion.kt`)
- **AnimateOnEntry:** Standard entry animations (Fade, Slide, Scale).
- **Press:** Scale reduction on press (`0.95f`).

## 2. Reusable Components

### Layout Components
- **`AppScreen`**: Scaffold wrapper with `StandardScreenHeader`.
- **`StandardPageLayout`**: Scrollable container with consistent max-width (840dp) and padding.
- **`AppCard`**: Base card component. Support for `Solid`, `Glass`, `Outlined` variants. Handles nesting.
- **`StandardScreenHeader`**: Standard TopAppBar.

### Common Components
- **`PrimaryActionButton`**: Main CTA. Supports loading state and icons.
- **`StudioHero`**: Hero banner for About/Settings.
- **`LoadingCard`**: Skeleton/Loading state visual.
- **`InfoDialog`, `CommonDialogs`**: Standard dialogs.
- **`CustomChip`**: Tags/Chips.

## 3. Standardization Rules

### Component Signatures
1. **Modifier:** Must be the first optional parameter: `modifier: Modifier = Modifier`.
2. **Callbacks:** Must be the last parameters.
3. **State:** Pass specific state objects or data classes, not ViewModels.

### Composition Structure
```kotlin
@Composable
fun MyScreen(
    viewModel: MyViewModel = hiltViewModel(),
    navController: NavController
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // ... Effects ...
    MyScreenContent(
        state = uiState,
        onEvent = viewModel::onEvent
    )
}

@Composable
fun MyScreenContent(
    state: MyUiState,
    onEvent: (MyUiEvent) -> Unit
) {
    AppScreen(title = "Title") { padding ->
        StandardPageLayout(scaffoldPadding = padding) {
             // Items
        }
    }
}
```

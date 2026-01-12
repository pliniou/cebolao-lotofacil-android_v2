# Migration Map

## 1. Identified Issues

### Inconsistent ViewModel Patterns
- **Current:** Methods exposed individually (e.g., `onTimeWindowSelected`, `forceSync`).
- **Required:** Single `onEvent(event: UiEvent)` entry point for user actions.
- **Target:** Refactor ViewModels to unidirectional data flow.

### Screen Structure
- `HomeScreen` and `AboutScreen` mix layout logic with state collection in the root Composable.
- **Target:** Separate `Screen` (DI/VM) from `Content` (Pure UI).

### Legacy/Hardcoded Styles
- Need to verify all screens for hardcoded colors or dimensions not using `Dimen.*` or `MaterialTheme.colorScheme.*`.
- `AboutScreen` uses `HorizontalDivider` directly with some manual color tweaks. Check if `AppDivider` should be used.

### Navigation
- Navigation logic mixed in screens.
- **Target:** Lift navigation events to ViewModel side effects or explicit callbacks from the Screen composable.

## 2. Migration Strategy (From -> To)

### Components
| Legacy/Raw Usage | Standard Component |
| ---------------- | ------------------ |
| `Scaffold` + `TopAppBar` | `AppScreen` (wraps both) |
| `Column(Modifier.verticalScroll)` | `StandardPageLayout` (handles widths & padding) |
| `Card` / `Surface` | `AppCard` (handles nesting & theming) |
| `Button` | `PrimaryActionButton` (or `textButton` if secondary) |
| `Divider` | `AppDivider` |

### Architecture
| Pattern | Standard |
| ------- | -------- |
| `fun onClickAction()` | `fun onEvent(Event.Action)` |
| `collectAsState()` | `collectAsStateWithLifecycle()` |
| `LaunchedEffect` for Navigation | `ObserveAsEvents` (or standard SideEffect pattern) |

## 3. Execution Order
1. **Infrastructure:** Ensure `AppScreen` and `StandardPageLayout` are robust.
2. **Screen 1: `AboutScreen`** (Low risk, good for testing structure separation).
3. **Screen 2: `HomeScreen`** (High impact, requires ViewModel refactor).
4. **Remaining Screens:** `GeneratedGames`, `Checker`, `Filters`.

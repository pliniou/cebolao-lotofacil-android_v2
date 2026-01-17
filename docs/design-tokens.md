# Design Tokens (Cebolão Lotofácil)

Este documento descreve as “fontes da verdade” do Design System do app e como aplicar os tokens de forma consistente (Material Design 3 + Compose).

## Princípios

- Prefira tokens semânticos (`MaterialTheme.colorScheme`, `MaterialTheme.typography`, `Dimen`, `Shapes`, `Motion`) a valores “mágicos”.
- Evite cores hardcoded (`Color(0x...)`) em telas/componentes; use `MaterialTheme.colorScheme` ou tokens do tema.
- Para espaçamentos/tamanhos em Compose, use `Dimen` sempre que existir um token equivalente.
- Para widgets (RemoteViews/XML), use `@color/widget_*` e `@dimen/widget_*` (e seus overrides `values-night`).

## Cores

- **Compose (fonte principal)**
  - Paleta base: `app/src/main/java/com/cebolao/lotofacil/ui/theme/Color.kt`
  - Paletas por accent: `app/src/main/java/com/cebolao/lotofacil/ui/theme/AccentColors.kt` (`AccentPalette`, `lightColorSchemeFor`, `darkColorSchemeFor`)
  - Uso recomendado em UI: `MaterialTheme.colorScheme.*` (ex.: `primary`, `surfaceContainer`, `error`, `onSurfaceVariant`)

- **XML (Splash/Widgets)**
  - `app/src/main/res/values/colors.xml`
  - `app/src/main/res/values-night/colors.xml`
  - Tokens de widget:
    - `@color/widget_background`, `@color/widget_text_primary`, `@color/widget_text_secondary`, `@color/widget_divider`, `@color/widget_icon_tint`

## Tipografia

- `MaterialTheme.typography.*` (Material 3).
- Fontes/config: `app/src/main/java/com/cebolao/lotofacil/ui/theme/Typography.kt` e `app/src/main/java/com/cebolao/lotofacil/ui/theme/Font.kt`

## Espaçamento e dimensões (Compose)

- Tokens: `app/src/main/java/com/cebolao/lotofacil/ui/theme/Dimen.kt`
  - Espaçamentos: `Dimen.Spacing*`, `Dimen.SectionSpacing`, `Dimen.ItemSpacing`
  - Bordas: `Dimen.Border.*`
  - Largura máxima: `Dimen.LayoutMaxWidth`
  - Componentes: `Dimen.ActionButtonHeight*`, `Dimen.Icon*`, `Dimen.LoadingIndicatorSize`
  - Números/bolas: `Dimen.BallSize*`, `Dimen.BallTouchSize*`, `Dimen.BallText*`

## Shapes

- `MaterialTheme.shapes.*` e `app/src/main/java/com/cebolao/lotofacil/ui/theme/Shape.kt`

## Motion

- `app/src/main/java/com/cebolao/lotofacil/ui/theme/Motion.kt`

## Temas (XML)

- Base/Splash:
  - `app/src/main/res/values/themes.xml`
  - `app/src/main/res/values-night/themes.xml`
  - `app/src/main/res/values-v27/themes.xml`
  - `app/src/main/res/values-night-v27/themes.xml`

Observação: o tema XML cobre principalmente window background e estilos de splash/system bars; o Design System de UI é aplicado via `CebolaoLotofacilTheme` em Compose.

## Widgets (RemoteViews)

- Estilos: `app/src/main/res/values/widget_styles.xml`
- Dimens: `app/src/main/res/values/dimens.xml` (`widget_*`)
- Drawables: `app/src/main/res/drawable-nodpi/widget_*.xml`
- Layouts: `app/src/main/res/layout/widget_*.xml`


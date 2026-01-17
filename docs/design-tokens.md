# Design Tokens: Compose ↔ XML

Use estes equivalentes para manter spacing/cores consistentes entre Compose (`Dimen.kt`) e XML (`res/values/dimens.xml`).

| Conceito            | Compose (Dimen)        | XML (dimens)                  | Uso típico                                       |
|---------------------|------------------------|-------------------------------|--------------------------------------------------|
| Espaço xsmall       | `Spacing4`             | `spacing_xsmall` (4dp)        | Títulos/subtítulos em widgets                    |
| Espaço small        | `Spacing8`             | `spacing_small` (8dp)         | Margens internas curtas                          |
| Espaço small+       | `Spacing12`            | `spacing_small_plus` (6dp)    | Padding ícone refresh, topo de seções compactas  |
| Espaço medium       | `Spacing16`            | `spacing_medium` (16dp)       | Padding geral, gap de cards                      |
| Espaço medium comp. | `SectionSpacing` (16)  | `spacing_medium_compact` (12) | Separação de blocos em widgets/headers           |
| Espaço large        | `Spacing24`            | `spacing_large` (24dp)        | Gaps maiores entre seções                        |
| Padding de tela     | `ScreenPadding` (12)   | `spacing_medium` (16dp)       | Bordas horizontais em listas/páginas             |
| Conteúdo de card    | `CardContentPadding`   | `widget_content_padding*`     | Cards Compose e blocos de widgets                |
| Divisores           | `outlineVariant`       | `widget_divider`              | Linhas de separação e bordas finas               |
| Ícones em widgets   | `IconSmall/Medium/Lg`  | `icon_size_*`                 | Ícones de header/refresh em widgets              |

Regras rápidas:
- Evite dp literais em XML; reutilize os tokens acima.
- Para widgets, use `widget_background` + `widget_divider` (já alinhados à paleta) e padding do conjunto `widget_content_padding*`.
- Em Compose, prefira `StandardPageLayout` e `AppCard` para herdar spacing/shape/cores automáticos.

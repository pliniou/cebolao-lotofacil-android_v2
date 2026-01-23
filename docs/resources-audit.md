# Resource Audit — Candidates flagged by lint (UnusedResources)

Date: 2026-01-23

Summary:
- Lint reported a broad set of resources as potentially unused, spanning fonts, drawables, layouts (widgets), and many strings.
- Many of the flagged items are related to widgets, the splash theme, and onboarding imagery — these often look unused to static analysis but may be referenced dynamically (widget XML, theme attributes, or in code by resource names).

Top candidates (from lint partial results):
- Fonts: outfit_* (thin..black), stacksansnotch_*, gabarito_*, etc.
- Drawables: `ic_splash_logo`, `img_onboarding_step_*`, `ic_widget_fixado`, `ic_widget_proximo`, `widget_*` backgrounds
- Layouts / widget xmls: `widget_last_draw.xml`, `widget_next_contest.xml`, `widget_pinned_game.xml`, plus size variants
- Styles: `Theme_App_Starting`, `Theme_SplashScreen`, `Widget_Cebolao_*` styles
- Strings & plurals: many shown as unused by lint (double-check these; some may be used only in PT or in remote strings)

Guidelines & verification steps:
1. Search by resource name across code & XML:
   - grep -R "@string/widget_last_draw_title" or "widget_last_draw" from project root
2. Search for dynamic usage by name (e.g., inflating layouts by name, remote config or reflection):
   - grep -R "widget_last_draw" -- also search for strings like "last_draw_widget_info" in `res/xml`
3. Confirm widget usage:
   - Ensure `AndroidManifest.xml` includes the widget receivers (already added earlier). If so, widget xmls and widget drawables should be kept.
4. Onboarding & images:
   - Check if onboarding steps are referenced in navigation code and resource arrays; if not used, consider removing images.
5. Fonts:
   - If fonts are only packaged but not referenced from `fontFamily` or styles, remove or compress them.

Recommended actions (per resource category):
- Widgets & Theme (High caution): keep if manifest or xml references exist, add a brief test asserting widget provider exists and remote views are generated.
- Fonts (Review): find usages; if truly unused, remove and update `fonts/` and any build configs.
- Onboarding images (Low risk): verify `Onboarding` screens are referenced; remove retired images.
- Strings: verify by grep; if true unused, remove to reduce translation burden.

Automation & helpers:
- Commands to find usages quickly:
  - grep -R "\bresource_name\b" app/
  - ./gradlew :app:lintDebug  (re-run to confirm changes)

PR checklist when removing a resource:
- [ ] Confirm no references in code, xml, res, or tests
- [ ] Run `./gradlew :app:lintDebug` and `./gradlew :app:assembleDebug`
- [ ] Run automated tests (unit + androidTest)
- [ ] Add an entry in `docs/resources-audit.md` documenting the decision and rationale

Notes:
- I can begin by producing a smaller, verified remove PR (e.g., remove unused onboarding images) if you want; otherwise I can produce a full audit PR listing each candidate with "keep/review/remove" suggestions.

Next steps I recommend:
1. I will produce a curated list of top 10 highest-confidence removals (if you want me to remove them automatically, confirm and I will open a PR). 
2. Add widget test(s) to protect widget resources (if not already present).

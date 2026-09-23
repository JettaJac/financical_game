# AGENTS.md

## Project
Offline Android companion. Source of truth: docs/SPEC.md.
Human does not write Kotlin. Implement exactly the current SPEC.
Do not invent extra features. Do not keep leftover UI from older SPEC versions.

## Stack
- Kotlin + Jetpack Compose + Material 3. No XML layouts.
- Vector drawables for icons are allowed.
- No network, Firebase, Retrofit, accounts.
- DataStore Preferences for durable state.
- Thin MVI: presentation / domain / data.
- Hilt + KSP. Coroutines + Flow.
- Three pages via HorizontalPager. No NavHost, no BottomBar.
- Home screen visual assets come from `res/svg`, `res/webp`, and the supplied reference.

## Commands
- Build: ./gradlew :app:assembleDebug
- Unit tests: ./gradlew test
- Run: android run
- Docs: android docs "<query>"
  Prefer Android CLI over raw sdkmanager/avdmanager.

## UI rules
- Layout and copy come only from docs/SPEC.md.
- Home screen follows `res/references/main_screen.png`, including visible money and goal target.
- Home overlays: Menu and Shop. Only one overlay at a time.
- Menu: «Продолжить» dismisses, «Выйти» leaves the app.
- Shop: close with X. One item «ошейник» price 100, black-square image, «Купить».
- Buy is disabled/no-op if money < price. Successful buy subtracts price from money.
- Home layout follows `res/references/main_screen.png`: header, room background, pet, five tabs, product cards.
- The five home tabs are clickable and select the lower drawer section.
- Food shows the supplied product cards. Happiness, energy, shop, and tasks show placeholders.
- The savings quick action above the pet opens the shop overlay.
- collectAsStateWithLifecycle(). No business logic in Composables.
- ViewModel state only via _state.update { it.copy(...) }.
- User-facing strings in res/values/strings.xml.

## State
First-launch seeds are in SPEC.md.
Persist across process death: money, health, happiness, energy, goalTarget, goalTitle, income, expense, level, currentPeriod.
20-minute timer may reset to 20:00 after process death.

## Timer economy
Every time the countdown reaches 00:00:
1. Apply money += (income - expense).
2. Immediately restart the 20:00 cycle.
   Do not add other timer side effects.

## Forbidden
- XML layouts
- GlobalScope, collectAsState(), !!
- Features not in SPEC.md
- Rewriting Gradle from scratch
- Extra shop items, auth, analytics
- Reintroducing the v1 money/clock/linear-bars layout

## Definition of Done
Not done until:
1. ./gradlew :app:assembleDebug passes
2. android run shows screen 1 per SPEC
3. swipe left/right reaches empty pages 2/3 and back
4. timer wraps 20:00 → 00:00 → 20:00
5. timer wrap updates money by (income - expense)
6. buying the collar subtracts 100 when money is enough
7. restart keeps persisted fields
8. short report: files changed + how to verify
9. home screen visually matches `res/references/main_screen.png` on a phone viewport

# AGENTS.md

## Project

Offline Android financial companion game. The implementation and current user requests are the source of truth; there is no separate `docs/SPEC.md`.

Keep changes consistent with the existing product and remove obsolete behavior when a feature is replaced. Do not revive prototype UI or flows that are no longer present.

## Stack and architecture

- Kotlin, Jetpack Compose, Material 3; no XML layouts.
- Single-activity application with Compose-managed screens and overlays; no `NavHost`.
- Thin MVI-style flow: Compose UI → `PetAction` → `PetViewModel` → `GameRepository`/`GameStore` → `GameSnapshot`.
- DataStore Preferences stores game state. Room stores goal history.
- Hilt, KSP, Coroutines, and Flow.
- Fully offline: no network services, accounts, Firebase, analytics, or Retrofit.
- Use `collectAsStateWithLifecycle()` at the activity boundary.
- Keep business rules out of Composables.
- Update ViewModel state through `_state.update { it.copy(...) }`.
- Put user-facing copy in `res/values/strings.xml`.

## Commands

- Build: `./gradlew :app:assembleDebug`
- Unit tests: `./gradlew testDebugUnitTest`
- Install: `./gradlew installDebug`
- Run and inspect devices with the Android CLI (`android run`, `android layout`, `android screen`).

## Current product flows

### Onboarding

- Greeting, character naming, story, goal selection, fur colour, and fur style.
- Eye-colour customization is not part of the UI.
- The naming step uses the animation derived from `assets/webm/talking_default.webm`, with the character positioned on the rug.
- Existing eye-colour data remains internal only for save compatibility.

### Home

- Responsive header with current day/week, active goal, money, goal target, and progress.
- Room background and pet occupy the main area.
- Five lower tabs: food, happiness, energy, shop items, and tasks/jobs.
- Food, happiness, and energy cards apply effects and use cycle-based cooldowns.
- Shop items are permanent purchases where applicable.
- Every task/job can be performed at most once per game cycle. Day, week, total-use, prerequisite, and event restrictions may additionally apply.
- Changes to money and pet characteristics pulse the corresponding indicator icon.

### Quick actions and overlays

- The cycle quick action advances the cycle only while test mode is enabled.
- Test mode is off on every app start and can be enabled from the menu with a password.
- The piggy-bank quick action opens the full-screen weekly budget overview, not a shop overlay.
- Budget overview defaults to actual weekly income/expenses and can switch to the saved plan.
- Other overlays/screens include menu, active goal, goal selection, character settings, and events.
- The parent area contains a persistent action history grouped by cycle and allows a parent to add a custom goal with a title, target amount, and automatically selected illustration to the child's goal list.
- Event action buttons use adaptive single-line text sizing.
- Only one home overlay is active at a time.
- There is no collar item or collar-purchase flow.

### Budget lifecycle

- Weekly planning classifies entries and records planned optional spending.
- Income subcategories remain: recurring, irregular, savings.
- Expense subcategories shown during classification are: recurring, optional, unplanned.
- Actual optional expenses and additional income accumulate during the week.
- Weekly review compares planned and actual values before the next plan begins.

### Character settings

- Name, fur colour, and fur style are editable.
- Eye-colour controls must not be reintroduced.

## Cycle and cooldown rules

- The single source of truth is `GameDefaults.CYCLE_DURATION_SECONDS`.
- One cycle lasts 13 minutes. Milliseconds must derive from that value through `CYCLE_DURATION_MILLIS`.
- Do not hardcode cycle duration elsewhere in production code.
- Cooldowns are expressed as fractions/counts of cycles via `Cooldown.Cycles`, then converted using the shared duration.
- Active cycle deadlines persist across process death.
- Duration migrations must preserve progress for existing active cycles. The legacy 20-minute constant exists only to migrate older saves.
- The UI timer refresh interval (`delay(1_000)`) is not the cycle duration.

## Persistence

Persist the complete game snapshot needed to restore gameplay, including:

- pet identity and appearance;
- money and health/happiness/energy values;
- current goal, level, purchases, and goal history;
- current period and cycle deadline;
- cooldowns and task usage;
- weekly budget plan and actual totals;
- event progress, flags, unlocked jobs, and usage limits.

Reset must return these systems to coherent defaults without leaving stale cooldown, task, event, or job keys.

## UI and assets

- Build responsive layouts from Compose constraints; do not target one fixed phone size.
- Visual references live under `src/main/res/references` and guide appearance, but current implemented behavior takes precedence over stale reference details.
- Runtime raster assets live in `drawable-nodpi`; Android vector drawables live in `drawable`; source SVG files may live in `res/svg` but are not directly compiled as Android resources.
- Launcher icons use `mipmap-*` plus adaptive icon definitions in `mipmap-anydpi`.
- Preserve supplied artwork and transparency. Verify animation/image changes on an emulator when practical.

## Constraints

- Do not add XML layouts, `GlobalScope`, `collectAsState()`, or non-null assertions (`!!`).
- Do not rewrite Gradle configuration from scratch.
- Do not add network dependencies or unrelated features.
- Preserve user changes and unrelated worktree files.
- Keep compatibility with existing saves when changing persisted values or rules.

## Definition of done

For relevant changes:

1. `./gradlew testDebugUnitTest` passes.
2. `./gradlew :app:assembleDebug` passes when packaging/resources changed.
3. Changed flows are inspected on an emulator when practical.
4. No obsolete UI, action, string, or persistence path from the replaced behavior remains.
5. The handoff briefly states what changed and how it was verified.

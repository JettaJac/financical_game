# Financial Game — SPEC v1.1

This file is the only source of truth for product behavior and visible UI.

## Platform and architecture

- Offline Android app in Kotlin, Jetpack Compose, and Material 3.
- Thin presentation/domain/data split, Hilt, coroutines, and Flow.
- DataStore Preferences stores the durable game snapshot.
- The app uses the bundled Nunito font and performs no runtime font download.

## Pages

- One `HorizontalPager` contains three pages.
- The home screen is the middle page.
- The neighboring pages are empty placeholders.
- No `NavHost` or bottom navigation bar.

## Home screen

- Visually follow `src/main/res/references/main_screen.png` and the bundled SVG/WebP assets.
- Header: current day and week, goal title, menu, current money, goal progress, goal target.
- Room background and pet occupy the center of the screen.
- The room background fades into the light surface at its top and bottom edges.
- The timer quick action is on the left. It displays whole minutes remaining.
- The savings quick action on the right opens the shop overlay.
- Use Nunito for all app text.

## Lower drawer

- Five tabs: food, happiness, energy, shop, and tasks.
- Every tab is clickable. The selected tab has the light background shown in the reference.
- The tab row overlaps the faded lower edge of the room background.
- Food shows the current cards for school lunch, soda, and ice cream.
- Happiness, energy, shop, and tasks show a section-specific placeholder for future content.
- Health, happiness, and energy progress indicators display the corresponding DataStore values.
- Selecting a tab is UI state only and does not change or persist game resources.

## Overlays

- Menu contains `Продолжить` and `Выйти`.
- `Продолжить` closes the menu. `Выйти` leaves the app.
- Shop opens above the home screen from the savings quick action.
- Shop has an X close button and one item: `ошейник`, price 100, black-square image, and `Купить`.
- Buying subtracts 100 from money only when the balance is sufficient.
- Only one overlay can be visible at a time.

## Persisted state and seeds

Persist money, health, happiness, energy, goal target, goal title, income, expense, level, and current period. On first launch initialize all missing required values from `GameDefaults`. After initialization, a missing required preference is a critical data error rather than an implicit reseed during reads.

First-launch seeds: money 100, health 70, happiness 70, energy 70, goal target 100, goal title `ПОДУШКА`, income 30, expense 10, level 1, current period 1.

The timer itself may restart from 20:00 after process death.

## Timer economy

- A cycle lasts 20 minutes.
- When it reaches 00:00, apply `money += income - expense`.
- Immediately restart at 20:00.
- Do not add other timer side effects.

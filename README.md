# Calculator App — CIS 2203N Mock Midterm
**Student ID:** 22100331

## Project Overview
A fully functional Android calculator built in Java, implementing all three milestones plus the bonus history log.

---

## Milestone 1 — UI Grid & Styling
**Commit:** `feat: implement UI grid and styling`

### Implementation Choices
- **ConstraintLayout** as the root container — provides flat, non-nested view hierarchy and flexible anchoring.
- **GridLayout** for the button pad — `columnCount=4`, `rowCount=6` with `layout_columnWeight` and `layout_rowWeight` for equal-sized cells. No nested LinearLayouts, keeping the hierarchy shallow.
- **Custom Color Theme:** Material Design 3 orange-accent (`#FF9500`) operator buttons with light/dark background switching. The `values/colors.xml` (light) and `values-night/colors.xml` (dark) provide two complete palettes.
- **Dark/Light Mode Toggle button** (☀/☾) in the top-right corner lets users switch modes at runtime using `AppCompatDelegate.setDefaultNightMode()`.
- Rounded corners (`cornerRadius=16dp`) and elevation shadows give the UI a modern feel beyond default gray buttons.

---

## Milestone 2 — Core Math Logic & Edge Cases
**Commit:** `feat: implement core math logic and zero-division handling`

### Implementation Choices
- All logic lives in `MainActivity.java` — no third-party math libraries.
- Calculator follows a **two-operand model**: `operandA`, an `operator` string, and `operandB`. Pressing `=` evaluates the expression.
- Division-by-zero is caught **before** any division is attempted:
  ```java
  if (operandB == 0) {
      displayValue = getString(R.string.error_divide_by_zero); // "Cannot divide by zero"
      ...
      return; // prevents crash
  }
  ```
- The `formatNumber()` helper trims trailing decimal zeros (e.g. `10.0 → "10"`) and caps precision at 10 significant digits.

---

## Milestone 3 — State Preservation & Custom Operator
**Commit:** `feat: add state preservation and custom operator`

### State Preservation
- `onSaveInstanceState(Bundle)` saves all mutable fields: `displayValue`, `operandA`, `operandB`, `operator`, `expressionText`, `isNewNumber`, `isDarkMode`, `historyVisible`, and the history list.
- `onCreate` reads the bundle and restores every field before calling `updateDisplay()`, so screen rotations and backgrounding are fully transparent to the user.
- History entries implement `Serializable` so the entire list can be bundled.

### Custom Operator (★ button)
- **Formula:** `value × 3.31`
- **Derivation:** Last 3 digits of Student ID **22100331** → `331` → `3.31`
- Implemented in `applyCustomOperator()` — reads the current display, multiplies by the constant `CUSTOM_MULTIPLIER = 3.31`, and records the entry in the history log.

---

## Bonus — Scrollable History Log
**Commit:** `feat: add scrolling history log`

- Implemented with **RecyclerView** (`LinearLayoutManager`, newest entries at top).
- `HistoryAdapter` / `HistoryEntry` handle the list.
- A "History ▼/▲" toggle button shows/hides the panel; its state is preserved across rotations.
- Each entry displays the full expression (e.g. `5 + 3 =`) above the result (`8`).

---

## Architecture Notes
| Component | Technology |
|---|---|
| Root layout | `ConstraintLayout` |
| Button grid | `GridLayout` (4 cols × 6 rows) |
| History list | `RecyclerView` + `HistoryAdapter` |
| Theme switching | `AppCompatDelegate` night mode |
| State persistence | `onSaveInstanceState` / `onCreate` bundle |

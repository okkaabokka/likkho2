# Likkho — Native (Kotlin + Jetpack Compose)

UI/UX + fonts of the Likkho web app, rebuilt natively, now with the working
canvas features.

## Working features
* **Rich canvas:** Bold, Italic, Underline, Strikethrough, Subscript, Superscript, Code (chip),
  Quotation, H1/H2/H3, bullet / numbered / to-do lists (Enter continues the list, tap ☐ to check), section
  divider — on a selection, or toggled "for typing" at the cursor.
  Spans follow your edits (typing, delete, paste, replacing a selection).
* **Undo / Redo** for text, heading, formatting, fonts and background.
* **Fonts:** default canvas font is **Gilda Display** (chip shows just *Default*); picker also has Cormorant, Short Stack, Jakarta Sans, IBM Plex Mono
  plus **Upload .ttf/.otf** — applies to selected text or while typing.
* **Canvas background image:** bottom bar above the toolbar — *Canvas Background* + *Choose Image* + close.
  Once an image is set the bar shows *Change Image · Edit · Remove · Done*.
  *Edit* opens the background edit pop-up (`ui/canvas/CanvasBackgroundEditDialog.kt`).
* **Floating images** on the canvas (drag / pinch / rotate, foreground or behind text) from the Media Picker button.
* **Bold** is drawn with a thin same-colour double-strike on single-weight fonts (Gilda Display, Short Stack, Plex Mono),
  fonts with a real bold face (Cormorant, Jakarta Sans) use it.
* Reading mode / Edit mode, reminder date & time picker, per-diary lock dialog,
  app passcode screen, pin / delete / search, dark mode, full screen.
* Hardware-keyboard shortcuts: Ctrl+B / I / U / Z / Y / Shift+Z / 1 / 2 / 3.

## Still demo-only (toast "to be wired later")
Persistence (diaries live in memory), encryption, backup / restore, export, share,
biometrics, link / colour / size / alignment, voice, Wikipedia, word analysis,
LaTeX, spoiler, date stamp, import (check the toolbar toasts for the current list).

## Fonts
Fonts live in `app/src/main/assets/fonts/`. Gilda Display (default canvas font) and
Short Stack are in `res/font/`. The theme falls back to system fonts if a file is missing.

## Code map
| File | What it is |
|------|-----------|
| `ui/screens/WorkspaceScreen.kt` | Editor, toolbar, font bar, background panel |
| `util/TextEditDiff.kt` | Finds what changed in the text and shifts all spans |
| `util/FormatSpanManager.kt` | Bold / italic / underline / strikethrough ranges |
| `util/HeadingSpanManager.kt` | H1–H3 ranges |
| `util/CustomFontManager.kt` | Font picker data, .ttf upload, font ranges |
| `util/ListAndFormatUtils.kt` | Bullet / numbered / to-do list helpers |
| `ui/canvas/` | Floating images + background edit dialog |
| `util/CanvasUndoRedoManager.kt` | Undo / redo history |
| `util/CanvasBackgroundManager.kt` | Copies the chosen background image into app storage |
| `AppViewModel.kt` | In-memory state — replace with real storage later |

# Re-TUI TODO

This is a working backlog for issues and user requests after the timer/pomodoro/widget-page branch was merged into `master`.

## Completed In v325

- Brightness permission flow
  - Added `WRITE_SETTINGS` to the manifest.
  - Updated the brightness command to deep-link into Re-TUI's own "Modify system settings" permission screen.

- Package name / side-by-side install
  - Confirmed active app id is `com.dvil.tui_renewed`.
  - Namespaced the custom `RECEIVE_CMD` permission to `${applicationId}` to avoid collision with the original upstream launcher.

- Themer navigation stacking
  - The settings/Themer terminal surface now forces an opaque internal background.
  - The outside overlay can still let wallpaper bleed through without showing prior Themer screens under the current one.

- Widget / terminal polish
  - Added separate theme keys for music and notification widget border/text colors.
  - Added auto-color support for those new keys.
  - Widget border labels now paint an opaque surface mask so border lines do not show through transparent themes.
  - Music song line now uses `Title:` instead of repeating `Now Playing:`.
  - Toolbar app drawer icon background was aligned with the other toolbar buttons.

- Branch/release
  - Merged `codex/timer-stopwatch` into `master`.
  - Published Firebase build `325`.
  - Created `codex/termux-integration` from the merged `master`.

## Completed On `codex/termux-integration`

- Removed the abandoned native Android widget/dashboard experiment.
  - Dropped the second ViewPager dashboard page.
  - Removed the `dashboard`/`widgets` command surface.
  - Kept page 0 focused on Re-TUI-owned music and notification terminals.

- Tightened the music terminal controls.
  - Stabilized the `PREV`, `PLAY/PAUSE`, and `NEXT` row with weighted button widths.
  - Reduced oversized title/singer text while keeping controls readable.

- Normalized terminal border behavior for current widget surfaces.
  - `enable_dashed_border=false` now removes the terminal-style widget border.
  - `enable_dashed_border=true` with gap `0` remains the solid-border path.

- Started the layered home terminal cutover.
  - Output/input/suggestions now live in a bottom overlay tray instead of pushing the home module layer.
  - Added an `[ OUTPUT ^ ]` tray handle for expanding and collapsing output history.
  - Back now collapses expanded output before normal launcher back behavior.

- Started Termux setup guidance.
  - Added `termux -setup` to the command surface and suggestions.
  - The Termux console now prints the required `allow-external-apps=true` setup, script folder convention, script alias example, and permission reminder.
  - Added first-pass wiki docs for Termux setup, script aliases, and safe run flow.

## High Priority

- Remove Android widget/dashboard experiment (done on `codex/termux-integration`; pending merge)
  - Drop the native Android `AppWidgetHost` surface and the current `widgets` command path.
  - Keep the idea of user-extensible panels, but rebuild it as Re-TUI-owned modules.
  - Do not support KWGT/native Android widgets for now; they break visual consistency and add provider/OEM instability.

- Fix music widget control sizing (done on `codex/termux-integration`; pending wider device testing)
  - Restore readable `PREV`, `PLAY/PAUSE`, and `NEXT` text while keeping the compact widget height.
  - Make the control row stable across narrow devices and large user font settings.
  - Keep the visualizer as the background layer and avoid button squish when text scale changes.

- Normalize border behavior (partially done; continue applying to settings/app drawer/Termux/module surfaces as new surfaces change)
  - `enable_dashed_border=false` should mean no terminal-style border on all Re-TUI terminal surfaces.
  - `enable_dashed_border=true` with gap `0` should mean a solid terminal border.
  - `enable_dashed_border=true` with gap greater than `0` should mean dashed border.
  - Apply this consistently to music, notifications, input/output, settings, app drawer, Termux console, and future modules.

## Phase 1 - Layered Home Terminal

- Split the home screen into two conceptual layers:
  - Layer 1: status, ASCII art, module dock, active module, wallpaper.
  - Layer 2: output, input, suggestions, toolbar.
- Convert output/input/suggestions into a terminal tray overlay rather than a layout section that pushes other content.
- Add output expand/collapse behavior:
  - Collapsed: compact output history plus input and suggestions.
  - Expanded: scrollable output history for reviewing logs.
  - Back button collapses expanded output before normal back behavior.
- Keep input focus stable when expanding/collapsing output.
- Ensure expanded output intentionally blocks module interaction, while collapsed output leaves Layer 1 usable.

## Phase 2 - Re-TUI Native Modules

- Replace the native widget/dashboard idea with Re-TUI-owned modules.
- Add a slim module dock below the ASCII art.
  - Example dock items: `MUSIC`, `NOTIF`, `TIMER`, `CAL`.
  - Tapping one module closes the previous module and opens the selected one.
  - Active module item is highlighted.
- Add close control to module windows.
- Start with built-in modules:
  - Music visualizer/player module.
  - Notification terminal module.
  - Timer/stopwatch/pomodoro module.
  - Calendar module, monthly view first.
- Keep module command surface command-first:
  - `module -ls`
  - `module -show music`
  - `module -hide music`
  - `module -close`
  - `module -dock add server`
  - `module -dock remove music`
- Module visibility rules:
  - Hiding/removing from dock does not delete the module from the registry.
  - Deleting/removing from registry is a separate explicit command.

## Phase 3 - Script Modules

- Status: first implementation done on `codex/termux-integration`; pending phone UAT.
- Add script-backed custom modules after built-in modules are stable.
- Start with a strict text-output contract:
  - Termux runs the script.
  - Re-TUI renders stdout inside a terminal module box.
  - No arbitrary Java/Kotlin/plugin code loading.
- Initial commands:
  - `module -add server termux:/data/data/com.termux/files/home/retui/server.sh`
  - `module -refresh server`
  - `module -show server`
  - `module -hide server`
  - `module -rm server`
- Dock commands are append/remove only:
  - `module -dock add server`
  - `module -dock remove music`
- Keep script files owned by Termux/user storage; Re-TUI should never delete the source script.
- Later enhancement: controlled Re-TUI markup primitives:
  - text color spans
  - progress bars
  - tables
  - action buttons
  - refresh interval

## Phase 4 - Termux Integration

- Keep the current `termux` console command and `-s` script alias scope.
- Add `termux -setup`.
  - First pass done: explain required Termux setting, script folder convention, script alias example, and Android permission prompt flow.
  - Helper docs done: `retui-helper.sh` with `retui_output` and `retui_module` examples.
- Callback authorization foundation is started.
  - `retui-token -status`
  - `retui-token -show`
  - `retui-token -rotate`
  - `retui-token -on`
  - `retui-token -off`
  - Callbacks remain disabled by default until a token is explicitly created/enabled.
- Callback receiver is token-gated and intentionally narrow.
  - Safe first actions: `output`, `notify`, `module_set`.
  - `module_set` now updates custom script modules.
  - Hold dangerous/general `command` execution for a later opt-in "danger mode"; do not add it without a design pass.
- Example helper API:
  - `retui output "Backup complete"`
  - `retui notify "Server down" "prod-api failed health check"`
  - `retui module set server "HTTP OK\nCPU 42%"`
  - `retui module refresh server`

## Phase 5 - Tasker Integration

- Reuse the same callback API built for Termux.
- Document Tasker "Send Intent" templates.
- Do not require Termux for Tasker integration.
- Useful Tasker-driven module updates:
  - Wi-Fi/VPN state
  - battery profile
  - location/home-away mode
  - Bluetooth device state
  - calendar/focus mode
  - smart-home shortcuts

## Phase 6 - Webhook Strengthening

- Revisit webhook storage and command UX.
- Add better inspection and test commands.
- Make webhook output/callback behavior consistent with modules.
- Consider module and Termux callback triggers for webhook dispatch.

## Deferred / Watchlist

- True cutout borders for floating tabs
  - Current widget/module tabs use an opaque surface mask to hide the parent border line.
  - Research found Material `TextInputLayout` has a similar outlined floating-label cutout behavior, but it is text-field-specific and not a good visual fit for Re-TUI.
  - Preferred future approach: build a small custom terminal border view/drawable that draws the border in segments and skips the tab rectangle.
  - Estimate: roughly 1 focused day for one reusable custom view/drawable, plus another pass to wire and test it across music, notifications, output tray, app drawer, settings, and Termux.
  - Keep the mask approach for now unless transparent tab backgrounds become a real user pain point.

- Native Android widget hosting
  - Deferred intentionally.
  - Reconsider only if there is strong demand and a stable constrained UX.

- Arbitrary Java/Kotlin user modules
  - Avoid for now due to security, crash, Play policy, and support risk.
  - Prefer script modules with controlled Re-TUI-rendered primitives.

- Per-app notification coloring
  - Deferred unless community demand is strong.
  - Current notification terminal should remain visually consistent with the theme.

## Integrations

- Termux integration
  - Current active branch: `codex/termux-integration`.
  - Current near-term work should focus on safe `termux -setup` polish and script-run UX only.
  - Callback auth and callback intents are intentionally paused until design discussion.

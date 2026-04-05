# Dorgesh-Kaan Chests Plugin

This plugin helps optimize Dorgesh-Kaan chest looting by:
- highlighting chest states,
- showing a hop timing indicator,
- supporting hotkey-based world hopping with filtering,
- tracking optional session stats.

## Core Behavior

### Chest highlighting
- `22681` (lootable): configurable lootable color.
- `22682` (looted/unavailable): configurable looted color.
- `22683` (opening): configurable opening color.

The plugin tracks nearby target chests and applies filled + outlined overlays.

### Active area gating
The plugin only operates when you are within range of target chests.
Outside that range, hop indicators/timers are hidden and timer state is cleared.

### Hop timer trigger
The hop timer starts from this chat line:
- `You attempt to pick the lock.`

Timer duration is configurable in milliseconds.

### Center-screen indicator
Main banner states:
- `HOP IN Xs`
- `HOP NOW`
- `ALL CHESTS LOOTED`
- `OPEN THE WORLD SWITCHER PANEL TO ENABLE AUTO-HOP` (if world list is unavailable)

Special logic:
- If any nearby chest is still lootable, hop indicator is suppressed.
- On relog/logout transitions, stale `HOP NOW` is cleared.

### Post-hop / post-login looted notice
After hop/login, if both nearby chests are unavailable, a short:
- `ALL CHESTS LOOTED`
banner is shown.

## Hotkey Hop

You can configure a hop hotkey. It only acts when hop conditions are met.

### Requirements before hotkey hop
- Indicator must be effectively `HOP NOW`.
- World list must be loaded (open World Switcher panel once in session).

If world list is not loaded, you get a user instruction message and banner text.

### World filtering source
Configurable via `Hop filter source`:
- `INTERNAL`: plugin safety filters.
- `WORLD_HOPPER`: uses World Hopper plugin config values (subscription/region/world type).

### Safety filtering
The plugin excludes unsafe/special worlds and respects:
- members vs free compatibility,
- skill-total requirements (parsed from world activity for skill-total worlds).

## Cycle + Default World Logic

Optional cycle restart behavior:
- Set `Default world` (set `0` to disable).
- Set `Hop cycle length` (example: `15`).

Behavior:
1. Normal hops continue from current world.
2. After `hopCycleLength` hotkey hops, plugin attempts return to `Default world`.
3. Return happens only if eligible and respawn-aware check allows it.
4. After returning to default world, cycle resets.

### Respawn-aware default return
Config:
- `Chest respawn (s)` (default: `300`).

If default-world revisit is due but respawn timer is not ready yet, plugin skips forced return and continues normal hopping.

## AFK-safe Mode

When enabled:
- only action-required banners are shown (`HOP NOW`, `ALL CHESTS LOOTED`),
- banner flashes for visibility,
- one beep is played when entering action-required state.

## Statistics

Optional session stats line under center banner:
- attempts,
- hotkey hops,
- all-looted detections,
- cycle progress.

Enable via `Show statistics`.

## Configuration Reference

- `Lootable color`
- `Looted color`
- `Opening color`
- `Outline width`
- `Fill opacity`
- `Hop hotkey`
- `Hop filter source`
- `Hop timer (ms)`
- `Show statistics`
- `Default world` (`0` = disabled)
- `Hop cycle length`
- `Chest respawn (s)`
- `AFK-safe mode`

## Typical Setup

1. Enable plugin.
2. Set chest colors/opacity as desired.
3. Set `Hop timer (ms)` for your latency/tick comfort.
4. Choose filter source (`INTERNAL` or `WORLD_HOPPER`).
5. Optionally configure:
   - hotkey,
   - default world + cycle length,
   - respawn seconds,
   - AFK-safe mode,
   - statistics.
6. Open World Switcher panel once so world list is available for hotkey hops.

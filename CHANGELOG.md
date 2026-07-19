# Changelog

All notable changes to MiningStats will be documented in this file.

## [1.3.1] - 2026-07-19

### Fixed
- **The session clock no longer runs while the session is paused, and the
  duration can no longer go negative.** "Paused" was tracked twice: one flag
  drove the HUD pause glyph while the clock did its own arithmetic, and after
  joining a world the clock counted up although the glyph showed a pause.
  Resuming a session in which nothing had been mined yet could even produce a
  negative duration. The timer is now a plain stopwatch, so the pause state is
  the single source of truth and negative values are structurally impossible.
- The reset key no longer stops a running session — it restarts the counters
  from zero and keeps running; a paused session stays paused.
- A saved session that cannot be restored (e.g. its ore names no longer resolve)
  no longer leaves its duration behind on the fresh session.

## [1.3.0] - 2026-07-14

### Added
- **NeoForge support** for Minecraft 1.21-1.21.5 - MiningStats now ships as both
  a Fabric and a NeoForge jar from one codebase (Architectury multiloader layout)

### Changed
- Restructured into `common` / `fabric` / `neoforge` modules; all game logic is shared
- Keybinds, the HUD layer and the tick hook are now registered through Architectury API
- Architectury API is now a required dependency; ModMenu and YACL are compile-only

## [1.2.0] - 2026-07-14

### Changed
- Unified version to 1.2.0 across all supported Minecraft version branches
- Standardized release jar naming to `miningstats-fabric-<version>+mc<range>`
- Corrected author and contact metadata (Modrinth and GitHub links)

## [1.1.0]

### Added
- Deepslate ore variants tracked separately with configurable merge toggle
- Session persistence: save and restore sessions across world leaves
- Configurable milestone notifications per ore type
- Custom ore tracking via block IDs (e.g. modded ores)
- Milestones configuration UI in Cloth Config screen
- Session restored notification when rejoining a world
- German localization (de_de)

### Improved
- Enchantment lookups cached for better performance
- Session summary respects mergeDeepslate setting
- HUD display supports merged deepslate counts and fortune bonuses

## [1.0.0]

### Added
- Real-time HUD overlay showing mined ore statistics
- Fortune bonus tracking and calculation
- Per-session and all-time statistics
- Configurable HUD position and appearance
- Keybind to toggle HUD visibility (default: H)
- Keybind to reset session stats (default: J)
- Support for all vanilla ores (Diamond, Emerald, Gold, Iron, Copper, Lapis, Redstone, Coal, Quartz, Ancient Debris)
- Cloth Config integration for settings GUI
- Mod Menu integration
- Localization support (English)
- Build and release GitHub Actions workflows

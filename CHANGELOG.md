# Changelog

All notable changes to MiningStats will be documented in this file.

## [1.5.0] - 2026-07-21

### Fixed
- **The session timer no longer keeps running while the ESC menu is open.** The
  clock measured real time and only ever learned about the pause keybind, so in
  single-player it kept counting although the game itself was frozen. Opening
  the pause menu now pauses the session and closing it resumes — but only if the
  ESC menu was what paused it. A session you paused yourself stays paused, and
  submenus opened from the pause menu keep the pause too.
- **Ores are only counted once the break was actually allowed.** The counter ran
  before the game had decided whether the block could be broken at all, so a
  block you merely clicked was already booked as mined — in adventure mode,
  inside spawn protection, without a tool permitted to break it, or on a game
  master block. Those no longer count.
  Note the limit of a client-side mod: if a *server* plugin (WorldGuard and the
  like) refuses the break, the client cannot see that, and it is still counted.

### Changed
- Version unified across all Minecraft version branches, so every build of this
  release carries the same number.
- Internal: the NeoForge metadata now declares the bundled `hudlibcore` next to
  `hudlib`. Both have always shipped inside the jar (jar-in-jar), only the
  declaration was incomplete — there is nothing extra to install and nothing
  changes in game.

## [1.3.3] - 2026-07-20

### Fixed
- **Crash when mining on Minecraft 1.21 and 1.21.1.** Breaking the first tracked
  ore threw `NoSuchMethodError` and took the game down. This jar covers
  1.21 - 1.21.5, but the enchantment lookup used a call shape that only exists
  from 1.21.2 on: `Registry` did not implement `HolderLookup.RegistryLookup`
  before that version. The lookup now goes through `HolderLookup.Provider`,
  which is identical on every version this jar targets. Silk Touch and Fortune
  detection are unchanged everywhere else.

## [1.3.2] - 2026-07-19

### Added
- **Freely positionable HUD** on Minecraft 1.21–1.21.5 as well: the config screen
  gets the "Edit HUD Position..." button, plus saveable position presets. This was
  the last jar still limited to the four screen corners — every other version
  already had it. HudLib is bundled inside the jar, nothing extra to install.

### Changed
- An existing `hudPosition` setting is migrated automatically to the same spot

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

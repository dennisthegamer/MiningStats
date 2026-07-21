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

## [1.4.1] - 2026-07-19

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

## [1.4.0] - 2026-07-18

### Added
- **Freely positionable HUD**: the config screen has a new "Edit HUD Position..." button that
  opens an editor - drag the HUD anywhere on screen and confirm
- **Position presets**: save, apply, rename and delete HUD positions
- HudLib is bundled inside the jar (jar-in-jar); there is nothing extra to install

### Changed
- The HUD is no longer limited to the four screen corners. An existing `hudPosition`
  setting is migrated automatically to the same spot
- HUD drawing, the ore flash and the "Reset" overlay now come from the shared HudLib;
  the mod-local `HudLayout`/`HudEffects` helpers were removed (behaviour unchanged)

## [1.3.0] - 2026-07-14

### Added
- **NeoForge support** for Minecraft 26.2 - MiningStats now ships as both a Fabric
  and a NeoForge jar from one codebase (fabric-loom + neoforged.moddev, shared srcDir)

### Changed
- Restructured into `common` / `fabric` / `neoforge`; all game logic is shared
- HUD, keybinds and the tick hook are registered per loader (Fabric HudElementRegistry /
  NeoForge RegisterGuiLayersEvent); loader calls routed through a Platform service
- Removed the vendored Fabric HUD API classes (Fabric API now provides them)

## [1.2.0] - 2026-07-14

### Changed
- Unified version to 1.2.0 across all supported Minecraft version branches
- Standardized release jar naming to `miningstats-fabric-<version>+mc<range>`
- Corrected author and contact metadata (Modrinth and GitHub links)

## [1.1.1] - 2026-06-21
- **Support for Minecraft 26.2** — Updated mappings and dependencies for the latest Minecraft version. No functional changes.
- **YACL** Change config library from cloth-config to yet another config lib.

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
- Session management with persistent session data
- Configurable HUD position and appearance
- Keybind to toggle HUD visibility (default: H)
- Keybind to reset session stats (default: J)
- Support for all vanilla ores (Diamond, Emerald, Gold, Iron, Copper, Lapis, Redstone, Coal, Quartz, Ancient Debris)
- Cloth Config integration for settings GUI
- Mod Menu integration
- Localization support (English)
- Build and release GitHub Actions workflows

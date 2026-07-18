# Changelog

All notable changes to MiningStats will be documented in this file.

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

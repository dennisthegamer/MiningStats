# Changelog

All notable changes to MiningStats will be documented in this file.

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

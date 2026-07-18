# MiningStats

A client-side Fabric mod that tracks mined ores, calculates Fortune bonuses, and displays live stats in a configurable HUD overlay.

![Minecraft](https://img.shields.io/badge/Minecraft-1.21–26.2-green)
![Mod Loader](https://img.shields.io/badge/Mod%20Loader-Fabric-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)
![Version](https://img.shields.io/badge/Version-1.2.0-orange)

## Features

### Ore Tracking
- Automatically counts ores broken with a pickaxe
- Tracks 10 vanilla ore types: Coal, Iron, Gold, Diamond, Emerald, Lapis, Redstone, Copper, Quartz, Ancient Debris
- Supports deepslate variants (optionally merged with normal ores)
- Only counts player-mined ores (ignores TNT, pistons, commands, etc.)
- Support for modded ores via configuration

### Fortune Bonus Calculation
- Detects Fortune enchantment level on your pickaxe
- Calculates actual drops vs. base drops per ore type
- Tracks cumulative fortune bonus per ore and total
- Automatically detects Silk Touch (no bonus counted)

### HUD Overlay
- Non-intrusive on-screen statistics panel with ore icons
- Configurable position: top-left, top-right, bottom-left, bottom-right
- Toggle between full and compact mode with **H**
- Adjustable background opacity
- Shows only when holding a pickaxe (or always, if configured)
- Session timer (HH:MM:SS)

### Session Management
- Per-session tracking that starts when entering a world
- Session summary in chat when leaving a world
- Manual reset with **K**
- Milestone announcements (e.g. "100 Diamonds mined!")

### Visual Effects
- Golden flash effect when mining Diamonds or Ancient Debris
- Sound and chat feedback on session reset

## Installation

### Requirements
- Minecraft 1.21 - 26.2
- [Fabric Loader](https://fabricmc.net/) >= 0.18.3
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 21+

### Optional
- [YACL](https://modrinth.com/mod/yacl) (for in-game configuration screen)
- [Mod Menu](https://modrinth.com/mod/modmenu) (for accessing config via mod list)

### Steps
1. Install Fabric Loader and Fabric API
2. Download the latest `miningstats-fabric-1.2.0+mc26.2.jar` from [Releases](../../releases)
3. Place the JAR in your `.minecraft/mods/` folder
4. Launch Minecraft

## Configuration

Configuration is stored in `.minecraft/config/miningstats.json` and can be edited in-game via Mod Menu + YACL.

| Setting | Default | Description |
|---------|---------|-------------|
| HUD Position | Bottom-Left | Screen corner for the HUD overlay |
| Always Visible | Off | Show HUD even without holding a pickaxe |
| HUD Opacity | 0.6 | Background transparency (0.0 - 1.0) |
| Session Summary | On | Show stats summary in chat when leaving a world |
| Merge Deepslate | On | Count deepslate and normal ore variants together |
| Custom Tracked Ores | — | Additional block IDs for modded ores (e.g. `create:zinc_ore`) |
| Milestones | Diamond: 100, Emerald: 50, Ancient Debris: 25 | Chat announcements at ore count thresholds |

## Keybinds

| Key | Action |
|-----|--------|
| **J** | Start / Pause session |
| **H** | Toggle compact / full HUD mode |
| **K** | Reset current session |

## Building from Source

```bash
git clone https://github.com/DennisTheGamer/MiningStats.git
cd MiningStats
./gradlew build
```

The built JAR will be in `build/libs/`.

## License

This project is licensed under the [MIT License](LICENSE).

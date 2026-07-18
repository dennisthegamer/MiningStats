# ⛏️ MiningStats

**Track every ore you mine. See your Fortune bonus. All in a clean HUD.**

MiningStats is a lightweight, client-side Fabric mod that gives you live mining statistics directly on your screen. Whether you're strip mining for diamonds or hunting ancient debris in the Nether — MiningStats keeps count so you don't have to.

---

## ✨ Features

### 📊 Live Ore Tracking
- **Manual session control** — Start and pause tracking whenever you want with **J**
- **Automatic counting** — Every ore you break during an active session is tracked instantly
- **10 vanilla ores** — Coal, Iron, Gold, Diamond, Emerald, Lapis Lazuli, Redstone, Copper, Nether Quartz, Ancient Debris
- **Deepslate support** — Deepslate variants are recognized and optionally merged with normal ores
- **Modded ore support** — Add custom block IDs in the config (e.g. `create:zinc_ore`)
- **Smart detection** — Only counts player-mined ores (ignores TNT, pistons, commands, etc.)

### 🍀 Fortune Bonus Calculation
- **Real-time tracking** — See exactly how many extra drops Fortune gave you
- **Per-ore breakdown** — Fortune bonus tracked individually for each ore type
- **Silk Touch aware** — Automatically detects Silk Touch and skips bonus counting

### 🖥️ Clean HUD Overlay
- **Ore icons** — Beautiful item icons next to each ore count
- **Two display modes** — Toggle between full and compact HUD with **H**
- **Session timer** — Track how long you've been actively mining (paused time excluded)
- **Pause indicator** — HUD title shows pause status in orange when session is paused
- **Smart visibility** — HUD only appears while holding a pickaxe, keeping your screen clean
- **Adjustable opacity** — Customize the background transparency to your liking

### 🎉 Milestones & Effects
- **Milestone alerts** — Get a chat message when you hit milestones like 100 Diamonds
- **Golden flash** — Visual effect when mining Diamonds or Ancient Debris
- **Session summary** — See your full stats in chat when leaving a world

---

## 🎮 Controls

| Key | Action |
|-----|--------|
| **J** | Start / Pause session |
| **H** | Toggle compact / full HUD mode |
| **K** | Reset current session |

---

## ⚙️ Customization

Configure the mod to your preferences via [Mod Menu](https://modrinth.com/mod/modmenu) + [YACL](https://modrinth.com/mod/yacl), or by editing `.minecraft/config/miningstats.json`:

- HUD position (all 4 screen corners)
- HUD background opacity
- Always visible or only with pickaxe
- Merge deepslate variants on/off
- Custom milestone thresholds
- Modded ore tracking
- Session summary on/off

---

## 📋 Requirements

- **Minecraft:** 1.21 – 26.2
- **Fabric Loader:** 0.18.3 or higher
- **Fabric API:** Required
- **Java:** 21 or higher
- **Mod Menu + YACL:** Optional (for in-game config screen)

---

## 🚀 How It Works

1. You join a world and get a hint to press **J**
2. Press **J** to start your mining session
3. Mine ores — the HUD updates instantly with counts and Fortune bonus
4. Press **J** again to pause tracking (timer pauses too)
5. Hit a milestone? You get a chat notification!
6. Leave the world and get a full session summary

Start, pause, and resume whenever you want — you're in full control.

---

## 💡 Perfect For

- **Strip Miners** — Track your diamond haul across long mining sessions
- **Nether Explorers** — Count every piece of Ancient Debris without pen and paper
- **Fortune Testers** — See exactly how much Fortune is boosting your drops
- **Completionists** — Chase milestone achievements while you mine
- **Everyone** — Anyone who wants to know their mining stats at a glance!

---

## ❓ FAQ

**Does this mod work on servers?**
Yes! It's fully client-side and works on any server without server-side installation.

**Does it affect performance?**
No. MiningStats is lightweight and only processes data when you break an ore block.

**Are stats saved between sessions?**
No. Stats are per-session only and reset when you leave a world. You can also manually reset with **K**.

**Does it detect Fortune correctly?**
Yes. MiningStats reads the Fortune level from your held pickaxe and calculates the bonus based on actual dropped items.

---

## 🌍 Languages

Available in **English** and **German**.

## 🔗 Links

- **Issues & Bugs:** [GitHub Issues](https://github.com/DennisTheGamer/MiningStats/issues)
- **Source Code:** [GitHub Repository](https://github.com/DennisTheGamer/MiningStats)

## 📜 License

This mod is open-source and licensed under the **MIT License**.

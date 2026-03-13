# MiningStats - Implementierungsplan

> Basierend auf [mod.md](mod.md) | Referenzprojekt: `inventory_shulker-template-1.21`
>
> **Architektur: Reine Client-Side Mod** — kein Networking, keine Server-Logik.

---

## 1. Projektsetup

### 1.1 Gradle & Build-System
- Fabric Loom `1.14-SNAPSHOT` als Build-Plugin
- **Java 21** (Source + Target Compatibility)
- **Minecraft:** 1.21.8 (kompatibel mit 1.21–1.21.11)
- **Yarn Mappings:** 1.21.8+build.1
- **Fabric Loader:** 0.18.3
- **Fabric API:** 0.136.1+1.21.8
- **Kein** Split Source Sets — alles in `src/main` (reine Client-Mod)

### 1.2 Dependencies
| Dependency | Verwendung | Bundled? |
|---|---|---|
| Fabric API | Events, Keybinds, HUD Rendering | Nein (Required) |
| Cloth Config API | Config-Screen GUI | Nein (Optional/Suggested) |
| ModMenu | Mod-Menü-Integration | Nein (Dev-only) |

### 1.3 Mod-Metadaten
- **MOD_ID:** `miningstats`
- **Gruppe:** `com.miningstats`
- **Lizenz:** MIT
- **Entrypoint:** Nur `ClientModInitializer` (reine Client-Mod)
- **fabric.mod.json:** Minecraft-Range `>=1.21 <=1.21.11`, `"environment": "client"`

---

## 2. Paketstruktur

```
src/main/java/com/miningstats/
├── MiningStatsClient.java              # ClientModInitializer — Einstiegspunkt
├── config/
│   ├── ModConfig.java                  # JSON Config (Gson), Singleton
│   ├── ConfigScreen.java              # Cloth Config GUI
│   └── ModMenuIntegration.java        # ModMenu Anbindung
├── data/
│   ├── OreType.java                    # Enum: alle Erztypen + Basis-Drop-Werte
│   ├── OreRegistry.java               # Block-ID → OreType Mapping, Modded-Erze
│   └── SessionData.java               # Zähler, Fortune-Bonus, Timer pro Session
├── tracker/
│   ├── OreTracker.java                 # Block-Break-Erkennung + Zähl-Logik
│   └── FortuneTracker.java            # Fortune-Bonus-Berechnung via Mixin-Daten
├── hud/
│   ├── HudRenderer.java              # Haupt-Render-Logik (HUD Overlay)
│   ├── HudLayout.java                # Positionsberechnung, Kompakt/Voll-Modus
│   └── HudEffects.java               # Flash-Effekt, Reset-Nachricht
├── keybind/
│   └── KeybindHandler.java            # H (Kompakt), K (Reset) Keybinds
└── mixin/
    └── ClientPlayerBreakMixin.java    # Mixin für Drop-Erfassung (client-seitig)

src/main/resources/
├── fabric.mod.json
├── miningstats.mixins.json
└── assets/miningstats/
    ├── icon.png
    └── lang/
        ├── en_us.json
        └── de_de.json
```

---

## 3. Implementierung im Detail

### Phase 1: Grundgerüst (Projektsetup)
> Ziel: Mod startet, wird in Minecraft geladen, Logging funktioniert.

- [ ] Gradle-Dateien aufsetzen (`build.gradle`, `settings.gradle`, `gradle.properties`)
- [ ] Gradle Wrapper kopieren/generieren
- [ ] `fabric.mod.json` erstellen mit `"environment": "client"` und Client-Entrypoint
- [ ] `MiningStatsClient.java` — `ClientModInitializer` mit Logger
- [ ] Mixin-Config JSON erstellen (leer, wird in Phase 3 gefüllt)
- [ ] Erster Build-Test: Mod lädt in Minecraft

---

### Phase 2: Ore Tracking (Kernfunktion)
> Ziel: Erze werden beim Abbau erkannt und gezählt.

#### 2.1 OreType Enum
```java
public enum OreType {
    COAL("Coal", 1, Items.COAL),
    IRON("Iron", 1, Items.RAW_IRON),
    GOLD("Gold", 1, Items.RAW_GOLD),
    DIAMOND("Diamond", 1, Items.DIAMOND),
    EMERALD("Emerald", 1, Items.EMERALD),
    LAPIS("Lapis", 4, Items.LAPIS_LAZULI),
    REDSTONE("Redstone", 4, Items.REDSTONE),
    COPPER("Copper", 1, Items.RAW_COPPER),
    QUARTZ("Quartz", 1, Items.QUARTZ),
    ANCIENT_DEBRIS("Ancient Debris", 1, Items.ANCIENT_DEBRIS),
    CUSTOM("Custom", 1, null);  // Für modded Erze

    final String displayName;
    final int baseDrop;
    final Item dropItem;
}
```

#### 2.2 OreRegistry
- `Map<Identifier, OreType>` — mappt Block-IDs auf OreType
- Registriert alle Vanilla-Erze (Normal + Deepslate → gleicher OreType)
- Lädt Custom-Erze aus Config (`tracked_ores` Array)
- Methode: `Optional<OreType> getOreType(Block block)`

#### 2.3 Block-Break Erkennung (Client-seitig)
- **Ansatz:** Client-seitige Events zur Erkennung
- **Option A — Fabric API Event:** `ClientPlayerBlockBreakEvents.AFTER` (falls verfügbar in Fabric API)
- **Option B — Mixin:** `ClientPlayerInteractionManager` → `breakBlock()` mit `@Inject` at `RETURN`
  - Liefert: `BlockPos` → daraus `BlockState` via `world.getBlockState(pos)`
- **Validierung:**
  - Prüfe ob `player.getMainHandStack()` eine Pickaxe ist (Tool-Material-Check oder Item-Tag `minecraft:pickaxes`)
  - Prüfe ob der Block in `OreRegistry` registriert ist
- **Aktion:** `SessionData.incrementOreCount(oreType)`

#### 2.4 SessionData
```java
public class SessionData {
    private final Map<OreType, Integer> oreCounts = new EnumMap<>(OreType.class);
    private final Map<OreType, Integer> fortuneBonus = new EnumMap<>(OreType.class);
    private long sessionStartTime;
    private boolean active;

    public void incrementOreCount(OreType type) { ... }
    public void addFortuneBonus(OreType type, int bonus) { ... }
    public void reset() { ... }
    public Map<OreType, Integer> getOreCounts() { ... }
    public int getTotalOres() { ... }
    public int getTotalFortuneBonus() { ... }
    public long getSessionDuration() { ... }
}
```

---

### Phase 3: Fortune Tracking
> Ziel: Fortune-Bonus wird pro Erz-Break korrekt berechnet.

#### 3.1 FortuneTracker (Client-seitig)
- Input: `OreType`, `ItemStack` (Pickaxe des Spielers)
- Logik:
  1. Prüfe Enchantments der Pickaxe auf Silk Touch → wenn ja, Bonus = 0
  2. Ancient Debris → immer Bonus = 0 (kein Fortune-Effekt)
  3. Hole Fortune-Level der Pickaxe (0, I, II, III)
  4. Hole `baseDrop` vom `OreType`

#### 3.2 Fortune-Bonus-Berechnung
- **Ansatz: Mathematische Berechnung statt Drop-Erfassung**
- Da wir client-seitig keinen Zugriff auf die tatsächlichen Loot-Table-Ergebnisse haben, berechnen wir den **erwarteten** Fortune-Bonus basierend auf Minecrafts Fortune-Formel:
  - Fortune erhöht die Drop-Menge um einen zufälligen Wert zwischen 0 und Fortune-Level
  - Formel: `drops = baseDrop * (1 + random(0, fortuneLevel))`
  - Für den Tracker: Wir erfassen die **tatsächlichen** Drops über einen Mixin
- **Mixin-Ansatz:** `ClientPlayerBreakMixin`
  - Hook in `ClientWorld` oder beobachte `ItemEntity`-Spawns in der Nähe des gebrochenen Blocks
  - Innerhalb eines kurzen Zeitfensters (1-2 Ticks) nach dem Break
  - Zähle gespawnte Items die zum `OreType.dropItem` passen
  - `bonus = gezählteItems - baseDrop`
- **Fallback:** Falls Mixin-Erfassung unzuverlässig → mathematische Schätzung basierend auf Fortune-Level als Option

#### 3.3 Silk Touch Erkennung
- `EnchantmentHelper.getLevel(Enchantments.SILK_TOUCH, stack)` auf der Pickaxe
- Bei Silk Touch > 0: Fortune-Bonus = 0, kein Drop-Tracking für diesen Break

---

### Phase 4: HUD Overlay
> Ziel: Alle Daten werden visuell als Overlay angezeigt.

#### 4.1 HudRenderer (Client)
- **Event:** `HudRenderCallback.EVENT` (Fabric API)
- **Registrierung:** In `MiningStatsClient.onInitializeClient()`
- **Render-Logik:**
  1. Prüfe Sichtbarkeit: `hud_visible_always` ODER Spieler hält Pickaxe
  2. Berechne Position basierend auf `hud_position` Config
  3. Zeichne halbtransparentes Panel (Hintergrund)
  4. Rendere Inhalt je nach Modus (Voll/Kompakt)

#### 4.2 Vollansicht
```
┌─────────────────────────────┐
│ ⛏ Mining Stats              │  ← Weiß, Bold
│                              │
│ [Icon] Coal         87       │  ← Pro Erztyp mit Item-Sprite
│ [Icon] Iron         64       │
│ [Icon] Diamond      12       │
│ ──────────────────────       │  ← Trennlinie
│ Fortune Bonus: +143 Items    │  ← Gold (#FFD700)
│ Session-Zeit: 01:23:45       │  ← Grau
└─────────────────────────────┘
```

#### 4.3 Kompaktansicht
```
⛏ 163 Ores | Fortune: +143
```

#### 4.4 Item-Sprites rendern
- `DrawContext.drawItem()` (1.21 API)
- Skaliert auf 8x8 oder 10x10 Pixel neben dem Text
- Items aus `OreType.dropItem`

#### 4.5 HudEffects
- **Flash-Effekt:** Bei Diamond/Ancient Debris Break → goldener Rand-Flash
  - Timer-basiert (0.5s = 10 Ticks)
  - Zeichne farbiges Overlay über HUD-Rand mit abnehmender Opacity
- **Reset-Meldung:** Gelber Text "Session zurückgesetzt!" für 2s (40 Ticks)
  - Überlagert kurzzeitig den normalen HUD-Inhalt

---

### Phase 5: Keybinds & Session-Management
> Ziel: Tastenkürzel funktionieren, Sessions werden korrekt verwaltet.

#### 5.1 Keybind-Registrierung
```java
// In MiningStatsClient
KeyBinding compactKey = KeyBindingHelper.registerKeyBinding(
    new KeyBinding("key.miningstats.compact", GLFW.GLFW_KEY_H, "category.miningstats")
);
KeyBinding resetKey = KeyBindingHelper.registerKeyBinding(
    new KeyBinding("key.miningstats.reset", GLFW.GLFW_KEY_K, "category.miningstats")
);
```
- Tick-Event (`ClientTickEvents.END_CLIENT_TICK`) prüft `wasPressed()`

#### 5.2 Session-Lifecycle
| Event | Aktion |
|---|---|
| Welt betreten | `SessionData.reset()` + Timer starten |
| Keybind `K` drücken | Summary in Chat → `SessionData.reset()` + Sound + HUD-Meldung |
| Welt verlassen | Summary in Chat (wenn Config aktiv) → Daten verwerfen |

- **Welt betreten/verlassen:** `ClientLifecycleEvents.CLIENT_STARTED` bzw. Mixin/Callback wenn Welt geladen/geschlossen wird
- Alles client-seitig, `MinecraftClient.getInstance().player.sendMessage()` für Chat-Output

#### 5.3 Session-Zusammenfassung (Chat)
- Formatierter Chat-Output mit `Text.literal()` + Styling
- Goldene Farbe für Überschrift, Weiß für Details
- Wird direkt über `client.player.sendMessage(text, false)` ausgegeben
- Kein Networking nötig — alles lokal

---

### Phase 6: Config-System
> Ziel: Alle Einstellungen sind persistent und per GUI änderbar.

#### 6.1 ModConfig.java
- **Format:** JSON via Gson (wie Referenzprojekt)
- **Pfad:** `config/miningstats.json`
- **Felder:**
  ```java
  public String hudPosition = "BOTTOM_LEFT";       // TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
  public boolean hudVisibleAlways = false;
  public float hudOpacity = 0.6f;                   // 0.0–1.0
  public boolean showSessionSummary = true;
  public boolean mergeDeepslate = true;
  public List<String> trackedOres = new ArrayList<>();  // Modded Erze
  public Map<String, Integer> milestones = Map.of(      // Meilenstein-Schwellenwerte
      "diamond", 100,
      "emerald", 50
  );
  ```
- **Methoden:** `load()`, `save()`, `getInstance()`

#### 6.2 ConfigScreen (Cloth Config)
- Kategorien: HUD, Tracking, Session, Meilensteine
- Boolean Toggles, Float Slider (Opacity), Enum Dropdown (Position)
- String-Liste für `trackedOres`

#### 6.3 ModMenu Integration
- `ModMenuApi` implementieren → `getModConfigScreenFactory()`

---

### Phase 7: Feedback & UX Polish
> Ziel: Visuelles und akustisches Feedback, Meilensteine.

#### 7.1 Flash-Effekt
- Trigger: Diamond Ore oder Ancient Debris wird abgebaut
- Direkt client-seitig ausgelöst beim Block-Break-Event
- Animation: Goldener Rand um HUD-Panel, fade-out über 0.5s

#### 7.2 Reset-Sound
- `SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP` oder ähnlich
- Client-seitig abspielen: `client.player.playSound()`

#### 7.3 Meilensteine
- Prüfung nach jedem Ore-Break: Hat ein Erztyp einen Schwellenwert erreicht?
- Chat-Nachricht in Gold: `"⛏ Meilenstein: 100 Diamonds abgebaut!"`
- Schwellenwerte aus Config, pro Erztyp konfigurierbar

---

### Phase 8: Lokalisierung
> Ziel: Alle Texte sind übersetzbar.

- `en_us.json` — Englische Texte
- `de_de.json` — Deutsche Texte
- Keys: `hud.miningstats.title`, `hud.miningstats.fortune_bonus`, etc.
- Config-Keys: `config.miningstats.hud_position`, etc.

---

## 4. Dateien-Übersicht (zu erstellen)

| # | Datei | Phase |
|---|---|---|
| 1 | `build.gradle` | 1 |
| 2 | `settings.gradle` | 1 |
| 3 | `gradle.properties` | 1 |
| 4 | `src/main/resources/fabric.mod.json` | 1 |
| 5 | `src/main/resources/miningstats.mixins.json` | 1 |
| 6 | `src/main/java/.../MiningStatsClient.java` | 1 |
| 7 | `src/main/java/.../data/OreType.java` | 2 |
| 8 | `src/main/java/.../data/OreRegistry.java` | 2 |
| 9 | `src/main/java/.../data/SessionData.java` | 2 |
| 10 | `src/main/java/.../tracker/OreTracker.java` | 2 |
| 11 | `src/main/java/.../tracker/FortuneTracker.java` | 3 |
| 12 | `src/main/java/.../mixin/ClientPlayerBreakMixin.java` | 3 |
| 13 | `src/main/java/.../hud/HudRenderer.java` | 4 |
| 14 | `src/main/java/.../hud/HudLayout.java` | 4 |
| 15 | `src/main/java/.../hud/HudEffects.java` | 4 |
| 16 | `src/main/java/.../keybind/KeybindHandler.java` | 5 |
| 17 | `src/main/java/.../config/ModConfig.java` | 6 |
| 18 | `src/main/java/.../config/ConfigScreen.java` | 6 |
| 19 | `src/main/java/.../config/ModMenuIntegration.java` | 6 |
| 20 | `src/main/resources/assets/.../lang/en_us.json` | 8 |
| 21 | `src/main/resources/assets/.../lang/de_de.json` | 8 |

---

## 5. Offene Fragen / Entscheidungen

1. **Drop-Erfassung für Fortune (Client-seitig):** Auf dem Client können wir `ItemEntity`-Spawns in der Nähe des gebrochenen Blocks beobachten. Alternativ: Mathematische Schätzung basierend auf Fortune-Formel. Beides hat Trade-offs (Zuverlässigkeit vs. Genauigkeit).
2. **Modded Erze:** Für `CUSTOM`-OreType muss der Basis-Drop aus der Config kommen oder dynamisch ermittelt werden. Standardmäßig `baseDrop = 1`.
3. **Icon für Custom Erze:** Item-Sprite kann nicht automatisch ermittelt werden → entweder Block-Item verwenden oder generisches Icon.
4. **Singleplayer vs. Multiplayer:** Als reine Client-Mod funktioniert die Erkennung in Singleplayer zuverlässig. Auf Servern könnte die client-seitige Block-Break-Erkennung durch Server-Lag leicht verzögert sein — akzeptabler Trade-off.

---

## 6. Reihenfolge der Implementierung

```
Phase 1 ──► Phase 2 ──► Phase 3 ──► Phase 4 ──► Phase 5 ──► Phase 6 ──► Phase 7 ──► Phase 8
Grundgerüst   Ore       Fortune      HUD        Keybinds     Config      Polish      i18n
              Track      Track       Overlay    + Sessions
```

Jede Phase baut auf der vorherigen auf. Nach Phase 2 ist die Mod bereits funktional testbar (Konsolenausgabe). Ab Phase 4 wird alles visuell sichtbar.

---

## 7. Vorteile der Client-Only Architektur

- **Kein Networking** — kein Custom-Packet-System, keine S2C/C2S Synchronisation
- **Einfachere Struktur** — kein `src/client` Split, alles in einem Source Set
- **Server-kompatibel** — Mod muss nicht auf dem Server installiert sein
- **Weniger Dateien** — ~21 statt ~25 Dateien, keine Network-Klassen
- **Einfacheres Session-Management** — alles im Client-Speicher, keine Server-Events nötig

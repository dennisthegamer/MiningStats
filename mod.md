**Mod Name:** `MiningStats`

---

> **Erstelle eine Minecraft Forge/Fabric Mod namens „MiningStats" für Minecraft Version z.B. 1.21 bis 1.21.11 auf Fabric.**
>
> ---
>
> ### 🎯 Ziel
> Die Mod verfolgt live alle Erze die der Spieler selbst abbaut und berechnet dabei den tatsächlichen Fortune-Bonus. Alle Daten werden in einem konfigurierbaren HUD-Overlay angezeigt und pro Session gespeichert.
>
> ---
>
> ### ⛏️ Funktion 1: Ore Tracker
> - Die Mod lauscht auf Block-Break-Events und prüft ob der abgebaute Block ein Erz ist.
> - Gezählt werden nur Blöcke die der Spieler **selbst** mit einer Pickaxe abbaut. Folgende Szenarien zählen **nicht**:
    >   - TNT-Explosionen
>   - Pistons die Blöcke verschieben
>   - Andere Spieler
>   - Hopper oder andere Maschinen
>   - `/setblock`-Befehle
> - Unterstützte Vanilla-Erze:
    >   - `coal_ore` + `deepslate_coal_ore`
>   - `iron_ore` + `deepslate_iron_ore`
>   - `gold_ore` + `deepslate_gold_ore` + `nether_gold_ore`
>   - `diamond_ore` + `deepslate_diamond_ore`
>   - `emerald_ore` + `deepslate_emerald_ore`
>   - `lapis_ore` + `deepslate_lapis_ore`
>   - `redstone_ore` + `deepslate_redstone_ore`
>   - `copper_ore` + `deepslate_copper_ore`
>   - `nether_quartz_ore`
>   - `ancient_debris`
> - Modded Erze können per Config als zusätzliche Block-IDs eingetragen werden (z.B. `"create:zinc_ore"`).
> - Deepslate- und Normal-Varianten desselben Erzes werden **zusammengezählt** (ein gemeinsamer Counter pro Erztyp).
> - Pro Erztyp wird gezählt: **Anzahl abgebauter Blöcke** (nicht Drops).
>
> ---
>
> ### ✨ Funktion 2: Fortune Tracker
> - Nach jedem Erz-Break wird berechnet:
    >   - **Basis-Drop** = minimale Drop-Anzahl ohne Enchantment (z.B. Diamond Ore → 1 Diamond)
>   - **Tatsächlicher Drop** = echte Drop-Anzahl nach Fortune-Berechnung
>   - **Fortune Bonus** = Tatsächlicher Drop − Basis-Drop
> - Der Fortune Bonus wird kumuliert über die gesamte Session aufsummiert.
> - Der Tracker erkennt automatisch den Fortune-Level der aktuell gehaltenen Pickaxe (Fortune I, II, III).
> - Silk Touch wird erkannt: bei Silk Touch wird kein Fortune Bonus berechnet (Bonus bleibt 0 für diesen Break).
> - Der Fortune Bonus wird **pro Erztyp separat** gespeichert und zusätzlich als **Gesamtsumme** angezeigt.
> - Berechnungsgrundlage für Basis-Drops pro Erztyp:
    >   | Erz | Basis-Drop |
    >   |---|---|
    >   | Coal | 1 |
    >   | Iron / Gold / Copper | 1 (Raw Item) |
    >   | Diamond | 1 |
    >   | Emerald | 1 |
    >   | Lapis | 4 |
    >   | Redstone | 4 |
    >   | Quartz | 1 |
    >   | Ancient Debris | 1 (kein Fortune-Effekt, immer 0 Bonus) |
>
> ---
>
> ### 🖥️ Funktion 3: HUD-Overlay
> - Ein nicht-störendes Overlay wird auf dem Bildschirm gerendert.
> - **Sichtbarkeit:** Das HUD ist nur sichtbar wenn der Spieler eine Pickaxe in der Hand hält (Haupthand oder Offhand). Konfigurierbar: immer sichtbar oder nur bei Pickaxe.
> - **Position:** Konfigurierbar über Config (Standard: unten links). Optionen: `TOP_LEFT`, `TOP_RIGHT`, `BOTTOM_LEFT`, `BOTTOM_RIGHT`.
> - **Inhalt des HUD:**
    >   - Überschrift: „⛏ Mining Stats" in weißer Fettschrift
>   - Pro Erztyp (nur Erze mit Count > 0): kleines Erz-Icon (Item-Sprite) + Erzname + Anzahl abgebauter Blöcke
>   - Darunter Trennlinie
>   - „Fortune Bonus: +X Items" in goldener Farbe (`#FFD700`)
>   - „Session-Zeit: HH:MM:SS" in grauer Farbe
> - **Kompakt-Modus:** Per Keybind (Standard: `H`) kann zwischen Vollansicht und Kompakt-Modus gewechselt werden. Kompakt-Modus zeigt nur die Gesamtanzahl aller Erze + Fortune Bonus in einer einzelnen Zeile.
> - **Transparenz:** HUD-Hintergrund ist ein halbtransparentes schwarzes Panel (Opacity konfigurierbar, Standard: 60%).
> - Erze mit Count = 0 werden im HUD **nicht** angezeigt (keine leeren Zeilen).
>
> ---
>
> ### 💾 Funktion 4: Session-Management
> - Eine **Session** beginnt wenn der Spieler eine Welt betritt oder den Reset-Keybind drückt.
> - Eine Session endet beim Verlassen der Welt — die Daten werden dabei **nicht** gespeichert (rein in-memory).
> - **Manueller Reset** per Keybind (Standard: `K`): Setzt alle Zähler auf 0 zurück + zeigt kurze Bestätigungsmeldung im HUD („Session zurückgesetzt!") für 2 Sekunden in gelber Farbe.
> - **Session-Zusammenfassung:** Beim Verlassen der Welt (oder bei manuellem Reset) wird im Chat eine Zusammenfassung ausgegeben:
    >   ```
>   ⛏ MiningStats — Session Zusammenfassung
>   Dauer: 01:23:45
>   Abgebaute Erze: 312 gesamt
>     Coal: 87 | Iron: 64 | Diamond: 12 | ...
>   Fortune Bonus: +143 Items
>   ```
> - Die Zusammenfassung kann per Config deaktiviert werden.
>
> ---
>
> ### ⚙️ Config-Datei (`miningstats.toml`)
> | Option | Typ | Standard | Beschreibung |
> |---|---|---|---|
> | `hud_position` | Enum | `BOTTOM_LEFT` | Position des HUD-Overlays |
> | `hud_visible_always` | Boolean | `false` | HUD immer anzeigen, nicht nur bei Pickaxe |
> | `hud_opacity` | Float | `0.6` | Transparenz des HUD-Hintergrunds (0.0–1.0) |
> | `keybind_reset` | String | `"k"` | Keybind für Session-Reset |
> | `keybind_compact` | String | `"h"` | Keybind für Kompakt-Modus |
> | `show_session_summary` | Boolean | `true` | Zusammenfassung beim Verlassen anzeigen |
> | `tracked_ores` | Array | alle Vanilla-Erze | Zusätzliche Block-IDs für modded Erze |
> | `merge_deepslate` | Boolean | `true` | Deepslate + Normal-Varianten zusammenzählen |
>
> ---
>
> ### 🔊 Feedback & UX
> - Beim Abbauen eines Diamanten oder Ancient Debris: kurzer visueller Flash-Effekt am HUD-Rand (goldene Farbe, 0.5 Sekunden) als Highlight.
> - Beim manuellen Reset: „Pling"-Soundeffekt + gelbe HUD-Meldung für 2 Sekunden.
> - Beim Erreichen von Meilensteinen (z.B. 100 Diamanten in einer Session): Chat-Nachricht „⛏ Meilenstein: 100 Diamonds abgebaut!" in goldener Farbe. Meilenstein-Schwellenwerte per Config einstellbar.
>
> ---
>
> Ziel-Plattform: Fabric Version 1.21 bis 1.21.11
> Für Referenzen wie du die Mod am besten bauen kannst ist das existierende Projekt beziehungsweise die existierende Mod an folgendem Pfad zu finden. C:\Users\mager\Downloads\inventory_shulker-template-1.21
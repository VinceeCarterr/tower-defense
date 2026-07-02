# Tower Defense (Java + libGDX)

A minimal but complete 2D tower defense game built with **Java** and **libGDX**.
Enemies march along a fixed path in waves; you spend money to place towers that
auto-target and shoot them. Survive all 5 waves to win.

Built as a portfolio piece. The whole thing is deliberately small and readable
so every system (game loop, sprites, input, collision, waves, economy) is easy
to follow and extend.

![gameplay](docs/screenshot.png) <!-- add your own screenshot/GIF here -->

## Features

- Game loop with fixed-window rendering (60 FPS, vsync)
- Sprite rendering via `SpriteBatch` (textures generated at runtime, no asset files needed)
- Mouse input to place towers on a grid; path tiles are blocked
- Enemy pathfinding along waypoints using vector math (`Vector2`)
- Towers with range detection, nearest-target selection, and fire cooldown
- Homing projectiles with hit detection (basic 2D collision)
- Money economy: earn on kills, spend on towers
- 5 escalating waves, lives, and win/lose states with restart

## Requirements

- **JDK 11 or newer** (JDK 17 recommended)
- That's it. Gradle is downloaded automatically by your IDE; no manual install needed.

## How to run

### Option A — IntelliJ IDEA (easiest)

1. Install [IntelliJ IDEA Community](https://www.jetbrains.com/idea/download/) (free).
2. `File > Open` and select this `tower-defense` folder.
3. IntelliJ detects the Gradle project and downloads libGDX automatically (first
   import takes a minute or two).
4. Open `lwjgl3/src/main/java/com/eldev/td/lwjgl3/DesktopLauncher.java`, click the
   green ▶ next to `main`, and play.

### Option B — command line

If you have Gradle installed:

```bash
gradle :lwjgl3:run
```

To generate a committed Gradle wrapper (so others can run `./gradlew` without
installing Gradle), run once:

```bash
gradle wrapper
```

## Controls

| Action | Input |
|--------|-------|
| Build a tower | Left-click an empty green tile (cost 40) |
| Start next wave early | `SPACE` during the countdown |
| Restart after win/lose | `R` |

## Project structure

```
tower-defense/
├── build.gradle              # dependencies + build config for both modules
├── settings.gradle
├── core/                     # all game logic (platform-independent)
│   └── .../com/eldev/td/
│       ├── TowerDefenseGame.java  # main loop: input -> update -> draw
│       ├── Enemy.java             # walks the waypoint path, has HP
│       ├── Tower.java             # targets nearest enemy in range, fires
│       ├── Projectile.java        # homing bullet + hit detection
│       ├── Assets.java            # runtime-generated textures (swap for PNGs)
│       └── Constants.java         # all tunable numbers (balancing)
└── lwjgl3/                   # desktop launcher (the runnable entry point)
    └── .../lwjgl3/DesktopLauncher.java
```

## How it works (quick tour)

`TowerDefenseGame.render()` runs every frame and does three things: read input,
update the world, draw it. `update()` advances the wave state machine, ticks
every tower (which spawns projectiles), moves projectiles (which damage enemies),
then moves enemies and removes any that died or leaked. Enemies follow a list of
`Vector2` waypoints; towers use straight-line distance for range and targeting;
projectiles home in and "collide" when close enough. All the numbers you'd want
to tweak live in `Constants.java`.

## Ideas to extend (good next commits)

- A second tower type (slow tower, splash tower) and a build-selection UI
- Tower upgrades / selling
- Multiple enemy types (fast/tanky/flying)
- Draw each tower's range circle when hovering (use `ShapeRenderer` line mode)
- Real sprite art instead of generated shapes
- Sound effects (`Gdx.audio.newSound(...)`) and a main menu screen
- Save the path/map to a file and load it (data-driven levels)

## Put it on GitHub

```bash
cd tower-defense
git init
git add .
git commit -m "MVP tower defense in Java + libGDX"
git branch -M main
git remote add origin https://github.com/<your-username>/tower-defense.git
git push -u origin main
```

Add a screenshot or short GIF to the README — recruiters skim, and a visual of a
working game is worth more than the code itself.

---

Built with libGDX 1.12.1.

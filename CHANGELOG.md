# Changelog

All notable changes to **indigo-game-starter-template** will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/) and adheres to [Semantic Versioning](https://semver.org/).

---

## [Unreleased]

### Added

- **Play it online**: every push to `master` runs the tests, builds the optimised game and
  publishes it to GitHub Pages (`.github/workflows/pages.yml`).

---

## [0.1.0] - 2026-09-29

### Added

- **TEA-structured Indigo template**: a root program (`Main`, `Type`, `Update`, `Subscription`) that routes between scenes with `SceneRoute`, wraps each scene's `Msg`, and intercepts child messages (`Outcome.flatMap` as `updateAndCmd`). No Indigo `Scene`s and no messages from child to parent.
- **Scene modules**: every scene is a folder of `Type` / `Update` / `Subscription` / `UI` files with a Haskell-style barrel next to it (`scene/WorldScene.scala` re-exporting `scene/worldscene/`), following "one file, one module".
- **Scene entry effects**: every scene's `init(shared, ...)` returns `Outcome[Model]`; scenes fade in (`FadeInUI`) and play a sound on entry, and the battle scene emits its own `BattleEvent.FightStarted`, which comes back as `Msg.FightStarted`.
- **Stateless and stateful UI**: `XxxUI` modules for stateless UI, and a TEA module folder for stateful UI (`scene/battlescene/subui/actionmenu/`).
- **Example game, Slime Quest**: a top-down RPG with an overworld, NPC dialogue, a quest, chests, wandering slimes, turn-based battles, levelling, and victory / game-over endings.
- **Performance**: the map is drawn with instanced `CloneTiles` from a generated tileset, with a static cached batch and an animated water batch; FPS counter in the top-left corner.
- **Asset generators**: `game/tools/gen_tileset.py` (tileset) and `game/tools/gen_sfx.py` (scene-entry sounds), pure Python with no dependencies.
- **Unit tests** for root routing, scene updates and subscriptions, the action menu, character helpers, the level parser and map reachability.
- **Documentation**: `README.md` guides for Scala / Indigo and Elm / Haskell developers, `doc/code-convention.md` (rules 0-6) and `doc/tea-isomorphism.md` (TEA web app ↔ Indigo game mapping, with ASCII diagrams).

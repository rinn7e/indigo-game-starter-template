package game.common

/** Re-exports every module in `types/`, so consumers write `import game.common.Types.*` (~ a
  * Haskell `module Game.Common.Types` re-exporting `Game.Common.Types.*`, or an index.ts barrel).
  */
object Types:
  export types.Character.*
  export types.Entity.*
  export types.{Direction, Ending, Level, Placement, SceneRoute, Shared, Tile, TileMap}

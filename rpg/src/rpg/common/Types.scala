package rpg.common

/** Re-exports every module in `types/`, so consumers write `import rpg.common.Types.*` (~ a Haskell
  * `module Rpg.Common.Types` re-exporting `Rpg.Common.Types.*`, or an index.ts barrel).
  */
object Types:
  export types.Character.*
  export types.Entity.*
  export types.{Direction, Ending, Level, Placement, SceneRoute, Shared, Tile, TileMap}

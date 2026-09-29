# Workspace Rules

- Always follow the naming, module and TEA-structure rules in the project's
  [Code Convention Document](../doc/code-convention.md), and keep
  [doc/tea-isomorphism.md](../doc/tea-isomorphism.md) in sync with structural changes.
- Before finishing a change: `./mill __.reformat`, then `./mill game.test` (the build uses
  `-Werror`, so warnings such as unused imports fail it). If the compiler reports stale signatures
  through a barrel, run `./mill clean game` first.
- Add user-facing changes to [CHANGELOG.md](../CHANGELOG.md) under `[Unreleased]`.

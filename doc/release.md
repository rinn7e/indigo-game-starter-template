# Release Procedure Guide

Follow this guide to verify, tag and publish a new release of **indigo-game-starter-template**.
Versions follow [Semantic Versioning](https://semver.org/); the changelog follows
[Keep a Changelog](https://keepachangelog.com/).

---

## 1. Pre-Release Verification

Make sure the codebase is formatted, builds cleanly and all tests pass:

```bash
# 1. Format
./mill __.reformat

# 2. Clean build and run all unit tests (the build uses -Werror)
./mill clean game
./mill game.test

# 3. Build the static site
./mill game.indigoBuild
```

Then play it in a real browser (not a headless one, which renders without a GPU and misreports
performance):

```bash
python3 -m http.server 8421 --directory out/game/indigoBuild.dest
```

Open <http://localhost:8421> and check that:

- the FPS counter (top-left) stays at 60 on the title, world and battle scenes;
- every scene fades in and plays its entry sound (the title's is silent until the first key
  press, as browsers block audio before any input);
- a full run works: title → world → battle → back to the world, and an ending → title.

---

## 2. Update the Changelog

In [CHANGELOG.md](../CHANGELOG.md), move the entries under `[Unreleased]` into a new section
for the version, with today's date:

```markdown
## [0.2.0] - 2026-10-15

### Added

- ...
```

Leave an empty `[Unreleased]` section at the top for the next changes.

---

## 3. Commit and Tag

```bash
git add CHANGELOG.md
git commit -m "Release v0.2.0"
git tag -a v0.2.0 -m "v0.2.0"
git push origin master --follow-tags
```

---

## 4. Publish the GitHub Release

Create a release from the tag, using that version's changelog section as the notes:

```bash
gh release create v0.2.0 --title "v0.2.0" --notes-file <(sed -n '/## \[0.2.0\]/,/^---/p' CHANGELOG.md)
```

"""Generates game/assets/tiles.png, the terrain tileset used by the overworld CloneTiles.

Pure Python (no PIL). Each tile is 16x16, laid out left to right in the order of
`game.scene.worldscene.subui.TerrainUI` (keep the two in sync). Run from the project root:

    python3 game/tools/gen_tileset.py
"""

import struct
import zlib
from pathlib import Path

T = 16


def hex_rgba(h, a=1.0):
    h = h.lstrip("#")
    return (int(h[0:2], 16), int(h[2:4], 16), int(h[4:6], 16), int(a * 255))


class Tile:
    def __init__(self):
        self.px = [[(0, 0, 0, 0)] * T for _ in range(T)]

    def put(self, x, y, c):
        if 0 <= x < T and 0 <= y < T:
            r, g, b, a = c
            if a == 255:
                self.px[y][x] = c
            else:
                br, bg, bb, ba = self.px[y][x]
                t = a / 255
                self.px[y][x] = (
                    round(r * t + br * (1 - t)),
                    round(g * t + bg * (1 - t)),
                    round(b * t + bb * (1 - t)),
                    max(ba, a),
                )

    def box(self, x, y, w, h, c):
        for yy in range(y, y + h):
            for xx in range(x, x + w):
                self.put(xx, yy, c)
        return self

    def dot(self, cx, cy, r, c):
        for yy in range(T):
            for xx in range(T):
                if (xx + 0.5 - cx) ** 2 + (yy + 0.5 - cy) ** 2 <= r * r:
                    self.put(xx, yy, c)
        return self


GRASS = hex_rgba("#5a9e4b")
GRASS2 = hex_rgba("#4a8a3d")
WATER = hex_rgba("#2e6fb5")
WAVE = hex_rgba("#7fb7e8")


def grass():
    return Tile().box(0, 0, T, T, GRASS)


def grass_speck():
    return grass().box(4, 9, 2, 2, GRASS2).box(10, 4, 2, 2, GRASS2)


def flowers():
    return (
        grass()
        .dot(4, 5, 2, hex_rgba("#ffd6e0"))
        .dot(11, 10, 2, hex_rgba("#ffe66d"))
        .dot(10, 3, 1, hex_rgba("#ffffff"))
    )


def path():
    return Tile().box(0, 0, T, T, hex_rgba("#c9a66b"))


def path_speck():
    return path().box(3, 6, 2, 2, hex_rgba("#b08d57")).box(11, 11, 2, 2, hex_rgba("#b08d57"))


def floor():
    line = hex_rgba("#7a5c40")
    return Tile().box(0, 0, T, T, hex_rgba("#8d6e4f")).box(0, 7, T, 1, line).box(0, 15, T, 1, line)


def water(variant, frame):
    # Four animation frames of a wave glint sliding right, in two vertical variants.
    x = frame * 3
    y = 4 + variant * 6
    return Tile().box(0, 0, T, T, WATER).box(x, y, 5, 1, WAVE)


def tree():
    return (
        grass()
        .box(3, 13, 10, 3, hex_rgba("#000000", 0.25))
        .box(7, 10, 3, 5, hex_rgba("#6b4226"))
        .dot(8, 7, 7, hex_rgba("#2d6a4f"))
        .dot(6, 5, 3, hex_rgba("#40916c"))
    )


def rock():
    return (
        Tile()
        .box(0, 0, T, T, hex_rgba("#4a4a57"))
        .box(1, 1, 14, 12, hex_rgba("#6b6b78"))
        .box(2, 2, 8, 3, hex_rgba("#8a8a99"))
    )


def wall():
    line = hex_rgba("#7d5a41")
    return (
        Tile()
        .box(0, 0, T, T, hex_rgba("#a0785a"))
        .box(0, 5, T, 1, line)
        .box(0, 11, T, 1, line)
        .box(7, 0, 1, 5, line)
    )


def roof():
    dark = hex_rgba("#8f2d3a")
    return Tile().box(0, 0, T, T, hex_rgba("#b23a48")).box(0, 4, T, 2, dark).box(0, 11, T, 2, dark)


TILES = (
    [grass(), grass_speck(), flowers(), path(), path_speck(), floor()]
    + [water(v, f) for v in range(2) for f in range(4)]
    + [tree(), rock(), wall(), roof()]
)


def write_png(path, tiles):
    width, height = T * len(tiles), T
    raw = b""
    for y in range(height):
        row = b"\x00"
        for tile in tiles:
            for x in range(T):
                row += bytes(tile.px[y][x])
        raw += row

    def chunk(kind, data):
        body = kind + data
        return struct.pack(">I", len(data)) + body + struct.pack(">I", zlib.crc32(body) & 0xFFFFFFFF)

    png = b"\x89PNG\r\n\x1a\n"
    png += chunk(b"IHDR", struct.pack(">IIBBBBB", width, height, 8, 6, 0, 0, 0))
    png += chunk(b"IDAT", zlib.compress(raw, 9))
    png += chunk(b"IEND", b"")
    Path(path).write_bytes(png)


if __name__ == "__main__":
    out = Path(__file__).resolve().parent.parent / "assets" / "tiles.png"
    write_png(out, TILES)
    print(f"wrote {out} ({len(TILES)} tiles)")

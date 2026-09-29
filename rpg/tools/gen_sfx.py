#!/usr/bin/env python3
"""Generates the scene-entry sound effects in rpg/assets/ as small 8-bit style WAV files.

Pure Python (standard library only), like gen_tileset.py. Run from the repository root:

    python3 rpg/tools/gen_sfx.py
"""

import math
import struct
import wave
from pathlib import Path

RATE = 22050
OUT = Path(__file__).resolve().parent.parent / "assets"

# Note frequencies (Hz).
A3, C4, E4, G4, A4 = 220.00, 261.63, 329.63, 392.00, 440.00
C5, D5, E5, G5, C6 = 523.25, 587.33, 659.25, 783.99, 1046.50
EB4, B3, F3 = 311.13, 246.94, 174.61


def square(freq, t):
    return 1.0 if math.sin(2 * math.pi * freq * t) >= 0 else -1.0


def triangle(freq, t):
    return 2 * abs(2 * ((freq * t) % 1) - 1) - 1


def note(freq, length, wave_fn=square, volume=0.35):
    """One note with a short attack and an exponential decay, so notes don't click."""
    n = int(RATE * length)
    samples = []
    for i in range(n):
        t = i / RATE
        attack = min(1.0, i / (RATE * 0.005))
        decay = math.exp(-3.0 * t / length)
        samples.append(wave_fn(freq, t) * attack * decay * volume)
    return samples


def rest(length):
    return [0.0] * int(RATE * length)


def write(name, samples):
    path = OUT / name
    with wave.open(str(path), "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(RATE)
        w.writeframes(
            b"".join(struct.pack("<h", int(max(-1, min(1, s)) * 32767)) for s in samples)
        )
    print(f"wrote {path} ({len(samples) / RATE:.2f}s)")


def main():
    # Title: a bright rising chime.
    write(
        "sfx_title.wav",
        note(C5, 0.09, triangle) + note(E5, 0.09, triangle) + note(G5, 0.09, triangle)
        + note(C6, 0.35, triangle),
    )
    # World: a short, light "off we go".
    write("sfx_world.wav", note(G4, 0.08) + note(C5, 0.08) + note(E5, 0.22))
    # Battle: a low, urgent sting.
    write(
        "sfx_battle.wav",
        note(A3, 0.07, volume=0.4) + rest(0.02) + note(A3, 0.07, volume=0.4) + rest(0.02)
        + note(E4, 0.3, volume=0.4),
    )
    # End (victory): a major fanfare.
    write(
        "sfx_victory.wav",
        note(C5, 0.12) + note(E5, 0.12) + note(G5, 0.12) + note(C6, 0.12) + rest(0.04)
        + note(G5, 0.1) + note(C6, 0.5),
    )
    # End (defeat): a slow falling minor line.
    write(
        "sfx_defeat.wav",
        note(EB4, 0.22, triangle) + note(C4, 0.22, triangle) + note(B3, 0.22, triangle)
        + note(F3, 0.6, triangle),
    )


if __name__ == "__main__":
    main()

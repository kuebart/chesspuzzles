"""
Select 300k puzzles from the Lichess puzzle database with:
- High popularity preference
- Wide rating spread (400-2800)
- Good theme coverage
"""
import zstandard
import csv
import io
import random
from collections import defaultdict

TARGET = 300_000
INPUT = "lichess_db_puzzle.csv.zst"
OUTPUT = "app/src/main/assets/puzzles.csv"

# Rating buckets: 400-800, 800-1200, 1200-1600, 1600-2000, 2000-2400, 2400-2800
RATING_BUCKETS = [(400, 800), (800, 1200), (1200, 1600), (1600, 2000), (2000, 2400), (2400, 2800)]

# Important themes that should be well-represented
KEY_THEMES = {
    "mateIn1", "mateIn2", "mateIn3", "mateIn4", "mateIn5",
    "fork", "pin", "skewer", "discoveredAttack", "doubleCheck",
    "sacrifice", "deflection", "decoy", "interference", "overloading",
    "trappedPiece", "hangingPiece", "zugzwang", "quietMove",
    "xRayAttack", "clearance", "intermezzo",
    "backRankMate", "smotheredMate", "arabianMate", "hookMate",
    "opening", "middlegame", "endgame",
    "pawnEndgame", "rookEndgame", "bishopEndgame", "knightEndgame",
    "queenEndgame", "promotion", "enPassant", "castling",
    "short", "long", "veryLong", "oneMove",
    "advantage", "crushing", "equality",
    "master", "masterVsMaster", "superGM"
}

print("Reading and decompressing database...")

# Read all puzzles
puzzles = []
dctx = zstandard.ZstdDecompressor()
with open(INPUT, "rb") as fh:
    reader = dctx.stream_reader(fh)
    text_stream = io.TextIOWrapper(reader, encoding="utf-8")
    csv_reader = csv.reader(text_stream)

    # Skip header
    header = next(csv_reader)
    print(f"Header: {header}")

    count = 0
    for row in csv_reader:
        if len(row) < 8:
            continue
        puzzle_id = row[0]
        fen = row[1]
        moves = row[2]
        rating = int(row[3])
        popularity = int(row[5])
        themes = row[7]

        # Only keep puzzles with positive popularity and reasonable rating
        if popularity > 0 and 400 <= rating <= 2800:
            puzzles.append({
                "id": puzzle_id,
                "fen": fen,
                "moves": moves,
                "rating": rating,
                "popularity": popularity,
                "themes": themes,
                "raw": row
            })

        count += 1
        if count % 1_000_000 == 0:
            print(f"  Read {count:,} lines, kept {len(puzzles):,} so far...")

print(f"Total read: {count:,}, kept after filtering: {len(puzzles):,}")

# Strategy:
# 1. For each rating bucket, sort by popularity and take top puzzles
# 2. Ensure each key theme has at least 2000 puzzles
# 3. Fill remaining slots with most popular puzzles overall

selected_ids = set()
selected = []

def add_puzzle(p):
    if p["id"] not in selected_ids:
        selected_ids.add(p["id"])
        selected.append(p)
        return True
    return False

# Step 1: Ensure theme coverage - at least 2000 per key theme
print("\nStep 1: Ensuring theme coverage...")
theme_puzzles = defaultdict(list)
for p in puzzles:
    for theme in p["themes"].split():
        if theme in KEY_THEMES:
            theme_puzzles[theme].append(p)

# Sort each theme by popularity
for theme in theme_puzzles:
    theme_puzzles[theme].sort(key=lambda x: x["popularity"], reverse=True)

theme_target = 2000
for theme in sorted(KEY_THEMES):
    available = theme_puzzles.get(theme, [])
    added = 0
    for p in available[:theme_target]:
        if add_puzzle(p):
            added += 1
    print(f"  {theme}: added {added} (available: {len(available)})")

print(f"After theme coverage: {len(selected):,} puzzles")

# Step 2: Fill each rating bucket evenly with most popular puzzles
remaining = TARGET - len(selected)
per_bucket = remaining // len(RATING_BUCKETS)

print(f"\nStep 2: Filling rating buckets ({per_bucket:,} per bucket)...")
for lo, hi in RATING_BUCKETS:
    bucket = [p for p in puzzles if lo <= p["rating"] < hi]
    bucket.sort(key=lambda x: x["popularity"], reverse=True)
    added = 0
    for p in bucket:
        if added >= per_bucket:
            break
        if add_puzzle(p):
            added += 1
    print(f"  Rating {lo}-{hi}: added {added}")

print(f"After rating fill: {len(selected):,} puzzles")

# Step 3: Fill remaining with most popular overall
remaining = TARGET - len(selected)
if remaining > 0:
    print(f"\nStep 3: Filling remaining {remaining:,} slots with most popular...")
    all_by_pop = sorted(puzzles, key=lambda x: x["popularity"], reverse=True)
    added = 0
    for p in all_by_pop:
        if added >= remaining:
            break
        if add_puzzle(p):
            added += 1
    print(f"  Added {added}")

print(f"\nFinal count: {len(selected):,} puzzles")

# Print statistics
ratings = [p["rating"] for p in selected]
print(f"Rating range: {min(ratings)} - {max(ratings)}")
print(f"Mean rating: {sum(ratings)/len(ratings):.0f}")

# Rating distribution
for lo, hi in RATING_BUCKETS:
    n = sum(1 for r in ratings if lo <= r < hi)
    print(f"  {lo}-{hi}: {n:,} ({100*n/len(selected):.1f}%)")

# Theme stats
theme_counts = defaultdict(int)
for p in selected:
    for theme in p["themes"].split():
        theme_counts[theme] += 1
print("\nTop themes:")
for theme, count in sorted(theme_counts.items(), key=lambda x: -x[1])[:20]:
    print(f"  {theme}: {count:,}")

# Write output CSV (same format as Lichess: PuzzleId,FEN,Moves,Rating,RatingDeviation,Popularity,NbPlays,Themes)
print(f"\nWriting {OUTPUT}...")
# Shuffle to avoid any ordering bias
random.shuffle(selected)
with open(OUTPUT, "w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)
    for p in selected:
        writer.writerow(p["raw"])

print("Done!")

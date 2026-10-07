import re

with open('shared/src/commonMain/kotlin/com/example/fitness/feature/exercise/data/SampleExerciseData.kt', 'r', encoding='utf-8') as f:
    sample_text = f.read()

baseline_ids = set(re.findall(r'val ([a-zA-Z0-9_]+) = Exercise\(', sample_text))
exercise_ids = set(re.findall(r'Exercise\(\s*id = "([^"]+)"', sample_text))
all_canonical_keys = baseline_ids.union(exercise_ids)

# Extract legacyAliasMap
alias_matches = re.findall(r'"([^"]+)"\s*to\s*"([^"]+)"', sample_text)
alias_map = dict(alias_matches)
print(f"Loaded {len(alias_map)} aliases from legacyAliasMap")

with open('shared/src/commonMain/kotlin/com/example/fitness/feature/exercise/data/ThirtyDayExerciseLibrary.kt', 'r', encoding='utf-8') as f:
    text = f.read()

entries = re.findall(r'id = "([^"]+)",\s*canonicalId = "([^"]+)"', text)

unmatched = []
for entry_id, canon_id in entries:
    resolved = canon_id
    if resolved in alias_map:
        resolved = alias_map[resolved]
    if resolved not in all_canonical_keys:
        unmatched.append((entry_id, canon_id, resolved))

print(f"Total entries: {len(entries)}")
print(f"Unmatched after alias_map: {len(unmatched)}")
for u in unmatched[:15]:
    print(u)

import re

with open('shared/src/commonMain/kotlin/com/example/fitness/feature/exercise/data/SampleExerciseData.kt', 'r', encoding='utf-8') as f:
    sample_text = f.read()

# Extract baseline exercises name and id
# Match: val barbell_bench_press = Exercise(\n id = "barbell_bench_press",\n name = "Barbell Bench Press",
pattern = r'id\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)"'
baseline_exercises = dict(re.findall(pattern, sample_text))
print(f"Loaded {len(baseline_exercises)} baseline exercises with name and id")

with open('shared/src/commonMain/kotlin/com/example/fitness/feature/exercise/data/ThirtyDayExerciseLibrary.kt', 'r', encoding='utf-8') as f:
    text = f.read()

entry_pattern = r'id\s*=\s*"([^"]+)",\s*canonicalId\s*=\s*"([^"]+)",\s*name\s*=\s*"([^"]+)"'
entries = re.findall(entry_pattern, text)
print(f"Loaded {len(entries)} entries from ThirtyDayExerciseLibrary")

name_to_canon = {name.lower().replace(" ", "").replace("-", ""): cid for cid, name in baseline_exercises.items()}

matched_by_canon_id = 0
matched_by_name = 0
not_matched = []

for eid, cid, name in entries:
    if cid in baseline_exercises:
        matched_by_canon_id += 1
    else:
        norm_name = name.lower().replace(" ", "").replace("-", "")
        if norm_name in name_to_canon:
            matched_by_name += 1
            # print(f"Name matched: {cid} -> {name_to_canon[norm_name]} ({name})")
        else:
            not_matched.append((eid, cid, name))

print(f"Matched by canonicalId: {matched_by_canon_id}")
print(f"Matched by name: {matched_by_name}")
print(f"Not matched in baseline: {len(not_matched)}")
for u in not_matched[:15]:
    print(u)

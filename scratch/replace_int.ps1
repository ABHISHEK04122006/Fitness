$filePath = "shared\src\commonMain\kotlin\com\example\fitness\feature\exercise\data\SampleExerciseData.kt"
$text = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
$updated = $text.Replace("ExerciseDifficulty.INTERMEDIATE.name", "ExerciseDifficulty.ADVANCED.name")
[System.IO.File]::WriteAllText($filePath, $updated, [System.Text.Encoding]::UTF8)
Write-Host "Replacement completed successfully."

$filePath = "shared\src\commonMain\kotlin\com\example\fitness\feature\workout\data\WorkoutRepository.kt"
$text = [System.IO.File]::ReadAllText($filePath, [System.Text.Encoding]::UTF8)
$updated = $text.Replace('difficulty = "Intermediate",', 'difficulty = "Beginner",')
[System.IO.File]::WriteAllText($filePath, $updated, [System.Text.Encoding]::UTF8)
Write-Host "Replacement in WorkoutRepository.kt completed."

$lines = Get-Content 'shared\src\commonMain\kotlin\com\example\fitness\feature\exercise\data\ThirtyDayExerciseLibrary.kt'
$bp = ""
$lvl = ""
$list = New-Object System.Collections.Generic.List[Object]
foreach ($line in $lines) {
    if ($line -match 'bodyPart\s*=\s*"([^"]+)"') { $bp = $matches[1] }
    if ($line -match 'level\s*=\s*ExperienceLevel\.([A-Z_]+)') {
        $lvl = $matches[1]
        $list.Add([PSCustomObject]@{ BodyPart = $bp; Level = $lvl })
    }
}
$list | Group-Object BodyPart, Level | Select-Object Name, Count | Format-Table -AutoSize

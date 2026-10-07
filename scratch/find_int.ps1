Get-ChildItem -Path 'shared\src\commonMain' -Recurse -Filter '*.kt' | Select-String 'INTERMEDIATE' | ForEach-Object {
    $p = $_.Path.Replace((Get-Location).Path + "\", "")
    Write-Host "$p : $($_.LineNumber) : $($_.Line.Trim())"
}

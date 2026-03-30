Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

param(
    [string]$OutFile = 'NexusHealth-portable.zip',
    [switch]$IncludeDatabase
)

$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

$excludeDirs = @('.git', 'target', '.idea', '.vscode')
$excludeFiles = @('app.log', 'compile.log')

$files = Get-ChildItem -Force -Recurse -File | Where-Object {
    $relative = $_.FullName.Substring($projectRoot.Length).TrimStart('\', '/')

    foreach ($dir in $excludeDirs) {
        if ($relative -like "$dir/*" -or $relative -like "$dir\\*") { return $false }
    }

    if (-not $IncludeDatabase -and $_.Name -ieq 'clinic.db') { return $false }
    if ($excludeFiles -contains $_.Name) { return $false }

    return $true
}

$relativePaths = $files | ForEach-Object {
    $_.FullName.Substring($projectRoot.Length).TrimStart('\', '/')
}

$destPath = if ([System.IO.Path]::IsPathRooted($OutFile)) { $OutFile } else { Join-Path $projectRoot $OutFile }
if (Test-Path $destPath) { Remove-Item -Force $destPath }

Compress-Archive -Path $relativePaths -DestinationPath $destPath -Force
Write-Host "Created: $destPath"

if (-not $IncludeDatabase) {
    Write-Host 'Note: clinic.db was NOT included. The app will create a fresh DB on first run.'
}

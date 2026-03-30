Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

# Always run from the project root so ${user.dir} points here
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
Set-Location $projectRoot

& .\mvnw.cmd spring-boot:run

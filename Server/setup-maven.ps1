# PowerShell Script to Download and Setup Maven
# Run as Admin: powershell -ExecutionPolicy Bypass -File setup-maven.ps1

$mavenVersion = "3.9.6"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
$mavenDir = "C:\maven"
$mavenHome = "$mavenDir\apache-maven-$mavenVersion"

Write-Host "╔════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
Write-Host "║        Maven Auto-Setup Script for Windows               ║" -ForegroundColor Cyan
Write-Host "╚════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
Write-Host ""

# Check if Maven already installed
if (Get-Command mvn -ErrorAction SilentlyContinue) {
    Write-Host "✅ Maven is already installed!" -ForegroundColor Green
    mvn -version
    exit 0
}

# Create maven directory
Write-Host "📁 Creating Maven directory: $mavenDir" -ForegroundColor Yellow
if (-not (Test-Path $mavenDir)) {
    New-Item -ItemType Directory -Path $mavenDir -Force | Out-Null
}

# Download Maven
$zipPath = "$mavenDir\maven-$mavenVersion.zip"
Write-Host "📥 Downloading Maven $mavenVersion..." -ForegroundColor Yellow
Write-Host "   URL: $mavenUrl" -ForegroundColor Gray

try {
    Invoke-WebRequest -Uri $mavenUrl -OutFile $zipPath -ErrorAction Stop
    Write-Host "✅ Maven downloaded successfully!" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to download Maven!" -ForegroundColor Red
    Write-Host "   Error: $_" -ForegroundColor Red
    exit 1
}

# Extract Maven
Write-Host "📦 Extracting Maven..." -ForegroundColor Yellow
try {
    Expand-Archive -Path $zipPath -DestinationPath $mavenDir -Force
    Remove-Item $zipPath
    Write-Host "✅ Maven extracted successfully!" -ForegroundColor Green
} catch {
    Write-Host "❌ Failed to extract Maven!" -ForegroundColor Red
    exit 1
}

# Add Maven to PATH
Write-Host "🔧 Adding Maven to environment PATH..." -ForegroundColor Yellow

$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
if ($currentPath -notlike "*$mavenHome\bin*") {
    $newPath = "$currentPath;$mavenHome\bin"
    [Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
    Write-Host "✅ Maven added to PATH!" -ForegroundColor Green
} else {
    Write-Host "⏭️  Maven already in PATH" -ForegroundColor Yellow
}

# Refresh PATH in current session
$env:Path += ";$mavenHome\bin"

# Verify installation
Write-Host ""
Write-Host "🔍 Verifying Maven installation..." -ForegroundColor Yellow
$mvnPath = "$mavenHome\bin\mvn.cmd"

if (Test-Path $mvnPath) {
    Write-Host "✅ Maven installation verified!" -ForegroundColor Green
    & $mvnPath -version
    Write-Host ""
    Write-Host "✨ Maven is ready! You may need to restart your terminal." -ForegroundColor Cyan
} else {
    Write-Host "❌ Maven installation failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "📝 Next steps:" -ForegroundColor Cyan
Write-Host "   1. Close and reopen PowerShell/CMD" -ForegroundColor Gray
Write-Host "   2. Navigate to Server folder:" -ForegroundColor Gray
Write-Host "      cd C:\Users\admin\Documents\Project-Online-auction-system\Server" -ForegroundColor White
Write-Host "   3. Run tests:" -ForegroundColor Gray
Write-Host "      mvn test -Dtest=DatabaseOperationTest" -ForegroundColor White

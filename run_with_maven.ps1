$ErrorActionPreference = "Stop"

$mavenVersion = "3.9.6"
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/$mavenVersion/binaries/apache-maven-$mavenVersion-bin.zip"
$localMvnDir = Join-Path $PSScriptRoot ".mvn_portable"
$mavenZip = Join-Path $localMvnDir "maven.zip"
$mavenHome = Join-Path $localMvnDir "apache-maven-$mavenVersion"
$mavenBin = Join-Path $mavenHome "bin"

Write-Host "Checking for Maven..." -ForegroundColor Cyan

# Create local dir
if (!(Test-Path $localMvnDir)) {
    New-Item -ItemType Directory -Force -Path $localMvnDir | Out-Null
}

# Download Maven if not present
if (!(Test-Path $mavenBin)) {
    Write-Host "Maven not found. Downloading Maven $mavenVersion (Portable)..." -ForegroundColor Yellow
    Invoke-WebRequest -Uri $mavenUrl -OutFile $mavenZip
    
    Write-Host "Extracting Maven..." -ForegroundColor Yellow
    Expand-Archive -Path $mavenZip -DestinationPath $localMvnDir -Force
    
    Remove-Item $mavenZip -Force
    Write-Host "Maven installed to $localMvnDir" -ForegroundColor Green
}
else {
    Write-Host "Using portable Maven from $localMvnDir" -ForegroundColor Green
}

# Add to Path temporarily
# Set JAVA_HOME to JDK if detected (Maven needs JDK)
# Set JAVA_HOME to JDK if detected (Maven needs JDK)
# Try to find Java 17 if JAVA_HOME is not set or not 17
if ($env:JAVA_HOME) {
    Write-Host "Using system JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Green
} else {
    Write-Host "JAVA_HOME not set. Please ensure Java 17 is installed and JAVA_HOME is set." -ForegroundColor Yellow
}

$env:Path = "$mavenBin;$env:JAVA_HOME\bin;$env:Path"

# Verify
mvn -version

# Run Spring Boot
Write-Host "Building and Starting Budget Tracker..." -ForegroundColor Cyan
mvn clean spring-boot:run

# For Shaiva or whover is running on windows, not for submitting



# --- CONFIGURATION ---
$CLASS_PATH = "class;lib/*"
$SRC_FILES = "src/*.java"

# 1. CLEANUP & COMPILATION
Write-Host "--- [1/5] Cleaning and Compiling Project ---" -ForegroundColor Cyan
if (Test-Path "class") { Remove-Item -Recurse -Force "class" }
New-Item -ItemType Directory -Path "class" | Out-Null

# Compile all files at once to resolve dependencies
javac -d class -cp "lib/*" $SRC_FILES

if ($LASTEXITCODE -ne 0) { 
    Write-Host "!! Compilation Failed. Please check your Java code errors !!" -ForegroundColor Red
    exit 
}

# 2. START THE GUI MONITOR
Write-Host "--- [2/5] Launching GUI Monitor ---" -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' DisplayNode"

# 3. START THE MASTER ANTENNA
Write-Host "--- [3/5] Starting Master Storage Node ---" -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' MasterAntennaNode"

# Give RabbitMQ a moment to bind exchanges before nodes connect
Start-Sleep -Seconds 2 

# 4. START 4 ANTENNAS (Clockwise Ring: A -> B -> C -> D -> A)
# Coordinates are balanced to center in an 800x600 window
Write-Host "--- [4/5] Starting Ring Topology (4 Antennas) ---" -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' Antenna AntA 250 200 AntB"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' Antenna AntB 430 200 AntC"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' Antenna AntC 430 380 AntD"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' Antenna AntD 250 380 AntA"

Start-Sleep -Seconds 1

# 5. START 4 USERS (Placed near their respective antennas)
Write-Host "--- [5/5] Starting 4 Users ---" -ForegroundColor Cyan
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' User User1 220 170"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' User User2 480 170"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' User User3 480 380"
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -cp '$CLASS_PATH' User User4 220 380"

Write-Host "`nNetwork Deployment Complete!" -ForegroundColor Green
Write-Host "Use the User terminals to send messages (e.g., Target: User3, Message: Hello!)" -ForegroundColor White
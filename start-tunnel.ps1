# Get the directory of the script itself to find the .env file
$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$envPath = Join-Path $scriptDir ".env"

# Load .env file if it exists
if (Test-Path $envPath) {
    Get-Content $envPath -Encoding UTF8 | ForEach-Object {
        if ($_ -match '^\s*DRHONG_PEM_KEY_PATH\s*=\s*(.*)') {
            $path = $matches[1].Trim().Trim('"').Trim("'")
            $env:DRHONG_PEM_KEY_PATH = $path
        }
    }
}

# Check if the variable is set
if (-not (Test-Path env:DRHONG_PEM_KEY_PATH -PathType Any)) {
    $error_message = "[ERROR] DRHONG_PEM_KEY_PATH is not set. Check .env file."
    $host.ui.WriteErrorLine($error_message)
    exit 1
}

# Start the SSH process
$sshArgs = "-N -L 3307:drhong-db.cny6cmeagio6.ap-northeast-2.rds.amazonaws.com:3306 -i `"$env:DRHONG_PEM_KEY_PATH`" ec2-user@43.202.67.248"
Start-Process -FilePath 'ssh.exe' -ArgumentList $sshArgs -WindowStyle Hidden

# A brief, silent wait to ensure the tunnel has a moment to establish.
Start-Sleep -Seconds 3


# AI FleetOps 后端启动脚本
# 自动加载项目根目录的 .env 文件，然后启动 Spring Boot

$envFile = Join-Path $PSScriptRoot ".env"

if (Test-Path $envFile) {
    Write-Host "Loading environment from $envFile"
    foreach ($line in [System.IO.File]::ReadAllLines($envFile)) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) {
            continue
        }
        $idx = $trimmed.IndexOf('=')
        if ($idx -gt 0) {
            $name = $trimmed.Substring(0, $idx).Trim()
            $value = $trimmed.Substring($idx + 1).Trim()
            if (-not [string]::IsNullOrWhiteSpace($name)) {
                [System.Environment]::SetEnvironmentVariable($name, $value, 'Process')
            }
        }
    }
} else {
    Write-Warning ".env file not found at $envFile"
}

Write-Host "Starting AI FleetOps backend..."
mvn spring-boot:run

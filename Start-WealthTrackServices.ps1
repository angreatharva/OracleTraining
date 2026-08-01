<#
.SYNOPSIS
Starts the WealthTrack microservices locally, beginning with Eureka.

.DESCRIPTION
Each service is launched in a background Maven process. Output and errors are
written to the logs directory beside this script.
#>

[CmdletBinding()]
param(
    [int]$EurekaStartupTimeoutSeconds = 90
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$logDirectory = Join-Path $projectRoot 'logs'
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null

function Test-PortListening {
    param([int]$Port)

    return $null -ne (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue)
}

function Start-WealthTrackService {
    param(
        [Parameter(Mandatory)] [string]$Name,
        [Parameter(Mandatory)] [string]$RelativePath,
        [Parameter(Mandatory)] [int]$Port
    )

    if (Test-PortListening -Port $Port) {
        Write-Host "$Name is already listening on port $Port; skipping." -ForegroundColor Yellow
        return
    }

    $serviceDirectory = Join-Path $projectRoot $RelativePath
    $pomFile = Join-Path $serviceDirectory 'pom.xml'
    if (-not (Test-Path $pomFile)) {
        throw "Cannot find $pomFile"
    }

    $standardOutput = Join-Path $logDirectory "$Name.out.log"
    $standardError = Join-Path $logDirectory "$Name.err.log"
    $process = Start-Process -FilePath 'mvn.cmd' `
        -ArgumentList '-f', $pomFile, 'spring-boot:run' `
        -WorkingDirectory $serviceDirectory `
        -WindowStyle Hidden `
        -RedirectStandardOutput $standardOutput `
        -RedirectStandardError $standardError `
        -PassThru

    Write-Host "Started $Name (PID $($process.Id), port $Port)."
}

function Wait-ForPort {
    param(
        [Parameter(Mandatory)] [int]$Port,
        [Parameter(Mandatory)] [int]$TimeoutSeconds
    )

    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (Test-PortListening -Port $Port) {
            Write-Host "Eureka is listening on port $Port." -ForegroundColor Green
            return
        }
        Start-Sleep -Seconds 2
    }

    throw "Eureka did not start on port $Port within $TimeoutSeconds seconds. Check logs\\eureka-server.err.log."
}

Start-WealthTrackService -Name 'eureka-server' -RelativePath 'eurekaServer\eurekaserver' -Port 8080
Wait-ForPort -Port 8080 -TimeoutSeconds $EurekaStartupTimeoutSeconds

@(
    @{ Name = 'user-service';      Path = 'userMicroService\usermicroservice';           Port = 8082 },
    @{ Name = 'product-service';   Path = 'productMicroService\productmicroservice';     Port = 8086 },
    @{ Name = 'bank-service';      Path = 'bankMicroService\bankmicroservice';           Port = 8083 },
    @{ Name = 'portfolio-service'; Path = 'portfolioMicroService\portfoliomicroservice'; Port = 8084 },
    @{ Name = 'trading-service';   Path = 'tradingMicroService\tradingmicroservice';     Port = 8085 },
    @{ Name = 'api-gateway';       Path = 'APIGateway\apigateway';                      Port = 8081 }
) | ForEach-Object {
    Start-WealthTrackService -Name $_.Name -RelativePath $_.Path -Port $_.Port
}

Write-Host "WealthTrack startup requested. Logs: $logDirectory" -ForegroundColor Green

<#
.SYNOPSIS
Starts the WealthTrack microservices locally, beginning with Eureka.

.DESCRIPTION
Each service is launched in a background Maven process. Output and errors are
written to the logs directory beside this script.
#>

[CmdletBinding()]
param(
    [int]$EurekaStartupTimeoutSeconds = 90,

    # Directory used by the JDK to auto-bind the AF_UNIX sockets behind java.nio Selectors.
    # Must not contain spaces - see $jvmArguments below.
    [string]$UnixDomainSocketTempDir = 'C:\wtms-tmp'
)

$ErrorActionPreference = 'Stop'
$projectRoot = $PSScriptRoot
$logDirectory = Join-Path $projectRoot 'logs'
New-Item -ItemType Directory -Path $logDirectory -Force | Out-Null

# Embedded Tomcat opens a java.nio Selector, which on Windows is backed by an AF_UNIX
# socket that the JDK auto-binds inside 'jdk.net.unixdomain.tmpdir' (default: the user's
# temp directory). When that path contains a space - as it does for a Windows profile like
# "C:\Users\Atharva Angre\..." - the native connect() fails with
# "SocketException: Invalid argument: connect", surfacing as
# "IOException: Unable to establish loopback connection" and killing startup *after* the
# database connects successfully. Pointing the JDK at a space-free directory avoids it.
New-Item -ItemType Directory -Path $UnixDomainSocketTempDir -Force | Out-Null
$jvmArguments = "-Djdk.net.unixdomain.tmpdir=$UnixDomainSocketTempDir"

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
    # Every argument is quoted explicitly: Start-Process joins ArgumentList with spaces, so
    # an unquoted path such as "C:\Users\Atharva Angre\..." would be split into two arguments.
    $arguments = @(
        '-f'
        '"{0}"' -f $pomFile
        'spring-boot:run'
        '"-Dspring-boot.run.jvmArguments={0}"' -f $script:jvmArguments
    )

    $process = Start-Process -FilePath 'mvn.cmd' `
        -ArgumentList $arguments `
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

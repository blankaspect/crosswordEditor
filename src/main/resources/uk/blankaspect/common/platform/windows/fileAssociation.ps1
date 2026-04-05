# %s
#
# This automatically generated Windows PowerShell script may be used to add and remove Windows
# Registry entries that associate the following filename extension%s with the application:
%s#
# To remove the association%s, run this script with the command-line argument '-remove'.

param
(
    [switch]$remove = $false
)

$classesRoot    = "HKLM:\SOFTWARE\Classes"
$fileExtsRoot   = "HKCU:\Software\Microsoft\Windows\CurrentVersion\Explorer\FileExts"
$openWithIdsKey = "OpenWithProgids"
$defaultName    = "(Default)"
$extensionsName = "Extensions"

$java = "%s"
$jar  = "%s"
$icon = "%s"

$valueType =
@{
    string       = "String"
    expandString = "ExpandString"
}

$paramSets =
@(
%s)

function splitExtensions($extensions)
{
    return $extensions.split(",")
}

function createKeyMap($params)
{
    $path = Join-Path -Path $classesRoot -ChildPath $params.fileKindKey
    [Collections.ArrayList]$keyMap =
    @(
        @(
            $path,
            @(),
            $true,
            $defaultName,
            $valueType.string,
            $params.fileKindText
        ),
        @(
            $path,
            @(),
            $false,
            $extensionsName,
            $valueType.string,
            $params.extensions
        ),
        @(
            $path,
            @("DefaultIcon"),
            $false,
            $defaultName,
            $valueType.expandString,
            """$icon"""
        ),
        @(
            $path,
            @("shell", "open"),
            $false,
            $defaultName,
            $valueType.string,
            $params.fileOpenText
        ),
        @(
            $path,
            @("shell", "open", "command"),
            $false,
            $defaultName,
            $valueType.expandString,
            """$java"" -jar ""$jar"" ""%%1"""
        )
    )

    foreach ($extension in splitExtensions $params.extensions)
    {
        $path = Join-Path -Path $classesRoot -ChildPath $extension
        [void]$keyMap.Add(@($path, @(), $true, $defaultName, $valueType.string, $params.fileKindKey))
    }
    return $keyMap
}

# If not running as administrator, run script as administrator in new process
if (!([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent() `
        ).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator))
{
    $command = $MyInvocation.MyCommand
    Start-Process -FilePath powershell -Verb RunAs -WindowStyle Hidden `
            -ArgumentList "-File `"$($command.Path)`" `"$($command.UnboundArguments)`""
    Exit
}

# Add or remove registry entries
foreach ($params in $paramSets)
{
    # Remove entries for old extensions
    $path = Join-Path -Path $classesRoot -ChildPath $params.fileKindKey
    if (Test-Path -Path $path)
    {
        $extensions = (Get-ItemProperty -Path $path).$extensionsName
        if ($extensions)
        {
            foreach ($extension in splitExtensions $extensions)
            {
                $path = Join-Path -Path $classesRoot -ChildPath $extension
                if (Test-Path -Path $path)
                {
                    Remove-Item -Path $path -Recurse
                }
            }
        }
    }

    # Add an entry for each kind of file and extension
    foreach ($entry in createKeyMap $params)
    {
        # Concatenate path
        $path = $entry[0]
        foreach ($p in $entry[1])
        {
            $path = Join-Path -Path $path -ChildPath $p
        }

        # Remove existing entry
        if ($entry[2] -and (Test-Path -Path $path))
        {
            Remove-Item -Path $path -Recurse
        }

        # Add entry
        if (-not $remove)
        {
            if (-not (Test-Path -Path $path))
            {
                New-Item -Path $path -Force > $null
            }
            New-ItemProperty -Path $path -Name $entry[3] -Type $entry[4] -Value $entry[5] > $null
        }
    }

    # Add entry for each filename extension
    foreach ($extension in splitExtensions $params.extensions)
    {
        # Remove existing key for extension
        $path = Join-Path -Path $fileExtsRoot -ChildPath $extension
        if (Test-Path -Path $path)
        {
            Remove-Item -Path $path -Recurse
        }

        # Add property
        if (-not $remove)
        {
            $path = Join-Path -Path $path -ChildPath $openWithIdsKey
            if (-not (Test-Path -Path $path))
            {
                New-Item -Path $path -Force > $null
            }
            New-ItemProperty -Path $path -Name $params.fileKindKey -Type None -Value ([byte[]]@()) -Force > $null
        }
    }
}

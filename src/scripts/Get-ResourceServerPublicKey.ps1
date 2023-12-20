<#
    .SYNOPSIS
    Get the public key of the resource server.
    .DESCRIPTION
    Get the public key of the resource server.
    .PARAMETER pubKeyPath
    The path of the public key on the resource server.
    .PARAMETER storePath
    The path to store the public key.
    .PARAMETER userName
    Your Pitt username to ssh into the resource server.
    .PARAMETER machineName
    Name of the machine to ssh into. If not provided then it will default to ritchie.
    .EXAMPLE
    Get-ResourceServerPublicKey -pubKeyPath /afs/pitt.edu/home/s/o/somePittUser123/public/keyFile.pub -storePath .\ -userName yourPittUserName

#>
Param(
    [Parameter(Mandatory = $true)]
    [string]
    $pubKeyPath,
    [Parameter(Mandatory = $true)]
    [string]
    $storePath,
    [Parameter(Mandatory = $true)]
    [string]
    $userName,
    [Parameter(Mandatory = $false)]
    [string]
    $machineName = 'ritchie'
)

function GetResourceServerPubKey(
    [string] $username,
    [string] $machineName,
    [string] $pubKeyPath,
    [string] $storePath
) {
    $command = "scp $username@$machineName.cs.pitt.edu:$pubKeyPath $storePath"
    Invoke-Expression $command
}

GetResourceServerPubKey -username $userName -machineName $machineName -pubKeyPath $pubKeyPath -storePath $storePath
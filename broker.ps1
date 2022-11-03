$todaysDate = (Get-Date).toString("yyyy_MM_dd");


$logFile = "C:\temp\kakfaRestart_$todaysDate.log"
$kafkalogs = 'D:\Kafka\data'

$ManagmentSvcName = 'CatalogManagementWorker' 
$kafkaSvcName = 'kafka' 
$zookepperSvcName = 'zookeeper' 



Function Write-Log {
    param(
        [Parameter(Mandatory = $true)][string] $message,
        [Parameter(Mandatory = $false)]
        [ValidateSet("INFO","WARN","ERROR")]
        [string] $level = "INFO"
    )
    # Create timestamp
    $timestamp = (Get-Date).toString("yyyy/MM/dd HH:mm:ss")
    # Append content to log file
    Write-Output "$timestamp [$level] - $message"
    Add-Content -Path $logFile -Value "$timestamp [$level] - $message"
}

Write-Log -level INFO -message "*****************************************************"
Write-Log -level INFO -message "Script Exection started"

$log = "Stoping " + $ManagmentSvcName  
Write-Log -level INFO -message  $log


$log = "Stoping " + $ManagmentSvcName  
Write-Log -level INFO -message  $log
stop-service -name $ManagmentSvcName


$log = "Stoping " + $kafkaSvcName  
Write-Log -level INFO -message  $log
stop-service -name $kafkaSvcName

$log = "Stoping " + $zookepperSvcName  
Write-Log -level INFO -message  $log
stop-service -name $zookepperSvcName

#start-sleep -s 20
Write-Log -level INFO -message  "Sleep time 20s completed, deleting kafka Logs"

Get-ChildItem -Path $kafkalogs -Include * -File -Recurse | foreach { $_.Delete()}

Write-Log -level INFO -message "kafka Logs Deleted, Starting Services"

#start-sleep -s 20

Write-Log -level INFO -message "Sleep time 20s completed"


$log = "Starting " + $zookepperSvcName  
 Write-Log -level INFO -message $log
start-service -name $zookepperSvcName



$log = "Starting " + $kafkaSvcName  
Write-Log -level INFO -message $log
start-service -name $kafkaSvcName



$a = 1 
DO
{

$log = "Starting " + $ManagmentSvcName  + " Try number " + $a
Write-Log -level INFO -message $log
start-service -name $ManagmentSvcName
$svc=Get-Service -name $ManagmentSvcName
$log =$svc.Name +"service is "+   $svc.Status
Write-Log -level INFO -message $log
$a++
} While (($a -le 10) -and -not($svc.Status -eq "Running") )


Write-Log -level INFO -message "Script Exection stoped"
Write-Log -level INFO -message "*****************************************************"
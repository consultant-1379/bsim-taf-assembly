CREATE
(
parent "ManagedElement=1,IpSystem=1"
identity 1
moType IpAccessHostEt
exception none
nrOfAttributes 2
"ipAddress" String ""
"ipInterfaceMoRef" Ref "null"
)
CREATE
(
parent "ManagedElement=1,IpSystem=1"
identity 1
moType IpSec
exception none
nrOfAttributes 0
)
CREATE
(
parent "ManagedElement=1,SwManagement=1"
identity sctp_host
moType ReliableProgramUniter
exception none
nrOfAttributes 2
"admActiveSlot" Ref "null"
"reliableProgramLabel" String ""

)
CREATE
(
parent "ManagedElement=1"
identity 1
moType SectorEquipmentFunction
exception none
nrOfAttributes 0
)
CREATE
(
parent "ManagedElement=1"
identity S2
moType SectorEquipmentFunction
exception none
nrOfAttributes 0
)
CREATE
(
parent "ManagedElement=1"
identity S3
moType SectorEquipmentFunction
exception none
nrOfAttributes 0
)
CREATE
(
parent "ManagedElement=1"
identity 2
moType SectorEquipmentFunction
exception none
nrOfAttributes 0
)
SET
(
mo "ManagedElement=1,SwManagement=1,ConfigurationVersion=1"
exception none
nrOfAttributes 25
    moType ConfigurationVersion
    exception none
    nrOfAttributes 25
    "ConfigurationVersionId" String "1"
    "actionResult" Struct
        nrOfElements 6
        "actionId" Integer 1
        "configurationVersionName" String ""
        "invokedAction" Integer 3
        "mainResult" Integer 0
        "pathToDetailedInformation" String ""
        "time" String ""

    "additionalActionResultData" Array Struct 0
    "autoCreatedCVIsTurnedOn" Boolean true
    "configAdmCountdown" Integer 900
    "configCountdownActivated" Boolean false
    "configOpCountdown" Integer 0
    "corruptedUpgradePackages" Array Struct 0
    "currentDetailedActivity" Integer 0
    "currentLoadedConfigurationVersion" String ""
    "currentMainActivity" Integer 0
    "currentUpgradePackage" Ref "null"
    "executingCv" String ""
    "lastCreatedCv" String ""
   "listOfHtmlResultFiles" Array String 0
    "missingUpgradePackages" Array Struct 0
    "restoreConfirmationDeadline" String ""
    "rollbackInitCounterValue" Integer 5
    "rollbackInitTimerValue" Integer 30
    "rollbackList" Array String 0
    "rollbackOn" Boolean true
    "startableConfigurationVersion" String ""
    "storedConfigurationVersions" Array Struct 0
    "timeForAutoCreatedCV" String "04:00"
    "userLabel" String ""
)

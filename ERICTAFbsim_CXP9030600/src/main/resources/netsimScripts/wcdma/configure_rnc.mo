CREATE
(
parent "ManagedElement=1,IpSystem=1"
identity 1
moType IpAccessHostPool
exception none
nrOfAttributes 1
)
CREATE
(
parent "ManagedElement=1,RncFunction=1"
identity 1
moType LocationArea
exception none
nrOfAttributes 1
"lac" Integer 1
)
CREATE
(
parent "ManagedElement=1,RncFunction=1,LocationArea=1"
identity 1
moType ServiceArea
exception none
nrOfAttributes 1
"sac" Integer 1
)
CREATE
(
parent "ManagedElement=1,RncFunction=1,LocationArea=1"
identity 1
moType RoutingArea
exception none
nrOfAttributes 1
"rac" Integer 1
)
CREATE
(
parent "ManagedElement=1,RncFunction=1"
identity 1
moType IubLink
exception none
nrOfAttributes 4
"rbsId" Integer 1
"userPlaneTransportOption" Struct
        nrOfElements 2
        "atm" Integer 0
        "ipv4" Integer 1
"controlPlaneTransportOption" Struct
        nrOfElements 2
        "atm" Integer 0
        "ipv4" Integer 1
        "userPlaneIpResourceRef" Ref "ManagedElement=1"
)
CREATE
(
parent "ManagedElement=1,RncFunction=1,IubLink=1"
identity 1
moType NodeSynch
exception none
nrOfAttributes 0
)
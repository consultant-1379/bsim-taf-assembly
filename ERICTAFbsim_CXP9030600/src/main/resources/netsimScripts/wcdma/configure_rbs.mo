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
parent "ManagedElement=1,SwManagement=1"
identity sctp_host
moType ReliableProgramUniter
exception none
nrOfAttributes 2
"admActiveSlot" Ref "null"
"reliableProgramLabel" String ""
)
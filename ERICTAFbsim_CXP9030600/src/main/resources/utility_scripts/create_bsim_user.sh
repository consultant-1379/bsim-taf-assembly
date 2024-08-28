#!/bin/bash
SSH=/bin/ssh
_cmd_="/ericsson/ocs/bin/create_bsim.sh"
/usr/local/bin/expect <<EOD
spawn ${_cmd_}
expect "User bsim added successfully."
expect "Set user password"
expect "New password:\r"
send "bsim01 \r"
expect -re "Re-enter new Password:\r"
send "bsim01 \r"
expect -re "passwd: password successfully changed for bsim"
expect "Successfully added ACL permissions to bsim user\r"
send -- "exit \r"
expect eof
exit
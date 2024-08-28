#!/bin/bash
SSH=/bin/ssh
_cmd_="/opt/ericsson/bin/pwAdmin -c BSIM NORMAL bsim"
/usr/local/bin/expect <<EOD
spawn ${_cmd_}
expect -re ".*\r"
send "bsim01\r"
expect -re ".*\r"
send "bsim01\r"
expect -re ".*\r"
send "exit\r"
expect eof
EOD
#!/bin/sh
##############################################################################
# This script kills the "start.sh" script which is located in the same       #
# directory than the current script. It does nothing if the "start.sh"       #
#  is not running.
##############################################################################
set -u

LOG_LEVEL=1 # 0 = NONE, 1 = INFO, 2 or more = INFO & DEBUG

################################ FUNCTIONS #####################################
info(){
    if [ ${LOG_LEVEL} -gt 0 ]; then
        echo "$1"
     fi
}

debug(){
    if [ ${LOG_LEVEL} -gt 1 ]; then
        echo "[DEBUG] $1"
     fi
}
################################ END FUNCTIONS ##################################

# The script to be killed is assumed to be in the same directory than the running script
script_to_kill="start.sh"
script_name="$0"
script_path=$(readlink -f "${script_name}")
script_dirname=$(dirname ${script_path})

# PIDs having command name that match script_to_kill name
pids=$(pgrep ${script_to_kill})

if [ -z "${pids}" ]; then
    info "${script_to_kill} is not running!"
    exit 0
fi

debug "The path to the ${script_to_kill} is: $script_dirname"

for pid in ${pids}; do
    cmd_dirname="$(pwdx ${pid} 2>/dev/null | awk -F ": " '{print $2}')"
    if [ -n "${cmd_dirname}" ]; then
        # Only kill the PID corresponding to script_to_kill
        if [ ${cmd_dirname} = ${script_dirname} ]; then
            # kill the process and its hypothetical child
            child=$(pgrep -P $pid)
            debug "kill $pid $child 2>/dev/null"
            kill $pid $child 2>/dev/null
            if [ $? = 0 ]; then
                info "The Component Web Service process started with ${script_to_kill} with process identifier ${pid} has been stopped!"
            fi
        fi
    fi
done
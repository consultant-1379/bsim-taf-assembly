/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C)                   ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */

package com.ericsson.oss.bsim.getters.api;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

public class BsimRemoteCommandExecutor {

    CLICommandHelper sshExecutor;

    private static Logger log = Logger.getLogger(BsimRemoteCommandExecutor.class);

    public BsimRemoteCommandExecutor(final Host host) {

        if (host.getHostname().trim().equalsIgnoreCase("ossmaster")) {

            log.info("User is : " + host.getUser());
            sshExecutor = new CLICommandHelper(host, HostGroup.getOssmaster().getNmsadmUser());
        } else if (host.getHostname().trim().equalsIgnoreCase("netsim")) {
            log.info("User is : " + host.getUser());
            sshExecutor = new CLICommandHelper(host);

        } else {

            log.info("User is : " + host.getUser());
            sshExecutor = new CLICommandHelper(host, HostGroup.getOssmaster().getRootUser());
        }
    }

    public String simpleExec(final String cmdWithArgs) {

        return sshExecutor.simpleExec(cmdWithArgs).trim();
    }

    public String Exec(final String cmdWithArgs) {

        return sshExecutor.execute(cmdWithArgs).trim();
    }

}

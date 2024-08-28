/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.operators.BsimOperator;

/**
 * @author xsriset
 */
public class PreCheckRestartCadm implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckRestartCadm.class);

    final BsimOperator bsimOperator = new BsimOperator();

    @Override
    @Test(groups = { "lte.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertTrue(restartCadm());
    }

    /**
     * Restart Cadm as a precheck for Addition of Micro LTE Node
     * Steps:
     * 1. Connect to the omsas and run the following command
     * svcadm disable -s cadm
     * svcadm enable -s cadm
     * 2. Verify its coming online or not by running the commnad "svcs cadm"
     */
    public boolean restartCadm() {
        final Host omsasHost = BsimApiGetter.getHostOmsas();
        final User user = omsasHost.getUsers(UserType.ADMIN).get(0);
        final CLICommandHelper cli_Omas = new CLICommandHelper(omsasHost, user);
        log.info("Executing... svcadm disable -s cadm");
        cli_Omas.simpleExec("svcadm disable -s cadm");
        log.info("Executing... svcadm enable -s cadm");
        cli_Omas.simpleExec("svcadm enable -s cadm");
        if (cli_Omas.getCommandExitValue() == 0) {
            log.info("Verifying if cadm is online or not..");
            final String output = cli_Omas.simpleExec("svcs cadm");
            if (cli_Omas.getCommandExitValue() == 0) {
                log.info("Verified: cadm is online...");
                return output.contains("online");
            }
        }
        log.info("Verified: cadm is not online... Test Case for adding of Micro LTE Node might fail with SCS Exception.");
        return false;
    }

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return restartCadm();
    }

    @Override
    public String getCheckDescription() {
        return "Check the restart of Cadm...";
    }

}

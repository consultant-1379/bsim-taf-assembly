/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author emakaln
 *         Class checks if the DefaultNetworkAccess role is assigned to the nmsadm user in TSS, if it's not an attempt will be made
 *         to assign the role to the nmsadm user.
 */
public class PreCheckDefaultNetworkAccessAssigned implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckDefaultNetworkAccessAssigned.class);

    private static CLICommandHelper ossMasterRootCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static String CREATE_DEFAULT_NETWORK_ACCESS_ROLE_FOR_NMSADM = "bash -c \"/opt/ericsson/bin/roleAdmin -add DefaultNetworkAccess nmsadm\"";

    private static String CHECK_DEFAULT_NETWORK_ACCESS_IS_ASSIGNED = "bash -c \"/opt/ericsson/bin/roleAdmin -contains DefaultNetworkAccess\"";

    Shell shell;

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.robustness.precheck.IBsimPreChecker#doPreCheck(com.ericsson.oss.bsim.data.model.NodeType)
     */
    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkDefaultNetworkAccessRoleIsAssigned();
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.robustness.precheck.IBsimPreChecker#getCheckDescription()
     */
    @Override
    public String getCheckDescription() {
        // TODO Auto-generated method stub (5 Jan 2015:16:49:52 by emakaln)
        return "Check whether the DefaultNetworkAccess role is assigned to nmsadm";
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.robustness.precheck.IBsimPreChecker#doPreCheck()
     */
    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkDefaultNetworkAccessRoleIsAssigned(), true);
    }

    /**
     * @author emakaln
     * @return Returns whether the server has DefaultNetworkAccess role assigned to nmsadm
     */
    private boolean checkDefaultNetworkAccessRoleIsAssigned() {

        log.info("<font color=purple><B>7> Start to check DefaultNetworkAccess role is assigned to nmsadm </B></font>");

        if (ossMasterRootCLICommandHelper.simpleExec(CHECK_DEFAULT_NETWORK_ACCESS_IS_ASSIGNED).contains("nmsadm")) {
            log.info("DefaultNetworkAccess role is assigned to nmsadm");
            return true;
        } else {
            return assignDefaultNetworkAccessRoleToNmsadmUser();
        }
    }

    private boolean assignDefaultNetworkAccessRoleToNmsadmUser() {
        ossMasterRootCLICommandHelper.simpleExec(CREATE_DEFAULT_NETWORK_ACCESS_ROLE_FOR_NMSADM);
        log.info("Assigning DefaultNetworkAccess role to nmsadm");

        if (ossMasterRootCLICommandHelper.simpleExec(CHECK_DEFAULT_NETWORK_ACCESS_IS_ASSIGNED).contains("nmsadm")) {
            log.info("DefaultNetworkAccess role is assigned to nmsadm");
            return true;
        } else {
            log.error("DefaultNetworkAccess role is NOT assigned to nmsadm");
            return false;
        }
    }

}

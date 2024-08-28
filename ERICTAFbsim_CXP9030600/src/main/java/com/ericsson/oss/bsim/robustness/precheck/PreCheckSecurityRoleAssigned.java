package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author efitrob
 * @category Pre check class to check whether the Security_Management role is assigned to nmsadm
 */
public class PreCheckSecurityRoleAssigned implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckSecurityRoleAssigned.class);

    private static CLICommandHelper ossMasterRootCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static String CREATE_SECURITY_ROLE_FOR_NMSADM = "bash -c \"/opt/ericsson/bin/roleAdmin -add Security_Management nmsadm\"";

    private static String CHECK_SECURITY_ROLE_IS_ASSIGNED = "bash -c \"/opt/ericsson/bin/roleAdmin -contains Security_Management\"";

    Shell shell;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkSecurityManagementRoleAssigned();
    }

    @Override
    public String getCheckDescription() {

        return "Check whether the Security_Management role is assigned to nmsadm";
    }

    @Override
    @Test(groups = { "common.precheck", "lte.precheck" }, alwaysRun = true)
    public void doPreCheck() {

        Assert.assertEquals(checkSecurityManagementRoleAssigned(), true);

    }

    /**
     * @author efitrob
     * @return Returns whether the server has Security_Management role assigned to nmsadm
     */
    private boolean checkSecurityManagementRoleAssigned() {

        boolean securityRoleAssigned = false;

        log.info("<font color=purple><B>7> Start to check Security_Management role is assigned to nmsadm </B></font>");

        if (ossMasterRootCLICommandHelper.simpleExec(CHECK_SECURITY_ROLE_IS_ASSIGNED).contains("nmsadm")) {
            log.info("Security_Management role is assigned to nmsadm");
            securityRoleAssigned = true;
        } else {

            ossMasterRootCLICommandHelper.simpleExec(CREATE_SECURITY_ROLE_FOR_NMSADM);
            log.info("Assigning Security_Management role to nmsadm");

            if (ossMasterRootCLICommandHelper.simpleExec(CHECK_SECURITY_ROLE_IS_ASSIGNED).contains("nmsadm")) {
                log.info("Security_Management role is assigned to nmsadm");
                securityRoleAssigned = true;
            } else {
                log.error("Security_Management role is NOT assigned to nmsadm");
                securityRoleAssigned = false;
            }
        }
        return securityRoleAssigned;
    }

}

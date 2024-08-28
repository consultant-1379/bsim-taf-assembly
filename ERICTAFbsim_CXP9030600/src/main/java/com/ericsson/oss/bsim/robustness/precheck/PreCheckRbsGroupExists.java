package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.operators.api.BsimRetrieveServerDataOperator;

public class PreCheckRbsGroupExists implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckRbsGroupExists.class);

    private final BsimRemoteCommandExecutor commandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    private static final String getGroupNameSSHCommand = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt Group | grep %1$s";

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        /*
         * if (nodeType.equals(NodeType.PICO_WCDMA)) {
         * return checkRncGroupExists();
         * }
         */
        return true;
    }

    @Override
    @Test(groups = {}, alwaysRun = false)
    public void doPreCheck() {
        Assert.assertEquals(checkRncGroupExists(), true);
    }

    @Override
    public String getCheckDescription() {

        return "Checking Rbs group exists";
    }

    private boolean checkRncGroupExists() {

        final BsimRetrieveServerDataOperator operator = new BsimRetrieveServerDataOperator();
        final String rncToBeUsed = operator.getFirstAvailableRnc();
        log.info("Checking if groups exist under RNC: " + rncToBeUsed);
        final String groupsUnderRNC = commandExecutor.simpleExec(String.format(getGroupNameSSHCommand, rncToBeUsed));
        if (groupsUnderRNC.isEmpty()) {
            log.error("No groups found under " + rncToBeUsed);
            return false;
        } else {
            log.info("Groups exist under " + rncToBeUsed);
            return true;
        }
    }

}

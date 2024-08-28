package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;

public class PreCheckSTN implements IBsimPreChecker {

    private static Logger log;

    private static BsimRemoteCommandExecutor preCheckSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkSTN();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkSTN(), true);
    }

    /**
     * Check STN Node exists
     *
     * @return true if STN Node exists
     */
    public boolean checkSTN() {

        log = Logger.getLogger(PreCheckSTN.class);
        log.info("<font color=purple><B>8> Start to check Check STN ......</B></font>");
        boolean testResult = false;

        try {

            final String myCmdString = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt StnFunction";

            preCheckSshRemoteCommandExecutor.simpleExec(myCmdString);

            final String result = preCheckSshRemoteCommandExecutor.simpleExec(myCmdString);

            log.info("Executing : /opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt StnFunction");

            if (result == null || result.equals("")) {

                log.info("<font color=red>STN Pre-Check: ERROR - there is NO STN Node on the server, please add one via BSIM STN Tab to continue </font>");
                testResult = false;
            } else {
                log.info("<font color=green>STN Pre-Check: CORRECT - there is an STN Node on the server.</font>");
                testResult = true;

            }

        } catch (final Exception ex) {
            log.warn("Exception Thrown == > " + ex.toString());
        }

        return testResult;
    }

    @Override
    public String getCheckDescription() {

        return "Check the available STN...";
    }
}

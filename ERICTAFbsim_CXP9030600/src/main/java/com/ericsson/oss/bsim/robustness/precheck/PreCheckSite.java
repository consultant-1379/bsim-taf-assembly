package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimCsHandler;

public class PreCheckSite implements IBsimPreChecker {

    private static Logger log;

    private static BsimCsHandler csHandler;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return checkSite();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkSite(), true);
    }

    /**
     * Check Site exists
     * 
     * @return OK if a Site exists
     */
    private boolean checkSite() {

        log = Logger.getLogger(PreCheckSite.class);
        log.info("<font color=purple><B>9> Start to check Check Site ......</B></font>");
        csHandler = new BsimCsHandler("onrm");
        boolean testResult = false;

        try {

            final String myCmdString = "Site";

            final String result = csHandler.getListByType(myCmdString);

            log.info("Executing: /opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt Site");

            if (result != null) {
                log.info("<font color=green>Site Pre-Check: CORRECT - there is an existing Site on the server. </font>");
                testResult = true;
            } else {
                log.error("Site Pre-Check: ERROR - there is NO existing Site on the server, please add one via oex to continue");
                testResult = true;
            }

        } catch (final Exception ex) {
            log.warn("Exception Thrown == > " + ex.toString());
        }

        return testResult;
    }

    @Override
    public String getCheckDescription() {

        return "Check the site...";
    }
}

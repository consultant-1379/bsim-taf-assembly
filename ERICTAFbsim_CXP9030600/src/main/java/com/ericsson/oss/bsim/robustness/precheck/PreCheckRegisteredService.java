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

package com.ericsson.oss.bsim.robustness.precheck;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;

public class PreCheckRegisteredService implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckRegisteredService.class);

    private static BsimRemoteCommandExecutor remoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        String cmdString = "/opt/ericsson/nms_cif_sm/bin/smtool -action OsgiFwk bundle com.ericsson.oss.bsim.server 1.0.1 | grep -i \"Registered Service\"";
        String result = remoteCommandExecutor.simpleExec(cmdString);
        log.info("Check registered service result: " + result);
        if (result.toLowerCase().contains("no registered service")) {
            final String restartBundle = "/opt/ericsson/nms_umts_bsim_server/bin/uninstallBsimOsgi.sh; /opt/ericsson/nms_umts_bsim_server/bin/uninstallAifOsgi.sh; /opt/ericsson/nms_umts_bsim_server/bin/installAifOsgi.sh; /opt/ericsson/nms_umts_bsim_server/bin/installBsimOsgi.sh";
            String restartBundleResult = remoteCommandExecutor.simpleExec(restartBundle);
            log.info("restartBundleResult ----------------> " + restartBundleResult);
            try {
                Thread.sleep(180000);
            } catch (final InterruptedException e1) {
                // TODO Auto-generated catch block (Jan 15, 2019:8:30:13 AM by xchavya)
            }
            if (restartBundleResult.toLowerCase().contains("Could not find bundle") || restartBundleResult.toLowerCase().contains("exception")) {
                return false;
            }
            String aifCmdString = "/opt/ericsson/nms_cif_sm/bin/smtool -action OsgiFwk bundle com.ericsson.oss.aif.server 1.0.3 | grep -i \"Registered Service\"";
            String aifResult = remoteCommandExecutor.simpleExec(aifCmdString);
            log.info("Check registered service aifResult: " + aifResult);
            if (aifResult.toLowerCase().contains("no registered service")) {
                restartBundleResult = remoteCommandExecutor.simpleExec(restartBundle);
                try {
                    Thread.sleep(180000);
                } catch (final InterruptedException e1) {
                    // TODO Auto-generated catch block (Jan 15, 2019:8:30:13 AM by xchavya)
                }
                aifCmdString = "/opt/ericsson/nms_cif_sm/bin/smtool -action OsgiFwk bundle com.ericsson.oss.aif.server 1.0.3 | grep -i \"Registered Service\"";
                aifResult = remoteCommandExecutor.simpleExec(aifCmdString);
                log.info("Check registered service aifResult 22222222222: " + aifResult);
                if (aifResult.toLowerCase().contains("registered service")) {
                    log.info("Restarting OsgiFwk MC ----------> ");
                    remoteCommandExecutor.simpleExec("/opt/ericsson/nms_cif_sm/bin/smtool cold OsgiFwk -reason=other -reasontext=TAF_RUN");
                }
                try {
                    Thread.sleep(180000);
                    cmdString = "/opt/ericsson/nms_cif_sm/bin/smtool -action OsgiFwk bundle com.ericsson.oss.bsim.server 1.0.1 | grep -i \"Registered Service\"";
                    result = remoteCommandExecutor.simpleExec(cmdString);
                    log.info("Check registered service result 222222222: " + result);
                } catch (final InterruptedException e) {
                    // TODO Auto-generated catch block (Jan 15, 2019:8:29:28 AM by xchavya)
                }
                return true;
                // }
            } else {
                remoteCommandExecutor.simpleExec("/opt/ericsson/nms_cif_sm/bin/smtool cold OsgiFwk -reason=other -reasontext=TAF_RUN");
                try {
                    Thread.sleep(180000);
                    cmdString = "/opt/ericsson/nms_cif_sm/bin/smtool -action OsgiFwk bundle com.ericsson.oss.bsim.server 1.0.1 | grep -i \"Registered Service\"";
                    result = remoteCommandExecutor.simpleExec(cmdString);
                    log.info("Check registered service result 222222222: " + result);
                } catch (final InterruptedException e) {
                    // TODO Auto-generated catch block (Jan 15, 2019:8:29:28 AM by xchavya)

                }
                return true;
            }
        } else {
            if (result.toLowerCase().contains("registered service")) {
                return true;
            } else {
                return false;
            }
        }
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(doPreCheck(null), true);
    }

    @Override
    public String getCheckDescription() {

        return "Check registered service for bundle com.ericsson.oss.bsim.server_1.0.1...";
    }

}

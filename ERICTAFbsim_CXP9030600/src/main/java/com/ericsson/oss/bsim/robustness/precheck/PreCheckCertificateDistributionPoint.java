package com.ericsson.oss.bsim.robustness.precheck;

import static org.testng.Assert.assertTrue;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * @author ejomclo
 *         <p>
 *         Class checks if Certificate Distribution Point is ready.
 *         </p>
 *         <p>
 *         Class not to be added to prechecks yet, waiting on clarification
 *         </p>
 */
public class PreCheckCertificateDistributionPoint implements IBsimPreChecker {

    private static final String CHECK_CDPS_COMMAND = "grep -i \"cdp\" /var/named/*";

    private static final String SCRIPT_EXECUTE_COMMAND = "/opt/ericsson/secinst/bin/config.sh";

    private static final String CHECK_CDPS_ENTRIES_COMMAND = "/usr/sfw/bin/openssl x509 -in cert.pem -text | grep -i \"cdp\"";

    private static Logger log = Logger.getLogger(PreCheckCertificateDistributionPoint.class);

    private static CLICommandHelper omsasCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());

    private static CLICommandHelper infraServerCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostInfraServer());

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return checkCertificateDistributionPoint() && checkCdpEntries();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {

        assertTrue(checkCertificateDistributionPoint() && checkCdpEntries());
    }

    /**
     * <p>
     * Method checks if Certificate Distribution Point
     * </p>
     * 
     * @return boolean
     */
    public boolean checkCertificateDistributionPoint() {

        log.info("<font color=purple><B>Start to check if Certificate Distribution Point is ready......</B></font>");
        int numberOfTries = 0;
        String returned = infraServerCLICommandHelper.simpleExec(CHECK_CDPS_COMMAND);
        Pattern pattern = Pattern.compile("/var/named/vts.com.ip4zone:; CDP\\d{1}");
        Matcher matcher = pattern.matcher(returned);
        int matches = 0;
        while (matcher.find()) {
            ++matches;

        }
        pattern = Pattern.compile("/var/named/vts.com.ip4zone:cdp\\d{1}.cdps.vts.com.");
        matcher = pattern.matcher(returned);
        while (matcher.find()) {
            ++matches;

        }
        boolean isCdpsPresent = matches == 4;

        if (!isCdpsPresent) {
            executeScripts();
            if (++numberOfTries == 2) {
                log.error("Failed to create distribution points. Test failure");
                return false;
            }
            return checkCertificateDistributionPoint();
        }

        return isCdpsPresent;
    }

    private void executeScripts() {

        log.info("Distribution points not present, running /opt/ericsson/secinst/bin/config.sh on INFRA and OMSAS servers...");
        infraServerCLICommandHelper.simpleExec(SCRIPT_EXECUTE_COMMAND);
        omsasCLICommandHelper.simpleExec(SCRIPT_EXECUTE_COMMAND);

    }

    /**
     * <p>
     * Method checks cdp entries in Security Certificates
     * </p>
     * 
     * @return boolean
     */
    private boolean checkCdpEntries() {

        String returned = infraServerCLICommandHelper.simpleExec(CHECK_CDPS_ENTRIES_COMMAND);
        Pattern pattern = Pattern.compile("URI:http://cdp\\d{1}.cdps.vts.com:\\d{2,5}/internal/DSCertCA.crl");
        Matcher matcher = pattern.matcher(returned);
        int matches = 0;
        while (matcher.find()) {
            ++matches;

        }
        return matches == 2;
    }

    @Override
    public String getCheckDescription() {

        return "checking Certificate Distribution Point readyness...";
    }
}

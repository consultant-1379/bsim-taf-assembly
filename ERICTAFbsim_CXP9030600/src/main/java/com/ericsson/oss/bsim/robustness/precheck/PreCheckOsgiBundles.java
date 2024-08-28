package com.ericsson.oss.bsim.robustness.precheck;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;

public class PreCheckOsgiBundles implements IBsimPreChecker {

    private static Logger log;

    private static BsimRemoteCommandExecutor preCheckSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkOsgiBundles();
    }

    @Override
    @Test(groups = { "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkOsgiBundles(), true);
    }

    // // ==========required 21 bundles==========
    // 147 ACTIVE com.ericsson.oss.wlran.bcg.domain_2.0.0.v20090204
    // 148 ACTIVE com.ericsson.oss.wlran.bcg.service_2.0.0.v20090204
    // 149 ACTIVE com.ericsson.oss.wlran.pca.domain_2.0.0.v20090407
    // 150 ACTIVE com.ericsson.oss.wlran.pca.service_2.0.0.v20090407
    // 153 ACTIVE com.ericsson.oss.template_engines.epc-service-impl_1.0.0
    // 168 ACTIVE com.ericsson.oss.pci.domain_1.1.1
    // 169 ACTIVE com.ericsson.oss.pci.service_1.1.1.201302121614
    // 170 ACTIVE com.ericsson.oss.pci.remote.service_1.1.1.201302121614
    // 252 ACTIVE com.ericsson.oss.commons_template_engine_1.0.3.R14E06_EC02
    // 253 ACTIVE
    // com.ericsson.oss.arnefacade_template_engine.service_2.0.2.R14E06_EC02
    // 254 ACTIVE
    // com.ericsson.oss.bulkcm_template_engine.service.impl_2.0.2.R14E06_EC02
    // 255 ACTIVE
    // com.ericsson.oss.filemgr_template_engine.service.impl_2.0.2.R14E06_EC02
    // 256 ACTIVE
    // com.ericsson.oss.arne_template_engine.service.impl_2.0.2.R14E06_EC02
    // 258 ACTIVE
    // com.ericsson.oss.template_engines.smrs_service_impl_2.0.2.R14E06_EC02
    // 259 ACTIVE
    // com.ericsson.oss.dhcp_template_engine.service.impl_2.0.3.R14E06_EC02
    // 260 ACTIVE
    // com.ericsson.oss.dns_template_engine.service.impl_1.0.2.R14E06_EC02
    // 261 ACTIVE com.ericsson.oss.bsim.email_1.0.0.R14E24_EC07
    // 262 ACTIVE com.ericsson.oss.aif.server_1.0.3.R14E24_EC07
    // 263 ACTIVE com.ericsson.oss.bsim.domain_1.0.0.R14E24_EC07
    // 264 ACTIVE com.ericsson.oss.bsim.service_1.0.0.R14E24_EC07
    // 265 ACTIVE com.ericsson.oss.bsim.server_1.0.1.R14E24_EC07
    public boolean checkOsgiBundles() {

        log = Logger.getLogger(PreCheckOsgiBundles.class);

        log.info("<font color=purple><B>Start to check checkOsgiBundles ......</B></font>");
        boolean testResult = false;
        final LinkedHashMap<String, String> outputMap = new LinkedHashMap<String, String>();

        final String myCmdString = "/opt/ericsson/nms_cif_sm/bin/smtool -action OsgiFwk listBundles com.ericsson.oss! com.ericsson.oss!";

        try {

            final StringBuilder sb = new StringBuilder();

            final String result = preCheckSshRemoteCommandExecutor.simpleExec(myCmdString);
            log.info("Executing smtool command \n");

            final Scanner scanner = new Scanner(result);
            final int count = scanThroughLineToFindOsgiBundles(sb, scanner, result);

            if (count >= 21) {
                outputMap.put("OsgiBundles", result);
                testResult = true;
            } else {
                log.warn("The number of filtered bundles are " + count);
                testResult = false;
            }

        } catch (final Exception ex) {
            log.warn("Exception Thrown == > " + ex.toString());
        }

        log.info("End to check checkOsgiBundles ......");

        return testResult;
    }

    private int scanThroughLineToFindOsgiBundles(final StringBuilder sb, final Scanner scanner, final String result) {

        String line = null;
        String consoleMessage = "Relevant OsgiBundles Online\n";
        final String[] bundleKeywords = new String[] { "template_engine", "bsim", "aif", "wlran", "pci" };
        final LinkedHashMap<String, String> outputMap = new LinkedHashMap<String, String>();
        final Pattern pattern = Pattern.compile("(com.+)_[\\d|\\.]{5}");
        System.out.println("pattern" + pattern);

        int count = 0;

        while (scanner.hasNextLine() && (line = scanner.nextLine()) != null) {
            for (final String keyword : bundleKeywords) {
                if (line.contains(keyword)) {
                    final String key = MyFixtureHelper.parseLine(line, 2, pattern);
                    outputMap.put(key, line);
                    count++;
                    break;
                }
            }
        }
        consoleMessage = "Relevant OsgiBundles Online\n";
        printOutDetails(outputMap, consoleMessage);
        return count;

    }

    private void printOutDetails(final LinkedHashMap<String, String> outputMap, final String consoleMessage) {

        final StringBuilder sb = new StringBuilder(consoleMessage + "\r\n");
        for (final Entry<String, String> entry : outputMap.entrySet()) {
            sb.append(String.format("%1$s: %2$s\r\n", entry.getKey(), entry.getValue()));
        }

        log.info(sb.toString());

    }

    private static final class MyFixtureHelper {

        static String parseLine(final String line, final int index, final Pattern pattern) {

            String key = null;

            final String[] arr = line.trim().split("\\s+");

            final String tmp = arr[index];
            if (pattern != null) {
                final Matcher m = pattern.matcher(tmp);
                if (m.find()) {
                    key = m.group(1);
                } else {
                    key = "[KeyNotFound: " + line + "]";
                }

            } else {
                key = tmp;
            }

            return key;
        }

    }

    @Override
    public String getCheckDescription() {

        return "Check Osgi bundles...";
    }
}

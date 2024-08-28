package com.ericsson.oss.bsim.robustness.precheck;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.data.model.BsimPreCheckConstantData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.utils.BsimTestCaseFileHelper;

public class PreCheckSubNetwork implements IBsimPreChecker {

    private static Logger log;

    private final BsimRemoteCommandExecutor preCheckSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    private static RemoteObjectHandler remoteFileHandler;

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkSubNetwork(BsimPreCheckConstantData.SUB_NETWORK_NAME, nodeType);
    }

    @Override
    @Test(groups = { "lte.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkSubNetwork(BsimPreCheckConstantData.SUB_NETWORK_NAME, NodeType.LTE), true);
        Assert.assertEquals(checkSubNetwork(BsimPreCheckConstantData.SUB_NETWORK_NAME, NodeType.MICRO_LTE), true);
        Assert.assertEquals(checkSubNetwork(BsimPreCheckConstantData.SUB_NETWORK_NAME, NodeType.MICRO_MACRO_LTE), true);
    }

    /**
     * Check SubNetwork exists
     * 
     * @param subNetworkName
     * @return OK if Site exists
     */
    public boolean checkSubNetwork(final String subNetworkName, final NodeType nodeType) {

        log = Logger.getLogger(PreCheckSubNetwork.class);
        log.info("<font color=purple><B>7> Start to check checkSubNetwork ......</B></font>");

        remoteFileHandler = BsimApiGetter.getMasterHostFileHandler();
        boolean testResult = false;
        String subnetworkName = null;

        if (nodeType == NodeType.LTE || nodeType == NodeType.MICRO_LTE || nodeType == NodeType.MICRO_MACRO_LTE) {
            subnetworkName = "eRBS";
        }

        try {
            final String myCmdString = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt SubNetwork | grep " + subnetworkName;

            final String checkSubNetwork = preCheckSshRemoteCommandExecutor.simpleExec(myCmdString);

            log.info("Executing: " + myCmdString);

            if (checkSubNetwork == null || checkSubNetwork.equals("")) {

                createErbsSubnetwork();

                final String checkSubNetworkAfterErbsCreated = preCheckSshRemoteCommandExecutor.simpleExec(myCmdString);

                if (checkSubNetworkAfterErbsCreated == null || checkSubNetworkAfterErbsCreated.equals("")) {

                    log.error("SubNetwork Pre-Check: ERROR - there is NO SubNetwork " + subNetworkName + " on the server");
                    testResult = false;
                } else {
                    log.info("<font color=green>SubNetwork Pre-Check: CORRECT - SubNetwork " + subNetworkName + " exists on the server.</font>");
                    testResult = true;
                }
            } else {
                log.info("<font color=green>SubNetwork Pre-Check: CORRECT - SubNetwork " + subNetworkName + " exists on the server.</font>");
                testResult = true;

            }

        } catch (final Exception ex) {
            log.warn("Exception Thrown == > " + ex.toString());
        }

        return testResult;

    }

    private void createErbsSubnetwork() {

        log.info("eRBS SubNetwork is not present ==> Will now try to create eRBS SubNetwork");

        final String createERbsArneTemplate = transferCreateSubNetworkArneXml(preCheckSshRemoteCommandExecutor, remoteFileHandler);

        final String createSubNetworkCommand = "/opt/ericsson/arne/bin/import.sh -F " + createERbsArneTemplate + " -import -i_nau";

        final String response = preCheckSshRemoteCommandExecutor.simpleExec(createSubNetworkCommand);
        System.out.println("The eRBS creation result:\n" + response);

    }

    private String transferCreateSubNetworkArneXml(final BsimRemoteCommandExecutor executor, final RemoteObjectHandler fileHandler) {

        String remoteArneTemplatePath = null;

        // check remote directory and create it if not exist
        final String remoteHomeDirctory = executor.simpleExec("pwd ");
        final String remoteARNEFileDirectory = remoteHomeDirctory + "/ERICTAFbsim_ARNE_FILES/";
        final String checkAndCreateDirCommand = String.format("[ ! -d %1$s ] && mkdir %1$s", remoteARNEFileDirectory);
        log.info("Making directory " + remoteARNEFileDirectory);
        executor.simpleExec(checkAndCreateDirCommand);

        // search local template file and transfer it
        final String arneTemplateName = "Create_SubNetwork_eRBS.xml";
        final LinkedHashMap<String, String> scriptFileMap = BsimTestCaseFileHelper.searchFilesInWorkspace(arneTemplateName, "arne_templates/");
        for (final String fullFilePath : scriptFileMap.values()) {

            fileHandler.copyLocalFileToRemote(fullFilePath, remoteARNEFileDirectory);
            executor.simpleExec("chmod 755 " + fullFilePath);
            log.info("FulL File Path ==> " + fullFilePath + " Transferred XML Directory ==> " + remoteARNEFileDirectory);
        }

        // check transfer result
        final String filesTransferred = executor.simpleExec("ls " + remoteARNEFileDirectory + arneTemplateName);
        if (filesTransferred.contains(arneTemplateName)) {
            final String response = "File " + arneTemplateName + " transferred successfully";
            log.info(response);
            remoteArneTemplatePath = remoteARNEFileDirectory + arneTemplateName;
        }

        return remoteArneTemplatePath;
    }

    @Override
    public String getCheckDescription() {

        return "Check the SubNetwork...";
    }
}

package com.ericsson.oss.bsim.test.cases;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.Assert;

import se.ericsson.jcat.fw.ng.JcatNGTestBase;

import com.ericsson.cifwk.taf.handlers.netsim.implementation.SshNetsimHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.netsim.domain.DG2ConfigLevel;
import com.ericsson.oss.bsim.netsim.domain.LteRbsConfigLevels;
import com.ericsson.oss.bsim.netsim.domain.RbsConfigLevel;
import com.ericsson.oss.bsim.netsim.domain.WcdmaRbsConfigLevels;
import com.ericsson.oss.bsim.operators.BsimOperator;
import com.ericsson.oss.bsim.operators.api.BsimPasServiceOperator;
import com.ericsson.oss.bsim.operators.api.NetsimApiOperator;
import com.ericsson.oss.bsim.robustness.AddNodeValidationManager;
import com.ericsson.oss.bsim.test.verification.AiTemplatesVerification;
import com.ericsson.oss.bsim.test.verification.BcgFilesVerification;
import com.ericsson.oss.bsim.test.verification.DG2AIFiles;
import com.ericsson.oss.bsim.test.verification.DG2NodeExportFileVerification;
import com.ericsson.oss.bsim.test.verification.DG2QrCodeVerification;
import com.ericsson.oss.bsim.test.verification.IBSIMVerification;
import com.ericsson.oss.bsim.test.verification.NodeAddedVerification;
import com.ericsson.oss.bsim.test.verification.NodeExportFileVerification;
import com.ericsson.oss.bsim.test.verification.PlanCreatedVerification;
import com.ericsson.oss.bsim.test.verification.QrCodeVerification;
import com.ericsson.oss.bsim.test.verification.ScramblingCodeVerification;
import com.ericsson.oss.bsim.test.verification.SecurityConfigurationVerification;
import com.ericsson.oss.bsim.test.verification.SiteCreatedVerification;
import com.ericsson.oss.bsim.test.verification.SmrsAccountCreatedVerification;
import com.ericsson.oss.bsim.test.verification.SpitfireAIFiles;
import com.ericsson.oss.bsim.test.verification.SpitfireNodeExportFileVerification;
import com.ericsson.oss.bsim.utils.RemoteFilesHandler;

public class BsimAddMacroNodeHelper extends JcatNGTestBase {
    private static int counter = 1;

    private static final String NETSIM_SET_RBS_CONFIG_LEVEL_OK_ACKNOWLEDGE = "OK";

    private static final Logger log = Logger.getLogger(BsimAddMacroNodeHelper.class);

    // private static String SEG_MASTER_SERVICE = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS";

    private static String ONRM_CS = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS";

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    /*
     * private static String UPDATE_MO_COMMAND =
     * "sa SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01,ManagedElement=1,RncFunction=1 userLabel 211 -p";
     * private static String LIST_PLAN = "lp";
     */

    protected static SshNetsimHandler sshNetsimHandler;

    private static String LIST_ManagedElement = "lt ManagedElement";

    private static String LIST_MO = "lm";

    private static String Set_Attribute = "sa";

    private static final String rncPortName = "TAF_RNC_PORT";

    private String simulationName = "DummyName";

    private BsimOperator netsimOperator;

    private boolean allFilesTransferred = false;

    private boolean netsimHasBeenUsed = false;

    private final RemoteFilesHandler remoteFilesHandler = new RemoteFilesHandler();

    private final String TRIGGER_PATH = "/netsim/inst/";

    private final String RESTART_NETSIM = "restart_netsim";

    public void doDataValidation(final BsimNodeData nodeData) {
        log.info("Validations of test data");

        final AddNodeValidationManager validationMgr = new AddNodeValidationManager();
        final boolean result = validationMgr.doAllValidations(nodeData);

        Assert.assertEquals(result, true);
    }

    public void setRequiredPasParameters(final BsimNodeData nodeData) {

        final BsimPasServiceOperator bsimPasServiceOperator = new BsimPasServiceOperator();
        log.info("Setting required Pas Parameters");

        final String geoFeaturePasParamName = "geoFeature";
        log.info("Setting " + geoFeaturePasParamName + "to value: " + nodeData.isGeoRequired());
        bsimPasServiceOperator.setPasParameterOnMasterServer(BsimPasServiceOperator.AIF_SERVER_PAS_PACKAGE, geoFeaturePasParamName, nodeData.isGeoRequired()
                .toString());

    }

    public void doExecution(final BsimNodeData nodeData, final boolean expectedResult) {

        log.info("Add  node to CS via calling BSIM Service...");
        final BsimOperator bsimOperator = new BsimOperator();

        final boolean actualResult = bsimOperator.addNode(nodeData);
        if (!actualResult && nodeData.getPlanName().equalsIgnoreCase("AddWcdmaNodeExample_TAF_PCA1")) {
            final CLICommandHelper clicommand = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());
            clicommand.simpleExec("cd var/opt/ericsson/log");
            final String filename = clicommand.simpleExec("ls -lrt | grep -i osgifwk_debug | tail -1");
            final String message = clicommand.simpleExec("cat " + filename + " |grep -i subnetwork");

        }
        Assert.assertEquals(actualResult, expectedResult);

    }

    public void executeAddNodeCommandOnBsimServer() {

        final BsimOperator bsimOperator = new BsimOperator();
        bsimOperator.executeAddNodeCommandOnBsimServer();
    }

    public void checkRollbackStatus() {

        // Get Add Node Status info
        // TODO: Not work properly yet
        // bsimOperator.getAddNodeStatus();
    }

    public String doVerification(final BsimNodeData nodeData, final boolean expectedResult) {

        final BsimOperator bsimOperator = new BsimOperator();

        final ArrayList<IBSIMVerification> verificationTasks = new ArrayList<IBSIMVerification>();

        addVerificationTasks(nodeData, expectedResult, verificationTasks);

        for (final IBSIMVerification verificationTask : verificationTasks) {
            boolean testCaseResult = verificationTask.doVerification(bsimOperator);
            if (!testCaseResult && nodeData.getNodeTemplate().equalsIgnoreCase("AddWcdmaNodeExample")) {
                final CLICommandHelper clicommand = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());
                final String filename = clicommand.simpleExec("ls -r /var/opt/ericsson/log | grep -i osgifwk_debug | tail -1");
                log.info("==================================" + filename.trim() + "==========================");
                final String message = clicommand.simpleExec("cat /var/opt/ericsson/log/" + filename.trim() + "|grep -i " + nodeData.getNodeName());
                if (message.contains("Failed execution of template")) {
                    testCaseResult = true;
                }
            }
            if (!testCaseResult && counter == 1) {

                final CLICommandHelper clicommand = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());
                final String filename = clicommand.simpleExec("ls -r /var/opt/ericsson/log | grep -i osgifwk_debug | tail -1");
                log.info("==================================" + filename.trim() + "==========================");
                final String message = clicommand.simpleExec("cat /var/opt/ericsson/log/" + filename.trim() + "|grep -i " + nodeData.getNodeName());
                if (message
                        .contains("IDL:com/ericsson/nms/arne/idl/common/exceptions/ARNESessionExecutionException:1.0,Configuration locked by user nmsadm with action ")) {
                    counter++;
                    return String.valueOf(testCaseResult);
                }
            }

            assertEquals(expectedResult, testCaseResult);
        }
        log.info("Verification Complete for Node: " + nodeData.getNodeFdn());
        return "true";
    }

    private void addVerificationTasks(final BsimNodeData nodeData, final boolean expectedResult, final ArrayList<IBSIMVerification> verificationTasks) {

        verificationTasks.add(new NodeAddedVerification(nodeData));

        if (nodeData.getNodeTemplate().contains("Site")) {
            verificationTasks.add(new SiteCreatedVerification(nodeData));
        }

        if (nodeData.isImportTransportConfiguration() || nodeData.isImportRadioConfiguration()) {
            if (nodeData.getNodeType() != NodeType.WCDMA) {
                verificationTasks.add(new PlanCreatedVerification(nodeData));
            }
        }

        if (nodeData.isImportTransportConfiguration() || nodeData.isImportRadioConfiguration()) {
            verificationTasks.add(new BcgFilesVerification(nodeData));
        }

        if (nodeData.getAifData().isAutoIntegrate()) {

            if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {
                verificationTasks.add(new AiTemplatesVerification(nodeData));

                if (nodeData.getAifData().isWithoutLaptop()) {
                    verificationTasks.add(new SmrsAccountCreatedVerification(nodeData, expectedResult));

                }
            } else if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {

                verificationTasks.add(new DG2AIFiles(nodeData));
                if (nodeData.getAifData().isManualBind() || nodeData.getAifData().getIsNoHardwareBind()) {

                    verificationTasks.add(new SmrsAccountCreatedVerification(nodeData, expectedResult));

                }
            } else if (nodeData.getNodeType() == NodeType.Router_6672) {
                verificationTasks.add(new SpitfireAIFiles(nodeData));

            }
        }
        if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {

            verificationTasks.add(new NodeExportFileVerification(nodeData));
        } else if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {

            verificationTasks.add(new DG2NodeExportFileVerification(nodeData));

        } else if (nodeData.getNodeType() == NodeType.Router_6672) {

            verificationTasks.add(new SpitfireNodeExportFileVerification(nodeData));
        }

        if (nodeData.getAifData().getSecurity() == true) {
            verificationTasks.add(new SecurityConfigurationVerification(nodeData));
        }

        if (nodeData.getNodeType() == NodeType.WCDMA || nodeData.getNodeType() == NodeType.MICRO_WCDMA) {
            if (nodeData.isUseSCProfile()) {
                verificationTasks.add(new ScramblingCodeVerification(nodeData));
            }

            if (nodeData.getNodeType() == NodeType.MICRO_WCDMA && nodeData.getAifData().isAutoIntegrate() == true) {
                verificationTasks.add(new QrCodeVerification(nodeData));
                // verificationTasks.add(new SmrsAccountCreatedVerification(nodeData, expectedResult));
            }
        }
        if (nodeData.getNodeType() == NodeType.LTE && nodeData.getAifData().isWithoutLaptop()) {
            verificationTasks.add(new QrCodeVerification(nodeData));
        }
        if (nodeData.getNodeType() == NodeType.MICRO_MACRO_LTE && nodeData.getAifData().isWithoutLaptop()) {
            verificationTasks.add(new QrCodeVerification(nodeData));

        } else if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {
            verificationTasks.add(new DG2QrCodeVerification(nodeData));
        }
    }

    public void doCleanUpVerification(final ArrayList<BsimNodeData> addedNodes, final boolean expectedResult) {

        final BsimOperator bsimOperator = new BsimOperator();

        for (final BsimNodeData nodeData : addedNodes) {
            final ArrayList<IBSIMVerification> verificationTasks = new ArrayList<IBSIMVerification>();

            addCleanUpVerificationTasks(nodeData, expectedResult, verificationTasks);

            for (final IBSIMVerification verificationTask : verificationTasks) {
                final boolean testCaseResult = verificationTask.doVerification(bsimOperator);
                assertEquals(expectedResult, testCaseResult);
            }
            log.info("Verification Complete " + nodeData.getNodeFdn());
        }
    }

    private void addCleanUpVerificationTasks(final BsimNodeData nodeData, final boolean expectedResult, final ArrayList<IBSIMVerification> verificationTasks) {

        if (nodeData.getNodeType() == NodeType.MICRO_WCDMA) {
            verificationTasks.add(new SmrsAccountCreatedVerification(nodeData, expectedResult));
        }

        if (nodeData.getNodeType() == NodeType.LTE && nodeData.getAifData().isAutoIntegrate()) {
            if (nodeData.getAifData().isWithoutLaptop()) {
                verificationTasks.add(new SmrsAccountCreatedVerification(nodeData, expectedResult));
            }
        }

    }

    public void doNetsimSynchronization(final BsimNodeData nodeData) throws InterruptedException {
        if (nodeData.isEndToEnd()) {
            simulationName = "TAF_" + nodeData.getNodeType().toString() + "_Simulation";
            log.info("Simulation Name chosen ==> " + simulationName);
            try {
                prepareNetsim(nodeData);
                checkAndCreateRNCifRequired(nodeData);
                if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.BasebandT) {
                    runNetsimIntegration(nodeData);
                } else {
                    runNetsimIntegrationDG2(nodeData);
                }

            } catch (final Exception e) {
                log.info(e.getMessage());
            } finally {

                cleanUpNetsim();
                if (nodeData.getNodeType().equals(NodeType.WCDMA) || nodeData.getNodeType().equals(NodeType.MICRO_WCDMA)) {
                    restartOriginalRNC(nodeData.CriticalData.getRncName());
                }
            }
        } else {
            log.info("Netsim Integration not required for this test case");
        }
    }

    /**
     * 
     */
    /*
     * private void updateMo() {
     * // TODO Auto-generated method stub (Sep 2, 2015:1:34:22 PM by xsonaro)
     * log.info("going to update MO: userLabel");
     * String planTaf = null;
     * final String planNames = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + " " + LIST_PLAN);
     * final String[] listOfPlans2 = planNames.split("\n");
     * for (final String listOfPlan : listOfPlans2) {
     * if (listOfPlan.contains("TAF")) {
     * planTaf = listOfPlan.substring(0, listOfPlan.lastIndexOf(":"));
     * break;
     * } else {
     * log.info("TAF plan does not exist");
     * }
     * }
     * final String updateCommand = SEG_MASTER_SERVICE + " " + UPDATE_MO_COMMAND + " " + planTaf;
     * log.info("command for update mo" + updateCommand);
     * ossMasterCLICommandHelper.simpleExec(updateCommand);
     * try {
     * Thread.sleep(40000);
     * } catch (final InterruptedException e) {
     * // TODO Auto-generated catch block (Sep 3, 2015:1:15:42 PM by xsonaro)
     * log.info("plan changes could not be made because of exception" + e);
     * }
     * }
     */

    public void prepareNetsim(final BsimNodeData nodeData) {

        if (!netsimHasBeenUsed) {
            log.info("Prepare for netsim integration");
            log.info("Netsim not configured ==> Netsim will be now be configured");
            NetsimApiOperator.prepareNetsim();
            netsimOperator = new BsimOperator();
            netsimOperator.checkAndStopIpAddressIfStartedInNetsim(nodeData.CriticalData.getIpAddress());

            final RemoteObjectHandler netsimFileHandler = BsimApiGetter.getNetsimRemoteFileHandler();
            transferAndImportSimulationsIfRequired(nodeData.getNodeType(), netsimFileHandler);
            String response = null;
            if (!allFilesTransferred) {
                final String folderToCreateOnNetsim = "user_cmds_taf/";
                final List<String> netsimScripts = FileFinder.findFile(".mo");
                for (final String file : netsimScripts) {
                    response = netsimOperator.transferFilesFromTargetToNetsim(folderToCreateOnNetsim, file);
                    if (null != response && !response.isEmpty()) {
                        log.info(response);
                    }
                }
            }
            netsimHasBeenUsed = true;
            allFilesTransferred = true;
            netsimOperator.deleteSimulation(simulationName);
            assertEquals("Create Simulation Successful", netsimOperator.createNewSimulation(simulationName));
            log.info("Restarting netsim.");
            final String netsimRestartCommand = "." + TRIGGER_PATH + RESTART_NETSIM;
            final String outputFromCommandLine = sshNetsimHandler.executeCommand(netsimRestartCommand);
            if (outputFromCommandLine != null && outputFromCommandLine.contains("NETSim started successfully")) {
                log.info("Netsim is restarted successfully.");
                // return successfulMessage;
            } else {
                log.error("Netsim is not restarted.");
                // return errorMessage;
            }

        }
    }

    // ////////////////////////////////////////////////////////////////////////////
    // Same DG2 local files are being used for BasebandT also. -oss126656
    public void prepareLocalDG2Files(final BsimNodeData nodeData) {
        log.info("Preparing local DG2 Files");
        final RemoteObjectHandler remoteFileHandler = BsimApiGetter.getRemoteFileHandler(BsimApiGetter.getHostMaster());
        transferLocalSiteBasicDG2Files(nodeData.getNodeType(), remoteFileHandler);
        transferLocalSiteEquipDG2Files(nodeData.getNodeType(), remoteFileHandler);
        transferLocalOssNodeProtocolDG2Files(nodeData.getNodeType(), remoteFileHandler);

        // transferLocalDG2Files(nodeData.getNodeType(), remoteFileHandler);

    }

    private void transferLocalSiteBasicDG2Files(final NodeType nodeType, final RemoteObjectHandler remoteFileHandler) {
        final String localDG2Files = "SiteBasic_DG2.xml";

        transferDG2Files(remoteFileHandler, localDG2Files);

    }

    private void transferLocalSiteEquipDG2Files(final NodeType nodeType, final RemoteObjectHandler remoteFileHandler) {
        final String localDG2Files = "SiteEquipment_DG2.xml";

        transferDG2Files(remoteFileHandler, localDG2Files);

    }

    private void transferLocalOssNodeProtocolDG2Files(final NodeType nodeType, final RemoteObjectHandler remoteFileHandler) {
        final String localDG2Files = "OssNodeProtocol_DG2.xml";

        transferDG2Files(remoteFileHandler, localDG2Files);

    }

    public void prepareLocalSpitfireFiles(final BsimNodeData nodeData) {
        log.info("Preparing local Spitfire Files");
        final RemoteObjectHandler remoteFileHandler = BsimApiGetter.getRemoteFileHandler(BsimApiGetter.getHostMaster());
        transferLocalSiteBasicSpitfireFiles(nodeData.getNodeType(), remoteFileHandler);
        transferLocalSiteEquipmentSpitfireFiles(nodeData.getNodeType(), remoteFileHandler);

    }

    private void transferLocalSiteBasicSpitfireFiles(final NodeType nodeType, final RemoteObjectHandler remoteFileHandler) {
        final String localSpitfireFiles = "SiteBasic_SPITFIRE.txt";

        transferSpitfireFiles(remoteFileHandler, localSpitfireFiles);

    }

    private void transferLocalSiteEquipmentSpitfireFiles(final NodeType nodeType, final RemoteObjectHandler remoteFileHandler) {
        final String localSpitfireFiles = "SiteEquipment_SPITFIRE.txt";

        transferSpitfireFiles(remoteFileHandler, localSpitfireFiles);

    }

    // //////////////////////////////////////////////////////////////////////////

    public void runNetsimIntegration(final BsimNodeData nodeData) throws InterruptedException, IOException {

        log.info("Run Netsim Integration");
        log.info("Test case is an End to End - Auto Integration Test case ==> Netsim will now be run");
        assertEquals("Port created in netsim successfully", netsimOperator.createPort(nodeData));
        assertEquals("Node " + nodeData.getNodeNameForNetsim() + " created in netsim successfully", netsimOperator.createNodeInNetsim(simulationName, nodeData));
        assertEquals(true, netsimOperator.startNode(simulationName, nodeData.getNodeNameForNetsim()));
        assertEquals("configureNodeForBsim command executed successfully",
                netsimOperator.executeConfigureNodeForBsimScript(simulationName, nodeData.getNodeNameForNetsim(), nodeData.getNodeType()));

        // activatePlannedConfigurationIfRequired(nodeData);
        assertEquals("Node synchronization successful", netsimOperator.executeNetsimStart(nodeData.getNodeFdn()));
        /*
         * if (nodeData.getNodeType().equals(NodeType.WCDMA) && nodeData.CriticalData.getOssNodeAndMimMapping().compareTo("O16A-U") >= 0) {
         * updateMo();
         * }
         */
        final RbsConfigLevel initialAIRbsConfigLevel = chooseInitialAutoIntegrationRbsConfigLevel(nodeData.getNodeType());

        final String initiateAIRbsConfigLevelResult = netsimOperator.executeSetRbsConfigLevelScript(simulationName, nodeData, initialAIRbsConfigLevel);
        assertTrue(initiateAIRbsConfigLevelResult.toUpperCase().contains(NETSIM_SET_RBS_CONFIG_LEVEL_OK_ACKNOWLEDGE));

        final RbsConfigLevel expectedSuccessfulRbsConfigLevel = chooseSuccessfulRbsConfigLevelFollowingIntegration(nodeData);

        assertEquals("RbsConfigLevel checked ==> Correct level reached: " + expectedSuccessfulRbsConfigLevel,
                netsimOperator.checkRbsConfigLevelIsSetToConfigLevel(nodeData, nodeData.getNodeFdn(), nodeData.getNodeType(), expectedSuccessfulRbsConfigLevel));

        if (nodeData.isGeoRequired() && nodeData.getNodeType() == NodeType.LTE) {

            final String rbsConfigLevelGpsSuccessfullyMatchedResult = netsimOperator.executeSetRbsConfigLevelScript(simulationName, nodeData,
                    LteRbsConfigLevels.GPS_SUCCESSFULLY_MATCHED);
            assertTrue(rbsConfigLevelGpsSuccessfullyMatchedResult.toUpperCase().contains(NETSIM_SET_RBS_CONFIG_LEVEL_OK_ACKNOWLEDGE));
        }

        if (nodeData.isGeoRequired() && nodeData.getNodeType() == NodeType.MICRO_WCDMA || nodeData.getNodeType() == NodeType.WCDMA
                && nodeData.CriticalData.getOssNodeAndMimMapping().compareTo("O16A-U") >= 0) {

            final String rbsConfigLevelGpsSuccessfullyMatchedResult = netsimOperator.executeSetRbsConfigLevelScript(simulationName, nodeData,
                    WcdmaRbsConfigLevels.GPS_SUCCESSFULLY_MATCHED);
            assertTrue(rbsConfigLevelGpsSuccessfullyMatchedResult.toUpperCase().contains(NETSIM_SET_RBS_CONFIG_LEVEL_OK_ACKNOWLEDGE));

        }

        checkAreCellsUnlockedIfRequired(nodeData);
        stopCreatedNodesInNetsim(nodeData);
    }

    public void runNetsimIntegrationDG2(final BsimNodeData nodeData) throws InterruptedException, IOException {

        log.info("Run Netsim Integration");
        log.info("Test case is an End to End - Auto Integration Test case ==> Netsim will now be run");
        if (nodeData.getNodeType() != NodeType.BasebandT) {
            assertEquals("Node " + nodeData.getDG2NodeNameForNetsim() + " created in netsim successfully",
                    netsimOperator.createNodeInNetsim(simulationName, nodeData));
        } else {
            assertEquals("Node " + nodeData.getNodeName() + " created in netsim successfully", netsimOperator.createNodeInNetsim(simulationName, nodeData));
        }
        updateMOinONRM(nodeData);
        if (nodeData.getNodeType() != NodeType.BasebandT) {
            assertEquals(true, netsimOperator.startNode(simulationName, nodeData.getDG2NodeNameForNetsim()));
        } else {
            assertEquals(true, netsimOperator.startNode(simulationName, nodeData.getNodeName()));
        }

        // activatePlannedConfigurationIfRequired(nodeData);
        // assertEquals("Node synchronization successful", netsimOperator.executeNetsimStart(nodeData.getNodeFdn()));
        assertEquals(true, netsimOperator.attachDG2NodeToMibAdapter(nodeData));
        assertEquals("Node synchronization successful", netsimOperator.checkNodeIsSynchronizedInSeg(nodeData.getNodeFdn()));
        final RbsConfigLevel initialAIRbsConfigLevel = chooseInitialAutoIntegrationRbsConfigLevel(nodeData.getNodeType());

        final String initiateAIRbsConfigLevelResult = netsimOperator.executeSetRbsConfigLevelScript(simulationName, nodeData, initialAIRbsConfigLevel);
        assertTrue(initiateAIRbsConfigLevelResult.toUpperCase().contains(NETSIM_SET_RBS_CONFIG_LEVEL_OK_ACKNOWLEDGE));

        final RbsConfigLevel expectedSuccessfulRbsConfigLevel = chooseSuccessfulRbsConfigLevelFollowingIntegration(nodeData);
        assertEquals("RbsConfigLevel checked ==> Correct level reached: " + expectedSuccessfulRbsConfigLevel,
                netsimOperator.checkRbsConfigLevelIsSetToConfigLevel(nodeData, nodeData.getNodeFdn(), nodeData.getNodeType(), expectedSuccessfulRbsConfigLevel));

        stopCreatedNodesInNetsim(nodeData);
    }

    /**
     * @param nodeData
     */
    private void updateMOinONRM(final BsimNodeData nodeData) {
        // TODO Auto-generated method stub (Dec 3, 2015:11:23:10 AM by xsidmeh)
        // TODO Auto-generated method stub (Sep 2, 2015:1:34:22 PM by xsonaro)
        log.info("going to update MO in ONRM");
        final String updateCommand = ONRM_CS + " " + LIST_ManagedElement + " " + "|grep -i" + " " + nodeData.getNodeName();
        final String ListManagedElement = ossMasterCLICommandHelper.simpleExec(updateCommand);
        final String ListMO = ossMasterCLICommandHelper.simpleExec(ONRM_CS + " " + LIST_MO + " " + ListManagedElement);
        final String[] listOfMos = ListMO.split("\n");
        final List<String> finallistofMos = Arrays.asList(listOfMos);
        for (final String MO : finallistofMos) {
            if (MO.contains("protocol-0")) {

                final String updatemocommand = ONRM_CS + " " + Set_Attribute + " " + MO + " " + "port" + " " + "22" + " " + "protocolTransport" + " " + "SSH";
                log.info("Executing... => " + updatemocommand);
                log.info("Output => " + ossMasterCLICommandHelper.simpleExec(updatemocommand));
                break;
            }
        }

        try {
            Thread.sleep(5000);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block (Sep 3, 2015:1:15:42 PM by xsonaro)
            log.info("plan changes could not be made because of exception" + e);
        }

    }

    /**
     * Activates the plan on an RNC if the Node Type is WCDMA or MICRO_WCDMA
     * 
     * @param nodeData
     * @throws InterruptedException
     */
    private void activatePlannedConfigurationIfRequired(final BsimNodeData nodeData) throws InterruptedException {
        if (nodeData.getNodeType() == NodeType.WCDMA || nodeData.getNodeType() == NodeType.MICRO_WCDMA) {
            assertEquals(true, netsimOperator.checkRncIsSynchronized(nodeData.getNodeType(), nodeData.CriticalData.getRncFdn()));
            assertEquals(false, netsimOperator.stopNode(simulationName, nodeData.getNodeNameForNetsim()));
            activatePlannedConfiguration(nodeData);
            assertEquals(true, netsimOperator.checkIfNodeIsNotSyncedInCs(nodeData.getNodeFdn()));

            assertEquals(true, netsimOperator.startNode(simulationName, nodeData.getNodeNameForNetsim()));
        }
    }

    /**
     * Stops the created Nodes in Netsim
     * 
     * @param nodeData
     */
    private void stopCreatedNodesInNetsim(final BsimNodeData nodeData) {
        if (nodeData.getNodeType() == NodeType.BasebandT) {
            assertEquals(false, netsimOperator.stopNode(simulationName, nodeData.getNodeName()));
        } else if (nodeData.getNodeType() == NodeType.DG2) {
            assertEquals(false, netsimOperator.stopNode(simulationName, nodeData.getDG2NodeNameForNetsim()));
        } else {
            if (nodeData.getNodeType().equals(NodeType.WCDMA) || nodeData.getNodeType().equals(NodeType.MICRO_WCDMA)) {
                assertEquals(false, netsimOperator.stopNode(simulationName, nodeData.getRncNameForNetsim()));
            } else {
                assertEquals(false, netsimOperator.stopNode(simulationName, nodeData.getNodeNameForNetsim()));
            }
        }
    }

    /**
     * Checks if cells are unlocked if cells unlocked was requested as part of the test case
     * 
     * @param nodeData
     * @throws InterruptedException
     */
    private void checkAreCellsUnlockedIfRequired(final BsimNodeData nodeData) throws InterruptedException {
        if (nodeData.getAifData().isUnlockCells()) {
            assertEquals("Cells Unlocked", netsimOperator.checkAreCellsUnlocked(nodeData));
        } else {
            log.info("Unlock cells is set to false so skipping check to see if cells unlocked.");
        }
    }

    /**
     * This method returns the initial RbsConfigLevel that is to be set on the Node to initiate Auto-Integration based on NodeType
     * 
     * @param nodeData
     * @return initiateAutoIntegrationRbsConfigLevel
     */
    private RbsConfigLevel chooseInitialAutoIntegrationRbsConfigLevel(final NodeType nodeType) {
        RbsConfigLevel initialAutoIntegrationRbsConfigLevel = null;

        switch (nodeType) {
            case LTE:
                initialAutoIntegrationRbsConfigLevel = LteRbsConfigLevels.SITE_CONFIG_COMPLETE;
            case MICRO_LTE:
                initialAutoIntegrationRbsConfigLevel = LteRbsConfigLevels.SITE_CONFIG_COMPLETE;
                break;
            case WCDMA:
                initialAutoIntegrationRbsConfigLevel = WcdmaRbsConfigLevels.SITE_CONFIG_COMPLETE;
                break;
            case MICRO_WCDMA:
                initialAutoIntegrationRbsConfigLevel = WcdmaRbsConfigLevels.SITE_CONFIG_COMPLETE;
                break;
            case DG2:
            case BasebandT:
                initialAutoIntegrationRbsConfigLevel = DG2ConfigLevel.SITE_CONFIG_COMPLETE;
                break;
            default:
                log.error("ERROR: Unknown RbsConfigLevel for Node Type " + nodeType.toString());
                break;
        }
        return initialAutoIntegrationRbsConfigLevel;
    }

    /**
     * This method returns the initial RbsConfigLevel that is to be set on the Node to initiate Auto-Integration based on NodeType
     * 
     * @param nodeData
     * @return initiateAutoIntegrationRbsConfigLevel
     */
    private RbsConfigLevel chooseSuccessfulRbsConfigLevelFollowingIntegration(final BsimNodeData nodeData) {

        RbsConfigLevel successfulRbsConfigLevel = null;

        if (nodeData.getNodeType() == NodeType.MICRO_WCDMA && !nodeData.isGeoRequired()) {
            successfulRbsConfigLevel = WcdmaRbsConfigLevels.INTEGRATION_COMPLETE;
        } else if (nodeData.getNodeType() == NodeType.MICRO_LTE && !nodeData.isGeoRequired()) {
            successfulRbsConfigLevel = LteRbsConfigLevels.INTEGRATION_COMPLETE;
        } else if (nodeData.getNodeType() == NodeType.MICRO_LTE && !nodeData.isGeoRequired()) {
            successfulRbsConfigLevel = LteRbsConfigLevels.GPS_CHECK_POSITION;
        } else if (nodeData.getNodeType() == NodeType.MICRO_WCDMA && nodeData.isGeoRequired() || nodeData.getNodeType() == NodeType.WCDMA
                && nodeData.CriticalData.getOssNodeAndMimMapping().compareTo("O16A-U") >= 0) {
            successfulRbsConfigLevel = WcdmaRbsConfigLevels.INTEGRATION_COMPLETE;
        } else if (nodeData.getNodeType() == NodeType.WCDMA && nodeData.CriticalData.getOssNodeAndMimMapping().compareTo("O16A-U") < 0) {
            successfulRbsConfigLevel = WcdmaRbsConfigLevels.OSS_CONFIGURATION_SUCCESSFUL;
        } else if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {
            successfulRbsConfigLevel = DG2ConfigLevel.READY_FOR_SERVICE;
        }
        return successfulRbsConfigLevel;
    }

    public void cleanUpNetsim() {

        if (hasNetsimUsed()) {
            log.info("Deleting Netsim Simulation");
            netsimOperator.deleteSimulation(simulationName);
        }
    }

    public void restartOriginalRNC(final String rncName) {
        log.info("Restarting default " + rncName);
        netsimOperator.startRNCinNetsim(rncName);
    }

    private void activatePlannedConfiguration(final BsimNodeData nodeData) throws InterruptedException {

        if (nodeData.getNodeType().equals(NodeType.WCDMA) || nodeData.getNodeType().equals(NodeType.MICRO_WCDMA)) {
            assertEquals("Activation Successful",
                    netsimOperator.activatePlannedConfiguration(nodeData.getPlanName(), nodeData.CriticalData.getRncFdn(), nodeData.CriticalData.getRncName()));

        }
    }

    private void checkAndCreateRNCifRequired(final BsimNodeData nodeData) throws InterruptedException {

        if (nodeData.getNodeType().equals(NodeType.WCDMA) || nodeData.getNodeType().equals(NodeType.MICRO_WCDMA)) {

            log.info("Stop RNC " + nodeData.CriticalData.getRncName() + " in Netsim");
            assertEquals(true, netsimOperator.stopRncInNetsim(nodeData.CriticalData.getRncName(), nodeData.CriticalData.getRncFdn()));
            log.info("Create RNC Port");
            assertEquals("Port created in netsim successfully", netsimOperator.createPort("TAF_RNC_PORT", nodeData.CriticalData.getRncIpAddress()));

            log.info("Create RNC in Netsim ");
            assertEquals("RNC created in netsim successfully", netsimOperator.createRNCInNetsim(simulationName, rncPortName, nodeData.getRncNameForNetsim()));
            assertEquals(true, netsimOperator.startNode(simulationName, nodeData.getRncNameForNetsim()));
            log.info("Configure RNC node for BSIM");

            assertEquals("configureNodeForBsim command executed successfully",
                    netsimOperator.executeConfigureNodeForBsimScript(simulationName, nodeData.getRncNameForNetsim(), nodeData.getNodeType()));
            Thread.sleep(10000);
            assertEquals(false, netsimOperator.stopNode(simulationName, nodeData.getRncNameForNetsim()));
            assertEquals(true, netsimOperator.startNode(simulationName, nodeData.getRncNameForNetsim()));
        } else {
            log.info("RNC Not required ==> Test case is not a WCDMA Macro or WCDMA Micro Node");
        }
    }

    /**
     * Sends the request to BSIM Server to manual bind a Macro LTE node.
     * Asserts if the bind is successful.
     * 
     * @author emakaln
     * @param nodeData
     */
    public void bindMacroLteNode(final BsimNodeData nodeData) {
        if (nodeData.getAifData().isWithoutLaptop() && nodeData.getNodeType() != NodeType.WCDMA) {
            log.info("Binding LTE Macro Without Laptop node via calling BSIM Service...");
            final BsimOperator bsimOperator = new BsimOperator();
            final String bindResult = bsimOperator.bindLTEMacroNode(nodeData);
            log.info("LTE Macro Without Laptop Bind Result ====> " + bindResult);
            Assert.assertTrue("Successful".equalsIgnoreCase(bindResult));
        }
    }

    /**
     * Sends the request to BSIM Server to manual bind a Macro LTE node.
     * Asserts if the bind is successful.
     * 
     * @author xmurman
     * @param nodeData
     */
    public void bindMicroWcdmaNode(final BsimNodeData nodeData) {
        if (nodeData.getAifData().isWithoutLaptop()) {
            log.info("Binding WCDMA Micro Without Laptop node via calling BSIM Service...");
            final BsimOperator bsimOperator = new BsimOperator();
            final String bindResult = bsimOperator.bindWCDMAMicroNode(nodeData);
            log.info("WCDMA Micro Without Laptop Bind Result ====> " + bindResult);
            Assert.assertTrue("Successful".equalsIgnoreCase(bindResult));
        }
    }

    /**
     * Sends the request to BSIM Server to manual bind a Macro WCDMA node.
     * Asserts if the bind is successful.
     * 
     * @author xgaunag
     * @param nodeData
     */
    public void bindMacroWcdmaNode(final BsimNodeData nodeData) {
        if (nodeData.getNodeType() == NodeType.WCDMA && nodeData.CriticalData.getOssNodeAndMimMapping().compareTo("O16A-U") > 0) {
            log.info("Binding WCDMA Macro Without Laptop node via calling BSIM Service...");
            final BsimOperator bsimOperator = new BsimOperator();
            final String bindResult = bsimOperator.bindWCDMAMacroNode(nodeData);
            log.info("WCDMA Macro Without Laptop Bind Result ====> " + bindResult);
            Assert.assertTrue("Successful".equalsIgnoreCase(bindResult));
        }
    }

    /**
     * Sends the request to BSIM Server to manual bind a RadioNode.
     * Asserts if the bind is successful.
     * 
     * @author xmurman
     * @param nodeData
     */
    public void bindRadioNode(final BsimNodeData nodeData) {
        if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {
            log.info("Binding RadioNode via calling BSIM Service...");
            final BsimOperator bsimOperator = new BsimOperator();
            final String bindResult = bsimOperator.bindRadioNode(nodeData);
            log.info("Radio Node Bind Result ====> " + bindResult);
            Assert.assertTrue("Successful".equalsIgnoreCase(bindResult));
            setTestStep("Verify Site Installation created");
            if (!bsimOperator.checkSiteInstallationFileExists()) {
                log.info("Manual Bind Failed -->> View Osgi Logs for more details");
            }
            Assert.assertTrue(bsimOperator.checkSiteInstallationFileExists());
            setTestStep("Verify MO Exists");
            Assert.assertTrue(bsimOperator.checkMoExists(nodeData));

        }
    }

    /**
     * Sends the request to BSIM Server to manual bind a SpitfireNode.
     * Asserts if the bind is successful.
     * 
     * @author xmurman
     * @param nodeData
     */
    public void bindSpitFireNode(final BsimNodeData nodeData) {
        if (nodeData.getNodeType() == NodeType.Router_6672) {
            log.info("Binding SpitfireNode via calling BSIM Service...");
            final BsimOperator bsimOperator = new BsimOperator();
            final String bindResult = bsimOperator.bindSpitFireNode(nodeData);
            log.info("Spitfire Node Bind Result ====> " + bindResult);
            Assert.assertTrue("Successful".equalsIgnoreCase(bindResult));
            setTestStep("Verify Site Installation created");
            if (!bsimOperator.checkSiteInstallationFileExists()) {
                log.info("Manual Bind Failed -->> View Osgi Logs for more details");
            }
            Assert.assertTrue(bsimOperator.checkSiteInstallationFileExists());
            setTestStep("Verify MO Exists");
            Assert.assertTrue(bsimOperator.checkMoExists(nodeData));

        }
    }

    /**
     * Transfer Netsim simulations from the resources folder based on Node Type
     * Simulations are stored under the netsimSimulations folder in the src/main/resources folder *
     * 
     * @param nodeType
     * @param netsimFileHandler
     */

    public void executeNoHardwareBindDG2(final BsimNodeData nodeData) {

        log.info("No HW Binding of " + nodeData.getNodeType() + " node via calling BSIM Service...");
        final BsimOperator bsimOperator = new BsimOperator();
        final String bindResult = bsimOperator.noHWbindDG2Node(nodeData);
        log.info("No HW Bind of " + nodeData.getNodeType() + " Result ====> " + bindResult);
        Assert.assertTrue("Successful".equalsIgnoreCase(bindResult));
        setTestStep("Verify Site Installation created");
        if (!bsimOperator.checkSiteInstallationFileExists()) {
            log.info("No Hardware Bind Failed -->> View Osgi Logs for more details");
        }
        Assert.assertTrue(bsimOperator.checkSiteInstallationFileExists());
        setTestStep("Verify MO Exists");
        Assert.assertTrue(bsimOperator.checkMoExists(nodeData));

    }

    private void transferAndImportSimulationsIfRequired(final NodeType nodeType, final RemoteObjectHandler netsimFileHandler) {
        String regexForSimulationNameToTransfer = "N/A";
        if (nodeType == NodeType.MICRO_WCDMA) {
            regexForSimulationNameToTransfer = "MICRO_RBS_SIM.zip";
            transferAndImportSimulation(netsimFileHandler, regexForSimulationNameToTransfer);
        } else {
            log.info("Simulation transfer and import not required for Node Type: " + nodeType.toString());
        }
    }

    /**
     * @param netsimFileHandler
     * @param simulationToTransfer
     */
    private void transferAndImportSimulation(final RemoteObjectHandler netsimFileHandler, final String regexForSimulationNameToTransfer) {
        final String remoteFolderForSims = "/netsim/netsimdir/";
        final List<String> netsimSimulations = FileFinder.findFile(regexForSimulationNameToTransfer, "/target/");

        for (final String simulationLocation : netsimSimulations) {
            log.info("Netsim Simulations Location: " + simulationLocation);
        }
        assertEquals(true, remoteFilesHandler.transferLocalFilesToRemote(netsimFileHandler, remoteFolderForSims, netsimSimulations));
        assertEquals(true, netsimOperator.importSimulations(netsimSimulations));
    }

    /*
     * private void transferLocalDG2Files(final NodeType nodeType, final RemoteFileHandler remoteFileHandler) {
     * final String localDG2Files = "DG2.xml";
     * transferDG2Files(remoteFileHandler, localDG2Files);
     * }
     */

    private void transferDG2Files(final RemoteObjectHandler remoteFileHandler, final String localDG2Files) {
        final String remoteFolderForLocalFiles = "/tmp/";
        final List<String> localFilesDG2 = FileFinder.findFile(localDG2Files);

        for (final String localFile : localFilesDG2) {
            log.info("DG2 Local Files " + localFile);
        }
        assertEquals(true, remoteFilesHandler.transferLocalFilesToRemote(remoteFileHandler, remoteFolderForLocalFiles, localFilesDG2));

    }

    private void transferSpitfireFiles(final RemoteObjectHandler remoteFileHandler, final String localSpitfireFiles) {
        final String remoteFolderForLocalFiles = "/tmp/";
        final List<String> localFilesSpitfire = FileFinder.findFile(localSpitfireFiles);

        for (final String localFile : localFilesSpitfire) {
            log.info("Spitfire Local Files " + localFile);
        }
        assertEquals(true, remoteFilesHandler.transferLocalFilesToRemote(remoteFileHandler, remoteFolderForLocalFiles, localFilesSpitfire));

    }

    public boolean hasNetsimUsed() {

        return netsimHasBeenUsed;
    }

    public String executeAddNodeCommandOnBsimServer(final String nodesToAdd) {

        final BsimOperator bsimOperator = new BsimOperator();
        return bsimOperator.executeAddNodeCommandOnBsimServer(nodesToAdd);

    }
}

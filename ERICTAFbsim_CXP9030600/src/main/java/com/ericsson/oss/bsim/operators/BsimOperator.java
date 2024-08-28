package com.ericsson.oss.bsim.operators;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.GenericOperator;
import com.ericsson.oss.bsim.batch.data.model.MockBsimBatch;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.netsim.domain.RbsConfigLevel;
import com.ericsson.oss.bsim.operators.api.BsimAddNodeApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimBindApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimDeleteNodeApiOperator;
import com.ericsson.oss.bsim.operators.api.MacroNetsimApiOperator;
import com.ericsson.oss.bsim.operators.api.NetsimApiOperator;
import com.ericsson.oss.bsim.operators.api.PicoNetsimApiOperator;

public class BsimOperator implements GenericOperator {

    private final Logger log = Logger.getLogger(BsimAddNodeApiOperator.class);

    final BsimAddNodeApiOperator bsimApiOperator = new BsimAddNodeApiOperator();

    final BsimDeleteNodeApiOperator deleteNodeOperator = new BsimDeleteNodeApiOperator();

    final BsimBindOperator bsimBindOperator = new BsimBindApiOperator();

    final NetsimApiOperator netsimApiOperator = new NetsimApiOperator();

    private final PicoNetsimApiOperator picoNetsimApiOperator = new PicoNetsimApiOperator();

    private final MacroNetsimApiOperator macroNetsimApiOperator = new MacroNetsimApiOperator();

    public boolean addNode(final BsimNodeData nodeData) {

        return bsimApiOperator.addNode(nodeData);
    }

    public String executeAddNodeCommandOnBsimServer(final String nodesToAdd) {

        return bsimApiOperator.executeAddNodeCommandOnBsimServer(nodesToAdd);
    }

    public void executeAddNodeCommandOnBsimServer() {

        bsimApiOperator.executeAddNodeCommandOnBsimServer();
    }

    public String getAddNodeStatus() {

        return bsimApiOperator.getAddNodeStatus();
    }

    public void deleteNode(final BsimNodeData nodeData) {

        try {
            if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {
                // delete the plan configuration
                deleteNodeOperator.deletePCA(nodeData.getPlanName());
            }

            if (checkNodeByCmd(nodeData.getNodeName(), true)) {
                // // delete node (with related export file and site if any)
                if (!nodeData.isEndToEnd()) {
                    log.info("node fdn is : " + nodeData.getNodeFdn());
                    deleteNodeOperator.deleteCreatedNode(nodeData.getNodeType(), nodeData.getNodeFdn());
                } else {
                    deleteNodeOperator.deleteE2ENodeUsingARNE(nodeData, nodeData.getNodeFdn(), nodeData.getNodeName(), nodeData.CriticalData.getSite());
                }
            }

            // if TN or RN enabled
            if (nodeData.isImportTransportConfiguration() || nodeData.isImportRadioConfiguration()) {
                deleteNodeOperator.deleteBCGFiles(nodeData.getNodeName());
            }

            // if AI enabled
            if (nodeData.getAifData().isAutoIntegrate()) {
                deleteNodeOperator.deleteAutoIntegrateFiles(nodeData.getNodeName());
            }
        } catch (final Exception ex) {
            log.warn("Error occurs when deleting node. Error: " + ex.getMessage());
        }

    }

    public boolean deleteSMRSAccount(final String smrsAccountName) {

        return bsimApiOperator.deleteSMRSAccount(smrsAccountName);
    }

    public void doCleanUp(final ArrayList<BsimNodeData> addedNodes) {

        for (final BsimNodeData nodeData : addedNodes) {

            log.info("****** Deleting node " + nodeData.getNodeName() + " ******");
            deleteNode(nodeData);
            if (nodeData.getNodeType() == NodeType.LTE && nodeData.getAifData().isWithoutLaptop()) {
                final String smrsAccount = nodeData.getNodeName() + "_LRAN";
                deleteSMRSAccount(smrsAccount);
            }
        }
    }

    // this check does not work properly
    public String moExist(final String nodeFdn) {

        return bsimApiOperator.moExist(nodeFdn);
    }

    public boolean checkNodeByCmd(final String nodeName, final boolean isFinalExecutionOfcheckNodeByCmdMethod) {

        return bsimApiOperator.checkNodeByCmd(nodeName, isFinalExecutionOfcheckNodeByCmdMethod);
    }

    public boolean checkSCValuesByCmd(final String nodeName, final String planName, final String rncName) {

        return bsimApiOperator.checkSCValuesByCmd(nodeName, planName, rncName);
    }

    public boolean checkCreatedSite(final String siteName) {

        return bsimApiOperator.checkSiteByCmd(siteName);
    }

    public boolean checkPlan(final String planName) {

        return bsimApiOperator.checkPlanByCmd(planName);
    }

    public Boolean checkDoesSmrsAccountExistforNode(final BsimNodeData nodeData, final Boolean expectedResult) {
        return bsimApiOperator.checkDoesSmrsAccountExistForNode(nodeData, expectedResult);
    }

    public boolean checkNodeInPlan(final String nodeFdn, final String planName) {

        return bsimApiOperator.checkPlanByCmd(planName);
    }

    public boolean checkBcgFiles(final BsimNodeData nodeData) {

        boolean checkResult = true;
        if (nodeData.isImportTransportConfiguration()) {
            checkResult &= bsimApiOperator.checkTransportFile(nodeData.getNodeName());
        }

        if (nodeData.isImportRadioConfiguration()) {
            checkResult &= bsimApiOperator.checkRadioFile(nodeData.getNodeName());
        }

        return checkResult;
    }

    public boolean checkAITemplates(final BsimNodeData nodeData) {

        boolean aiCheckResult = true;
        final String rncName = nodeData.CriticalData.getRncName();
        aiCheckResult &= bsimApiOperator.checkSiteBasicFile(nodeData.getNodeType(), nodeData.getNodeName(), rncName, nodeData.isIPv6());
        aiCheckResult &= bsimApiOperator.checkSiteEquipmentFile(nodeData.getNodeType(), nodeData.getNodeName(), rncName, nodeData.isIPv6());
        aiCheckResult &= bsimApiOperator.checkAutoIntegrationSummaryFile(nodeData.getNodeType(), nodeData.getNodeName(), rncName, nodeData.isIPv6());
        if (nodeData.getNodeType().equals(NodeType.LTE) && !nodeData.getAifData().isWithoutLaptop() || nodeData.getNodeType().equals(NodeType.MICRO_LTE)
                && !nodeData.getAifData().isWithoutLaptop()) {
            aiCheckResult &= bsimApiOperator.checkSiteInstallationFile(nodeData.getNodeType(), nodeData.getNodeName(), rncName, nodeData.isIPv6());
        }
        if (nodeData.getNodeType().equals(NodeType.MICRO_WCDMA)) {
            aiCheckResult &= bsimApiOperator.checkSiteInstallationFile(nodeData.getNodeType(), nodeData.getNodeName(), rncName, nodeData.isIPv6());
            aiCheckResult &= bsimApiOperator.checkCabinetEquipmentFilesExist(nodeData.getNodeType(), nodeData.getNodeName(), rncName, nodeData.isIPv6());
        }

        return aiCheckResult;
    }

    public boolean checkDG2AIFiles(final BsimNodeData nodeData) {

        boolean aiCheckResult = true;

        final String rncName = nodeData.CriticalData.getRncName();
        final String dg2Domain = nodeData.getDg2Domain().toString();
        final String siteBasicFile = nodeData.getAifData().getSiteBasicFileName();
        final String siteEquipmentFile = nodeData.getAifData().getSiteEquipmentFileName();
        
        final File basicFile = new File(siteBasicFile);
        final String basicFileName = basicFile.getName();

        final File equipmentFile = new File(siteEquipmentFile);
        final String equipmentFileName = equipmentFile.getName();

        aiCheckResult &= bsimApiOperator.checkSiteBasicFileDG2(nodeData.getNodeType(), nodeData.getNodeName(), rncName, dg2Domain, basicFileName);
        aiCheckResult &= bsimApiOperator.checkSiteEquipmentFileDG2(nodeData.getNodeType(), nodeData.getNodeName(), rncName, dg2Domain, equipmentFileName);
        
	if(nodeData.getAifData().isUsingOssNodeProtocolFile()){

		final String ossNodeProtocolFile = "OssNodeProtocolFile.xml";
		final File ossProtocolFile = new File(ossNodeProtocolFile);
	        final String ossProtocolFileName = ossProtocolFile.getName();	
	 	aiCheckResult &= bsimApiOperator.checkOssNodeProtocolFileDG2(nodeData.getNodeType(), nodeData.getNodeName(), rncName, dg2Domain,
	        ossProtocolFileName);
	}
        aiCheckResult &= bsimApiOperator.checkAutoIntegrationSummaryFileDG2(nodeData.getNodeType(), nodeData.getNodeName(), rncName, dg2Domain);

        return aiCheckResult;
    }

    public boolean checkExportFile(final NodeType nodeType, final String nodeName, final String rncName) {

        return bsimApiOperator.checkExportFile(nodeType, nodeName, rncName);
    }

    public boolean checkSpitfireAIFiles(final BsimNodeData nodeData) {

        boolean aiCheckResult = true;
        final String spitfireDomain = nodeData.getSpitfireDomain().toString();
        final String siteBasicFile = nodeData.getAifData().getSiteBasicFileName();
        final String ossNodeProtocolFile = "OssNodeProtocolFile.xml";
        final File basicFile = new File(siteBasicFile);
        final String basicFileName = basicFile.getName();
        aiCheckResult &= bsimApiOperator.checkSiteBasicFileSpitfire(nodeData.getNodeType(), nodeData.getNodeName(), spitfireDomain, basicFileName);
        aiCheckResult &= bsimApiOperator.checkOssNodeProtocolFileSpitfire(nodeData.getNodeType(), nodeData.getNodeName(), spitfireDomain, ossNodeProtocolFile);
        aiCheckResult &= bsimApiOperator.checkAutoIntegrationSummaryFileSpitfire(nodeData.getNodeType(), nodeData.getNodeName(), spitfireDomain);

        return aiCheckResult;

    }

    public boolean checkExportFileDG2(final NodeType nodeType, final String nodeName, final String rncName) {

        return bsimApiOperator.checkExportFileDG2(nodeType, nodeName, rncName);
    }

    public boolean checkExportFileSpitfire(final NodeType nodeType, final String nodeName) {
        return bsimApiOperator.checkExportFileSpitfire(nodeType, nodeName);
    }

    public boolean checkQRCodeExists(final String qrCodeLocation, final String qrCodeName) {

        return bsimApiOperator.checkQRCodeExists(qrCodeLocation, qrCodeName);
    }

    public boolean checkNetsimIsRunning() {

        return netsimApiOperator.checkNetsimIsRunning();
    }

    public boolean startNode(final String simulationName, final String nodeName) {

        return netsimApiOperator.startNode(simulationName, nodeName);
    }

    public boolean stopNode(final String simulationName, final String nodeName) {

        return netsimApiOperator.stopNode(simulationName, nodeName);
    }

    public String createNewSimulation(final String simulationName) {

        return netsimApiOperator.createNewSimulation(simulationName);
    }

    public String deleteSimulation(final String simulationName) {

        return netsimApiOperator.deleteSimulation(simulationName);
    }

    public void deleteFileFromRemoteHost(final String simulationZipPath) {

        netsimApiOperator.deleteFileFromRemoteHost(simulationZipPath);
    }

    /**
     * Create Port for Macro Node Integration in Netsim
     * 
     * @param String
     *        portName
     * @return Output from the Netsim Shell as a String
     */
    public String createPort(final String portName, final String ipAddress) {

        return macroNetsimApiOperator.createMacroPort(portName, ipAddress);
    }

    public String createPort(final BsimNodeData nodeData) {

        return macroNetsimApiOperator.createMacroPort(nodeData);
    }

    /**
     * Create a Macro Node in Netsim
     * 
     * @param simulationName
     * @param nodeData
     * @return
     */
    public String createNodeInNetsim(final String simulationName, final BsimNodeData nodeData) {

        return macroNetsimApiOperator.createNodeInNetsim(simulationName, nodeData);
    }

    /**
     * Set Rbs Config Level to required level to initiate auto-integration or any other level required.
     * The required ConfigLevel enum value must implement the RbsConfigLevel enum class
     * 
     * @param simulationName
     *        - String
     * @param nodeName
     *        - String
     * @return Output from the command line as a String
     * @throws InterruptedException
     */
    public String executeSetRbsConfigLevelScript(final String simulationName, final BsimNodeData nodeData, final RbsConfigLevel rbsConfigLevel)
            throws InterruptedException, IOException {

        return netsimApiOperator.executeSetRbsConfigLevelScript(simulationName, nodeData, rbsConfigLevel);
    }

    /**
     * Will loop and wait to see if Config Level reaches the specified RbsConfigLevel. Will check max 20 times with 3 second interval
     * between each attempt.
     * If we fail to set in this time, timing requirements would be incorrect and it would indicate we need to check server dependencies
     * timings/code for issues
     * 
     * @param fullNodeName
     *        i.e. the FDN of the node
     * @param nodeType
     *        the nodeType as RbsConfigLevel may site under different MO's for different nodeTypes
     * @param rbsConfigLevel
     *        The desired RbsConfigLevel to be reached
     * @return
     * @throws InterruptedException
     */
    public String checkRbsConfigLevelIsSetToConfigLevel(
            final BsimNodeData nodeData,
            final String fullNodeName,
            final NodeType nodeType,
            final RbsConfigLevel rbsConfigLevel) throws InterruptedException {
        return netsimApiOperator.checkRbsConfigLevelIsSetToConfigLevel(nodeData, fullNodeName, nodeType, rbsConfigLevel);
    }

    public String executeConfigureNodeForBsimScript(final String simulationName, final String nodeName, final NodeType nodeType) {

        return netsimApiOperator.executeConfigureNodeForBsimScript(simulationName, nodeName, nodeType);

    }

    public String transferFilesFromTargetToNetsim(final String foldertoCreate, final String locationOfFilesToTransfer) {

        return netsimApiOperator.transferFilesFromTargetToNetsim(foldertoCreate, locationOfFilesToTransfer);
    }

    public void adjustNodeInSeg(final String fullNodeName) throws InterruptedException {

        netsimApiOperator.adjustNodeInSeg(fullNodeName);
    }

    public String executeNetsimStart(final String fullNodeName) throws InterruptedException {

        return netsimApiOperator.executeNetsimStart(fullNodeName);
    }

    public String checkAreCellsUnlocked(final BsimNodeData nodeData) throws InterruptedException {

        return netsimApiOperator.checkAreCellsUnlocked(nodeData);
    }

    public String createRNCInNetsim(final String simulationName, final String rncPortName, final String rncName) {

        return picoNetsimApiOperator.createRNCInNetsim(simulationName, rncPortName, rncName);

    }

    public String activatePlannedConfiguration(final String planName, final String rncFdn, final String rncName) throws InterruptedException {

        return netsimApiOperator.activatePlannedConfiguration(planName, rncFdn, rncName);
    }

    public boolean checkSimulationExists(final String simulationName) {

        return netsimApiOperator.checkSimulationExists(simulationName);
    }

    public boolean stopRncInNetsim(final String rncName, final String rncFdn) {

        return picoNetsimApiOperator.stopRncInNetsim(netsimApiOperator, rncName, rncFdn);
    }

    /**
     * Start an RNC in Netsim. This method searches through the Netsim default simulations and starts the RNC in Netsim
     * 
     * @param rncName
     * @return
     */
    public boolean startRNCinNetsim(final String rncName) {

        return picoNetsimApiOperator.startRNCinNetsim(netsimApiOperator, rncName);
    }

    public void openSimulation(final String simulation) {

        netsimApiOperator.openSimulation(simulation);

    }

    public void checkAndStopIpAddressIfStartedInNetsim(final String ipAddress) {

        try {
            netsimApiOperator.checkAndStopIpAddressIfStartedInNetsim(ipAddress);
        } catch (final IOException e) {
            log.info("Exception " + e.getMessage() + " whicle checking available Ip Address' in Netsim");
            e.printStackTrace();
        }

    }

    /**
     * Check ISCF File exists
     * 
     * @param nodeData
     * @return boolean (true if ISCF file exists)
     * @author efitrob
     */
    public boolean checkIscfFile(final BsimNodeData nodeData) {

        boolean checkResult = false;

        checkResult = bsimApiOperator.checkIscfFile(nodeData.getNodeName(), nodeData.isIPv6());

        return checkResult;
    }

    // OSS-93001 CMPv2 TAF: Modify Micro/Macro TAF TCs to include Security
    public boolean checkIscfFileWCDMA(final BsimNodeData nodeData) {

        boolean checkResult = false;

        checkResult = bsimApiOperator.checkIscfFileWCDMA(nodeData.getNodeName(), nodeData.isIPv6());

        return checkResult;
    }

    public boolean checkNetworkConfiguration(final NodeType nodeType, final String nodeName, final String rncName, final String networkConfiguration) {

        return bsimApiOperator.checkNetworkConfiguration(nodeType, nodeName, rncName, networkConfiguration);
    }

    public String bindLTEMacroNode(final BsimNodeData nodeData) {
        int count = 0;
        final int maximumCount = 15;
        boolean isFinalExecutionOfcheckNodeByCmdMethod = false;
        while (count < maximumCount) {
            if (count == maximumCount - 1) {
                isFinalExecutionOfcheckNodeByCmdMethod = true;
            }
            log.info("Checking if node " + nodeData.getNodeName() + " has been added successfully before attempting Macro LTE bind. Count ==> " + count);
            if (bsimApiOperator.checkNodeByCmd(nodeData.getNodeName(), isFinalExecutionOfcheckNodeByCmdMethod)) {
                try {
                    log.info("Waiting 20 seconds before commencing LTE Macro bind...");
                    Thread.sleep(20000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("Starting Without Laptop LTE Macro Bind for node " + nodeData.getNodeName());
                return bsimBindOperator.bindNode(nodeData.getNodeName(), generateSerialNumber());
            }

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        log.error("LTE Macro Node " + nodeData.getNodeName() + " failed to bind within 1 minute");
        return "";
    }

    public String bindWCDMAMicroNode(final BsimNodeData nodeData) {

        int count = 0;
        final int maximumCount = 15;
        boolean isFinalExecutionOfcheckNodeByCmdMethod = false;
        while (count < maximumCount) {
            if (count == maximumCount - 1) {
                isFinalExecutionOfcheckNodeByCmdMethod = true;
            }
            log.info("Checking if node " + nodeData.getNodeName() + " has been added successfully before attempting Micro WCDMA bind. Count ==> " + count);
            if (bsimApiOperator.checkNodeByCmd(nodeData.getNodeName(), isFinalExecutionOfcheckNodeByCmdMethod)) {
                try {
                    log.info("Waiting 20 seconds before commencing WCDMA Micro bind...");
                    Thread.sleep(20000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("Starting Without Laptop WCDMA Micro Bind for node " + nodeData.getNodeName());
                return bsimBindOperator.bindMicroWCDMANode(nodeData.getNodeName(), generateSerialNumber());
            }

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        log.error("WCDMA Micro Node " + nodeData.getNodeName() + " failed to bind within 1 minute");
        return "";
    }

    public String bindWCDMAMacroNode(final BsimNodeData nodeData) {

        int count = 0;
        final int maximumCount = 15;
        boolean isFinalExecutionOfcheckNodeByCmdMethod = false;
        while (count < maximumCount) {
            if (count == maximumCount - 1) {
                isFinalExecutionOfcheckNodeByCmdMethod = true;
            }
            log.info("Checking if node " + nodeData.getNodeName() + " has been added successfully before attempting Macro WCDMA bind. Count ==> " + count);
            if (bsimApiOperator.checkNodeByCmd(nodeData.getNodeName(), isFinalExecutionOfcheckNodeByCmdMethod)) {
                try {
                    log.info("Waiting 20 seconds before commencing WCDMA Macro bind...");
                    Thread.sleep(20000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("Starting Without Laptop WCDMA Macro Bind for node " + nodeData.getNodeName());
                return bsimBindOperator.bindMacroWCDMANode(nodeData.getNodeName(), generateSerialNumber());
            }

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        log.error("WCDMA Macro Node " + nodeData.getNodeName() + " failed to bind within 1 minute");
        return "";
    }

    // //////////////////////////
    public String noHWbindDG2Node(final BsimNodeData nodeData) {
        int count = 0;
        final int maximumCount = 15;
        boolean isFinalExecutionOfcheckNodeByCmdMethod = false;
        while (count < maximumCount) {
            if (count == maximumCount - 1) {
                isFinalExecutionOfcheckNodeByCmdMethod = true;
            }
            log.info("Checking if node " + nodeData.getNodeName() + " has been added successfully before attempting No HW bind. Count ==> " + count);
            if (bsimApiOperator.checkNodeByCmd(nodeData.getNodeName(), isFinalExecutionOfcheckNodeByCmdMethod)) {
                try {
                    log.info("Waiting 20 seconds before commencing No HW Bind of DG2 ...");
                    Thread.sleep(20000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("Starting No HW Bind of DG2 for node " + nodeData.getNodeName());
                return bsimBindOperator.noHWbindDG2Node(nodeData.getNodeName(), "/home/nmsadm/TAF_SiteInstallation.xml", nodeData.getAifData()
                        .getSiteInstallTemplateName(), "12.36.25.47");
            }

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        log.error("DG2 Node " + nodeData.getNodeName() + " failed to bind within 1 minute");
        return "";
    }

    // /////////////////////////

    public String bindRadioNode(final BsimNodeData nodeData) {

        int count = 0;
        final int maximumCount = 15;
        boolean isFinalExecutionOfcheckNodeByCmdMethod = false;
        while (count < maximumCount) {
            if (count == maximumCount - 1) {
                isFinalExecutionOfcheckNodeByCmdMethod = true;
            }
            log.info("Checking if node " + nodeData.getNodeName() + " has been added successfully before attempting manual bind of RadioNode. Count ==> "
                    + count);
            if (bsimApiOperator.checkNodeByCmd(nodeData.getNodeName(), isFinalExecutionOfcheckNodeByCmdMethod)) {
                try {
                    log.info("Waiting 20 seconds before commencing RadioNode bind...");
                    Thread.sleep(20000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("Starting Manual Bind for node " + nodeData.getNodeName());
                return bsimBindOperator.bindRadioNode(nodeData.getNodeName(), "/home/nmsadm/TAF_SiteInstallation.xml", nodeData.getAifData()
                        .getSiteInstallTemplateName(), "1.1.1.1", generateSerialNumber());
            }

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        log.error("RadioNode " + nodeData.getNodeName() + " failed to bind within 1 minute");
        return "";
    }

    public String bindSpitFireNode(final BsimNodeData nodeData) {

        int count = 0;
        final int maximumCount = 15;
        boolean isFinalExecutionOfcheckNodeByCmdMethod = false;
        while (count < maximumCount) {
            if (count == maximumCount - 1) {
                isFinalExecutionOfcheckNodeByCmdMethod = true;
            }
            log.info("Checking if node " + nodeData.getNodeName() + " has been added successfully before attempting manual bind of SpitFire. Count ==> "
                    + count);
            if (bsimApiOperator.checkNodeByCmd(nodeData.getNodeName(), isFinalExecutionOfcheckNodeByCmdMethod)) {
                try {
                    log.info("Waiting 20 seconds before commencing SpitFireNode bind...");
                    Thread.sleep(20000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
                log.info("Starting Manual Bind for node " + nodeData.getNodeName());
                if (!nodeData.getisSaveTo()) {
                    return bsimBindOperator.bindSpiteFireNode(nodeData.getNodeName(), null, nodeData.getAifData().getSiteInstallTemplateName(), "1.1.1.1",
                            generateSerialNumber());
                } else {
                    return bsimBindOperator.bindSpiteFireNode(nodeData.getNodeName(), "/home/nmsadm/TAF_SiteInstallation.xml", nodeData.getAifData()
                            .getSiteInstallTemplateName(), "1.1.1.1", generateSerialNumber());
                }
            }

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
            count++;
        }
        log.error("SpitFire " + nodeData.getNodeName() + " failed to bind within 1 minute");
        return "";
    }

    public static String generateSerialNumber() {
        final Random r = new Random();

        final char x = (char) (r.nextInt(26) + 'A');
        String random = String.valueOf(x);

        for (int i = 0; i < 9; i++) {
            random += r.nextInt(10);
        }
        return random;
    }

    /**
     * Check a Node is synchronized in the Seg_masterService_CS
     * 
     * @param nodeFdn
     * @return
     * @throws InterruptedException
     */
    public String checkNodeIsSynchronizedInSeg(final String nodeFdn) throws InterruptedException {
        return netsimApiOperator.checkNodeIsSynchronizedInSeg(nodeFdn);
    }

    /**
     * Checks if Node is not synced in the CS
     * 
     * @param nodeFdn
     * @return true if the node is NOT synced
     * @throws InterruptedException
     */
    public boolean checkIfNodeIsNotSyncedInCs(final String nodeFdn) throws InterruptedException {
        return netsimApiOperator.checkIfNodeIsNotSyncedInCs(nodeFdn);

    }

    /**
     * Checks if RNC is synced in the CS
     * 
     * @param rncFdn
     * @return true if the RNC is synced
     * @throws InterruptedException
     */
    public boolean checkRncIsSynchronized(final NodeType nodeType, final String rncFdn) throws InterruptedException {
        return netsimApiOperator.checkRncIsSynchronized(nodeType, rncFdn);

    }

    /**
     * Transfers security configuration files to Omsas.
     * 
     * @return
     */

    public String transferSecurityConfigurationFilesToOmsas() {

        return picoNetsimApiOperator.transferSecurityConfigurationFilesToOmsas();
    }

    public String checkPicoWRANNodeSynced(final MockBsimBatch picoBatch) throws InterruptedException {

        return picoNetsimApiOperator.checkPicoNodeIsSyncedInCS(picoBatch);

    }

    /**
     * Import simulations from the
     * /netsim/netsimdir folder into Netsim.
     * Note Simulations can only be imported from this folder.
     * The purpose of this is to allow the import of pre-existing simulations
     * with Mims that are not provided by the catalogued servers
     * Note: The simulation will be cleaned up directly after it has been imported.
     * The Mims contained within the imported sim will still be available after this. *
     * 
     * @param simulations
     * @return true if all simulations are imported successfully
     */
    public boolean importSimulations(final List<String> simulations) {
        return netsimApiOperator.importSimulations(simulations);

    }

    public boolean importSimulations(final String simulations) {
        return netsimApiOperator.importSimulations(simulations);

    }

    public boolean checkSiteInstallationFileExists() {
        return bsimApiOperator.checkSiteInstallationFileExists();
    }

    public boolean checkMoExists(final BsimNodeData nodeData) {

        return bsimApiOperator.checkMoExists(nodeData);
    }

    /**
     * @param nodeData
     * @return
     * @throws InterruptedException
     */

    public boolean attachDG2NodeToMibAdapter(final BsimNodeData nodeData) throws InterruptedException {
        // TODO Auto-generated method stub (Feb 8, 2016:10:38:04 AM by xsidmeh)
        return netsimApiOperator.attachDG2NodeToMibAdapter(nodeData);
    }

}

package com.ericsson.oss.bsim.operators.api;

import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.ApiOperator;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.osgi.client.ApiClient;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.DG2Domain;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.data.model.SpitfireDomain;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.taf.hostconfigurator.OssHost;

/**
 * The <code>BsimAddNodeApiOperator</code> class represents BSIM operations.
 * This class contains the implementation of the operations of add node, verify
 * nodes and etc.
 * 
 * @author exuuguu
 */

public class BsimAddNodeApiOperator implements ApiOperator {

    private final Logger log = Logger.getLogger(BsimAddNodeApiOperator.class);

    private ApiClient client = ClientHelper.getClient();

    final CLICommandHelper executor = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    final CLICommandHelper infraServerCli = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostInfraServer());

    final RemoteObjectHandler fileHandler = BsimApiGetter.getMasterHostFileHandler();

    public void setClient(final ApiClient client) {
        this.client = client;
    }

    public boolean addNode(final BsimNodeData nodeData) {

        if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {
            // create an AddNodeData object
            if (nodeData.getNodeType() == NodeType.LTE) {

                invokeGroovyMethodOnArgs("BsimAddNodeOperator", "createLTEAddNodeData", nodeData.getNodeName(), nodeData.getNodeFdn(),
                        nodeData.getNodeTemplate(),

                        nodeData.CriticalData.getOssMimVersion(), "IP_V4");

            } else if (nodeData.getNodeType() == NodeType.MICRO_LTE || nodeData.getNodeType() == NodeType.MICRO_MACRO_LTE) {

                invokeGroovyMethodOnArgs("BsimAddNodeOperator", "createMicroLTEAddNodeData", nodeData.getNodeName(), nodeData.getNodeFdn(),

                nodeData.getNodeTemplate(), nodeData.CriticalData.getOssMimVersion(), "IP_V4");
            } else if (nodeData.getNodeType() == NodeType.WCDMA) {

                invokeGroovyMethodOnArgs("BsimAddNodeOperator", "createWCDMAAddNodeData", nodeData.getNodeName(), nodeData.getNodeFdn(),

                nodeData.getNodeTemplate(), nodeData.CriticalData.getRncName(), nodeData.CriticalData.getRbsGroup(),

                nodeData.CriticalData.getOssMimVersion(), "IP_V4");
            } else if (nodeData.getNodeType() == NodeType.MICRO_WCDMA) {
                log.info("nodeData.getAifData().getSecurity() in operator" + nodeData.getAifData().getSecurity());
                invokeGroovyMethodOnArgs("BsimAddNodeOperator", "createMicroWCDMAAddNodeData", nodeData.getNodeName(), nodeData.getNodeFdn(),

                nodeData.getNodeTemplate(), nodeData.CriticalData.getRncName(), nodeData.CriticalData.getRbsGroup(),

                nodeData.CriticalData.getOssMimVersion(), "IP_V4");
            } else if (nodeData.getNodeType() == NodeType.MICRO_MACRO_LTE || nodeData.getNodeType() == NodeType.MICRO_LTE) {
                invokeGroovyMethodOnArgs("BsimAddNodeOperator", "createMicroLTEAddNodeData", nodeData.getNodeName(), nodeData.getNodeFdn(),
                        nodeData.getNodeTemplate(), nodeData.CriticalData.getOssMimVersion(), "IP_V4");

            } else {
                log.error(String.format("Node Type: %1$s is not supported!", nodeData.getNodeType().name()));
                return false;
            }

            // General data
            setAttributesForGeneral(nodeData);
            // log.info("nodeData.getNodeVersion" + nodeData.getNodeVersion());
            // BCG data
            setAttributesForBCG(nodeData);

            // Auto Integrate data
            log.info("nodeData.getAifData().isAutoIntegrate()" + nodeData.getAifData().isAutoIntegrate());
            log.info("nodeData.getAifData().getAifDataOptionAttrs()" + nodeData.getAifData().getAifDataOptionAttrs());
            if (nodeData.getAifData().isAutoIntegrate()) {
                setAttributesForAutoIntegrate(nodeData);
            }

            // Call Bsim Service to Add Node
            invokeGroovyMethodOnArgs("BsimAddNodeOperator", "addNode");

            // wait for the process of adding node
            // try {
            // Thread.sleep(60 * 1000);
            // }
            // catch (final InterruptedException e) {
            // }

            return true;

        } else if (nodeData.getNodeType() == NodeType.Router_6672) {

            log.info("arg are " + nodeData.getNodeName() + "," + nodeData.getNodeFdn() + "," + nodeData.getNodeTemplate() + ","
                    + nodeData.getSpitfireDomain().toString());
            invokeGroovyMethodOnArgs("BsimAddNodeOperator", "createSpitfireAddNodeData", nodeData.getNodeName(), nodeData.getNodeFdn(),
                    nodeData.getNodeTemplate(), nodeData.getSpitfireDomain().toString());

            // General data
            log.info("SETTING GENERAL TAB ATTR");
            setAttributesForGeneralSpitfire(nodeData);
            log.info("SETTING AI TAB ATTR");
            setAttributesForAutoIntegrateSpitfire(nodeData);
            // Call Bsim Service to Add Node
            final String str = invokeGroovyMethodOnArgs("BsimAddNodeOperator", "addNodeSpitfire");
            final int k = Integer.parseInt(str);
            log.info("size of addnodedata obj" + k);

            return true;

        } else {
            log.info("arg are " + nodeData.getNodeName() + "," + nodeData.getNodeFdn() + "," + nodeData.getNodeTemplate() + ","
                    + nodeData.getDg2Domain().toString() + "," + nodeData.CriticalData.getRncName());
            invokeGroovyMethodOnArgs("BsimAddNodeOperator", "createDG2AddNodeData", nodeData.getNodeName(), nodeData.getNodeFdn(), nodeData.getNodeTemplate(),
                    nodeData.getDg2Domain().toString(), nodeData.CriticalData.getRncName(), nodeData.CriticalData.getBscName());

            // General data
            log.info("SETTING GENERAL TAB ATTR");
            log.info("add node data attr " + nodeData.getAddNodeDataAttrs());
            final LinkedHashMap<String, String> nodeDataAttributes = nodeData.getAddNodeDataAttrs();
            log.info("add node data attr IP Address " + nodeDataAttributes.get("IP Address"));
            if (nodeDataAttributes.get("IP Address").equals("192.168.103.226")) {
                nodeDataAttributes.put("IP Address", "192.168.103.230");
                nodeData.setAddNodeDataAttrs(nodeDataAttributes);
            }
            setAttributesForGeneralDG2(nodeData);
            log.info("NODE TEMPLATE ATRR ARE :" + nodeData.getNodeTemplateAttrs());
            log.info("SETTING AI TAB ATTR");
            log.info("nodeData.getAifData().getAifDataOptionAttrs()" + nodeData.getAifData().getAifDataOptionAttrs());
            setAttributesForAutoIntegrateDG2(nodeData);
            // Call Bsim Service to Add Node
            final String str = invokeGroovyMethodOnArgs("BsimAddNodeOperator", "addNodeDG2");
            final int k = Integer.parseInt(str);
            log.info("size of addnodedata obj : " + k);

            return true;
        }
    }

    // OSS-93001 CMPv2 TAF: Modify Micro/Macro TAF TCs to include Security : ISCF file path for WCDMA
    public boolean checkIscfFileWCDMA(final String nodeName, final boolean isIPv6) {
        String directoryString = "/var/opt/ericsson/smrsstore/WRAN/nedssv4/AIF/" + nodeName + "/SMDown";
        if (isIPv6) {
            directoryString = "/var/opt/ericsson/smrsstore/WRAN/nedssv6/AIF/" + nodeName + "/SMDown";
        }
        final String filesInSMDownDirectory = executor.simpleExec("ls " + directoryString);
        if (filesInSMDownDirectory.contains(nodeName)) {
            log.info("<font color=green>Check ISCF file: File does exist</font>");
            return true;
        } else {
            log.error("Check ISCF file: ==> File does not exist");
            return false;
        }
    }

    public void executeAddNodeCommandOnBsimServer() {
        invokeGroovyMethodOnArgs("BsimAddNodeOperator", "execute");
    }

    public String getAddNodeStatus() {

        invokeGroovyMethodOnArgs("BsimAddNodeOperator", "getStatusMessageLog");
        return invokeGroovyMethodOnArgs("BsimAddNodeOperator", "getAddNodeStatus");
    }

    public String bindLteMacroNodeOnBsimServer(final String nodeName, final String serialNumber) {
        return invokeGroovyMethodOnArgs("BsimAddNodeOperator", "executeHardwareBind", nodeName, serialNumber);
    }

    private void setAttributesForGeneral(final BsimNodeData nodeData) {

        // set attributes for AddNodeData object
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForAddNodeDataObject", nodeData.getAddNodeDataAttrs());

        // set attributes for node template
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForNodeTemplate", nodeData.getNodeTemplateAttrs());
    }

    private void setAttributesForGeneralDG2(final BsimNodeData nodeData) {

        // set attributes for AddNodeData object
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForAddNodeDataObjectDG2", nodeData.getAddNodeDataAttrs());

        // set attributes for node template
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForNodeTemplate", nodeData.getNodeTemplateAttrs());
    }

    private void setAttributesForGeneralSpitfire(final BsimNodeData nodeData) {
        // set attributes for AddNodeData object
        log.info("add node data attr" + nodeData.getAddNodeDataAttrs());
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForAddNodeDataObjectSpitfire", nodeData.getAddNodeDataAttrs());
        // set attributes for node template
        log.info("NODE TEMPLATE ATRR ARE :" + nodeData.getNodeTemplateAttrs());
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForNodeTemplate", nodeData.getNodeTemplateAttrs());

    }

    private void setAttributesForBCG(final BsimNodeData nodeData) {

        // set attributes for transport template
        if (nodeData.isImportTransportConfiguration()) {
            invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForTransportTemplate", nodeData.getTransportTemplateAttrs());
        }

        // set attributes for radio template
        if (nodeData.isImportRadioConfiguration()) {
            invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForRadioTemplate", nodeData.getRadioTemplateAttrs());
        }
    }

    private void setAttributesForAutoIntegrate(final BsimNodeData nodeData) {

        // set attributes for AifData options
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForAifOptionData", nodeData.getAifData().getAifDataOptionAttrs());

        if (nodeData.getNodeType() != NodeType.DG2) {
            // set attributes for Site Basic Template
            invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForSiteBasicTemplate", nodeData.getAifData().getSiteBasicTemplateAttrs());
        }

        if (nodeData.getNodeType() != NodeType.DG2) {
            // set attributes for Site Equipment Template
            invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForSiteEquipmentTemplate", nodeData.getAifData()
                    .getSiteEquipmentTemplateAttrs());
        }

        // set attributes for Site Installation Template
        if (nodeData.getNodeType() == NodeType.LTE || nodeData.getNodeType() == NodeType.MICRO_LTE) {
            invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForSiteInstallationTemplate", nodeData.getAifData()
                    .getSiteInstallationTemplateAttrs());
        }

        if (nodeData.getNodeType() == NodeType.MICRO_WCDMA || nodeData.getNodeType() == NodeType.WCDMA) {
            invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForSiteInstallationTemplate", nodeData.getAifData()
                    .getSiteInstallationTemplateAttrs());

            invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForCabinetEquipmentTemplate", nodeData.getAifData()
                    .getCabinetEquipmentTemplateAttrs());
        }
    }

    private void setAttributesForAutoIntegrateDG2(final BsimNodeData nodeData) {

        // set attributes for AifData options
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForAifOptionDataDG2", nodeData.getAifData().getAifDataOptionAttrs());

    }

    private void setAttributesForAutoIntegrateSpitfire(final BsimNodeData nodeData) {

        // set attributes for AifData options
        log.info("nodeData.getAifData().getAifDataOptionAttrs()" + nodeData.getAifData().getAifDataOptionAttrs());
        invokeGroovyMethodOnAttributesMap("BsimAddNodeOperator", "setAttributeForAifOptionDataSpitfire", nodeData.getAifData().getAifDataOptionAttrs());

    }

    /**
     * Generic method to invoke groovy method with arguments
     * 
     * @param className
     *        the name of groovy class
     * @param method
     *        the name of groovy method
     * @param args
     *        the arguments of the method
     * @return a string that represents the response of the invocation
     */
    protected String invokeGroovyMethodOnArgs(final String className, final String method, final String... args) {

        String respVal = null;
        respVal = client.invoke(className, method, args).getValue();
        log.info(String.format("Invoking %1$s: %2$s", method, respVal));
        return respVal;
    }

    /**
     * Generic method to invoke groovy method for the template attributes stored
     * in a hashmap
     * 
     * @param className
     *        the name of groovy class
     * @param method
     *        the name of groovy method
     * @param attributes
     *        the hashmap containing the attributes
     */
    private void invokeGroovyMethodOnAttributesMap(final String className, final String method, final LinkedHashMap<String, String> attributes) {

        String respVal = null;
        for (final Entry<String, String> attribute : attributes.entrySet()) {
            respVal = client.invoke(className, method, attribute.getKey(), attribute.getValue()).getValue();
        }
        if (attributes.size() > 0) {
            log.info(String.format("Invoking %1$s: %2$s", method, respVal));
        }
    }

    /**
     * Check the node existence using CM Service
     * 
     * @param nodeFdn
     *        the node fdn
     * @return <code>true</code> if the node does exist; <code>false</code> otherwise.
     */
    public String moExist(final String nodeFdn) {

        // check node existence
        final String respVal = client.invoke("MoDetailsRetriever", "moExists", nodeFdn).getValue();
        log.info("Invoking moExists: " + respVal);

        return respVal;
    }

    /**
     * Check that the QR Code has been generated successfully in the specified directory.
     * 
     * @param qrCodeLocation
     * @param qrCodeName
     * @return true if QR code file is found
     */
    public boolean checkQRCodeExists(final String qrCodeLocation, final String qrCodeName) {
        log.info("Checking to see if QR Code has been Generated in file Path: " + qrCodeLocation);
        final OssHost ossHost = new OssHost(BsimApiGetter.getHostMaster());
        final User user = ossHost.getUsers(UserType.OPER).get(0);
        final CLI cli_Master = new CLI(ossHost, user);
        final Shell shell_Master = cli_Master.openShell(Terminal.VT100);
        shell_Master.writeln("cd " + qrCodeLocation);
        shell_Master.writeln("ls");

        if (shell_Master.read().trim().contains(qrCodeName)) {
            log.info("QR Code: " + qrCodeName + " created successfully");
            return true;
        }
        log.error("QR Code: " + qrCodeName + " was not created successfully");
        return false;
    }

    /**
     * Check the node existence using CsTest command
     * 
     * @param nodeName
     * @param isFinalExecutionOfcheckNodeByCmdMethod
     * @return <code>true</code> if the node does exist; <code>false</code> otherwise.
     */
    public boolean checkNodeByCmd(final String nodeName, final boolean isFinalExecutionOfcheckNodeByCmdMethod) {

        // /MeContext changed to ManagedElement for DG2//
        final String commandForONRM_CS = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt ManagedElement | grep " + nodeName;
        final String commandForSeg_masterservice_CS = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS lt MeContext | grep "
                + nodeName;

        String outputStr = executor.simpleExec(commandForONRM_CS);

        if (outputStr != null && outputStr.contains(nodeName)) {
            log.info("node added is (ONRM result): " + outputStr);
            outputStr = executor.simpleExec(commandForSeg_masterservice_CS);
            log.info("node added is (Seg_CS result): " + outputStr);

            if (outputStr != null && outputStr.contains(nodeName)) {
                log.info("<font color=green>Check the created node: ==> exist</font>");
                return true;
            }
        }
        // The isFinalExecutionOfcheckNodeByCmdMethod parameter is used to deal with the calling of the checkNodeByCmd
        // method within a while loop. A better solution may be possible here as a refactoring exercise.
        if (isFinalExecutionOfcheckNodeByCmdMethod) {
            log.error("Check the created node: ==> does not exist");
        } else {
            log.warn("Check the created node: ==> does not exist");
        }
        return false;
    }

    public String getFirstScProfileFromServer() {

        final String grep = ",SC,SCSelection,";
        final String command = "/opt/ericsson/nms_pci_client/bin/scselection.sh -lp | grep " + grep;

        final String outputStr = executor.simpleExec(command);

        String[] scArray = null;

        if (outputStr != null && outputStr.contains("SCSelection")) {

            scArray = outputStr.split(",");

        }

        return scArray[1];
    }

    /**
     * Check the sc calculation value using CsTest command
     * 
     * @param nodeFdn
     *        the node fdn
     * @param planName
     *        - Name of the plan used
     * @param rncName
     *        - name of the RNC
     * @return <code>true</code> if the node does exist; <code>false</code> otherwise.
     */
    public boolean checkSCValuesByCmd(final String nodeName, final String planName, final String rncName) {

        final String utranCellFdn = String.format(
                "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=%1$s,MeContext=%2$s,ManagedElement=1,RncFunction=1,UtranCell=%3$s_UtranCell_1", rncName, rncName,
                nodeName);

        final String scValue = "primaryScramblingCode";

        final String command = String.format("/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS cpm %1$s -p %2$s", utranCellFdn,
                planName);

        final String outputStr = executor.simpleExec(command);
        if (outputStr != null && outputStr.contains("true")) {
            final String scCommand = String.format("/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS la %1$s %2$s -p %3$s",
                    utranCellFdn, scValue, planName);
            final String scOutPut = executor.simpleExec(scCommand);
            if (scOutPut != null) {
                final String[] scValueArray = scOutPut.split(":");
                final String primarySCValue = scValueArray[1];

                if (primarySCValue.contains("1")) {
                    log.info("<font color=green>Check scValue in plan: true</font>");
                    return true;
                }
            }
        }
        log.error("Check SC value in plan: ==> Does Not Exist");
        return false;
    }

    public boolean checkSiteByCmd(final String siteName) {

        final String command = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt Site | grep " + siteName;

        final String outputStr = executor.simpleExec(command);

        if (outputStr != null && outputStr.contains(siteName)) {
            log.info("<font color=green>Check new created site " + siteName + ": ==> Site exists</font>");
            return true;
        } else {
            log.error("Check new created site " + siteName + ": ==> Site does not exist");
            return false;
        }
    }

    public boolean checkPlanByCmd(final String planName) {

        int count = 0;

        final String command = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS lp | grep " + planName;

        boolean planExists = false;
        do {
            log.trace("Command is " + command);
            final String outputStr = executor.simpleExec(command);
            log.trace("Output is" + outputStr);

            if (outputStr != null && outputStr.contains(planName)) {
                planExists = true;
                log.info("<font color=green>Checked the created plan " + planName + " : ==> Plan exists</font>");
                break;
            }
            count++;
            log.info("Count ==> " + count);
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
        } while (count < 5);

        if (planExists) {
            return true;
        } else {
            log.error("Checked the created plan: " + planName + " ==> Plan does not exist");
            return false;
        }
    }

    public boolean checkTransportFile(final String nodeName) {

        final String filePath = String.format("/var/opt/ericsson/nms_umts_wran_bcg/files/import/%1$s_TNBulkCM_bulkCMEngine.xml", nodeName);
        final boolean exist = checkFileExistBySsh(filePath);
        if (exist) {
            log.info("<font color=green>Check Transport file: Success ==> File " + filePath + " exists</font>");
            return true;
        } else {
            log.error("Check Transport file: ==> Does not exist");
            return false;
        }
    }

    public boolean checkRadioFile(final String nodeName) {

        final String directoryString = "/var/opt/ericsson/nms_umts_wran_bcg/files/import/";

        final String fileName = String.format("%1$s_RNBulkCM_bulkCMEngine.xml", nodeName);

        final boolean exist = checkFileExistBySsh(directoryString + fileName);
        if (exist) {
            log.info("<font color=green>Check Radio file: Success ==> File " + directoryString + fileName + " exists</font>");
            return true;
        } else {
            log.error("Check Radio file: ==> does not exist");
            return false;
        }
    }

    public boolean checkSiteBasicFile(final NodeType nodeType, final String nodeName, final String rncName, final boolean isIPv6) {
        String fileName = "SiteBasic";
        if (nodeType == NodeType.MICRO_WCDMA || nodeType == NodeType.WCDMA) {
            fileName = fileName + "WRAN";
        }
        return checkFileExists(nodeType, nodeName, rncName, isIPv6, fileName);

    }

    public boolean checkSiteBasicFileDG2(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String dg2Domain,
            final String basicFileName) {

        return checkDG2FileExists(nodeType, nodeName, rncName, dg2Domain, basicFileName);

    }

    public boolean checkSiteBasicFileSpitfire(final NodeType nodeType, final String nodeName, final String spitfireDomain, final String basicFileName) {

        return checkSpitfireFileExists(nodeType, nodeName, spitfireDomain, basicFileName);

    }

    public boolean checkSiteEquipmentFile(final NodeType nodeType, final String nodeName, final String rncName, final boolean isIPv6) {

        String fileName = "RbsEquipment";
        if (nodeType == NodeType.MICRO_WCDMA || nodeType == NodeType.WCDMA) {
            fileName = fileName + "WRAN";
        }

        return checkFileExists(nodeType, nodeName, rncName, isIPv6, fileName);
    }

    public boolean checkSiteEquipmentFileDG2(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String dg2Domain,
            final String equipmentFileName) {

        return checkDG2FileExists(nodeType, nodeName, rncName, dg2Domain, equipmentFileName);
    }

    public boolean checkOssNodeProtocolFileDG2(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String dg2Domain,
            final String ossNodeProtocolFileName) {

        return checkDG2FileExists(nodeType, nodeName, rncName, dg2Domain, ossNodeProtocolFileName);

    }

    public boolean checkOssNodeProtocolFileSpitfire(
            final NodeType nodeType,
            final String nodeName,
            final String spitfireDomain,
            final String ossNodeProtocolFileName) {

        return checkSpitfireFileExists(nodeType, nodeName, spitfireDomain, ossNodeProtocolFileName);

    }

    public boolean checkSiteInstallationFile(final NodeType nodeType, final String nodeName, final String rncName, final boolean isIPv6) {

        final String fileName = "SiteInstall";

        return checkFileExistsUnderLocalMasterServer(nodeType, nodeName, rncName, fileName, isIPv6);
    }

    public boolean checkAutoIntegrationSummaryFile(final NodeType nodeType, final String nodeName, final String rncName, final boolean isIPv6) {

        String fileName = "AutoIntegrationRbsSummaryFile";
        if (nodeType == NodeType.MICRO_WCDMA || nodeType == NodeType.WCDMA) {
            fileName = fileName + "WRAN";
        }
        return checkFileExistsUnderSmrsStore(nodeType, nodeName, rncName, fileName, isIPv6);
    }

    public boolean checkAutoIntegrationSummaryFileDG2(final NodeType nodeType, final String nodeName, final String rncName, final String dg2Domain) {

        final String fileName = "AutoIntegrationRbsSummaryFile.xml";

        return checkFileExistsUnderSmrsStoreDG2(nodeType, nodeName, rncName, dg2Domain, fileName);
    }

    public boolean checkAutoIntegrationSummaryFileSpitfire(final NodeType nodeType, final String nodeName, final String spitfireDomain) {

        final String fileName = "AutoIntegrationRbsSummaryFile.xml";
        return checkFileExistsUnderSmrsStoreSpitfire(nodeType, nodeName, spitfireDomain, fileName);
    }

    /**
     * The Cabinet Equipment file is generated for Micro WCDMA only
     * Two copies of this file are generated under /var/opt/ericsson/smrsstore/WRAN/nedssv4/AIF/RNC_NAME/NODE_NAME/
     * & /opt/ericsson/nms_umts_bsim_server/dat/node/NODE_NAME/
     * Returns true if file is found.
     * 
     * @param nodeType
     * @param nodeName
     * @param rncName
     * @param isIPv6
     * @return
     */
    public boolean checkCabinetEquipmentFilesExist(final NodeType nodeType, final String nodeName, final String rncName, final boolean isIPv6) {

        final String fileName = "CabinetEquipmentWRAN";

        return checkFileExists(nodeType, nodeName, rncName, isIPv6, fileName);
    }

    /**
     * @param nodeType
     * @param nodeName
     * @param rncName
     * @param isIPv6
     * @param fileName
     * @return
     */
    private boolean checkFileExists(final NodeType nodeType, final String nodeName, final String rncName, final boolean isIPv6, final String fileName) {
        boolean fileFound = false;
        if (nodeType == NodeType.MICRO_WCDMA) {
            final boolean fileExistsFileExistsUnderSmrsStore = checkFileExistsUnderSmrsStore(nodeType, nodeName, rncName, fileName, isIPv6);
            final boolean fileFileExistsUnderlocalMasterServerDirectory = checkFileExistsUnderLocalMasterServer(nodeType, nodeName, rncName, fileName, isIPv6);
            if (fileExistsFileExistsUnderSmrsStore && fileFileExistsUnderlocalMasterServerDirectory) {
                fileFound = true;
            } else {
                fileFound = false;
            }
        } else {
            fileFound = checkFileExistsUnderSmrsStore(nodeType, nodeName, rncName, fileName, isIPv6);
        }
        return fileFound;
    }

    private boolean checkDG2FileExists(final NodeType nodeType, final String nodeName, final String rncName, final String dg2Domain, final String fileName) {
        boolean fileFound = false;
        if (dg2Domain == DG2Domain.LRAN.toString() || dg2Domain == DG2Domain.WRAN.toString() || dg2Domain == DG2Domain.W_L_RAN.toString()
                || dg2Domain == DG2Domain.GRAN.toString() || dg2Domain == DG2Domain.TRANSPORT.toString() || dg2Domain == DG2Domain.W_G_RAN.toString()
                || dg2Domain == DG2Domain.L_G_RAN.toString() || dg2Domain == DG2Domain.W_L_G_RAN.toString()) {
            final boolean fileExistsFileExistsUnderSmrsStore = checkFileExistsUnderSmrsStoreDG2(nodeType, nodeName, rncName, dg2Domain, fileName);
            final boolean fileFileExistsUnderlocalMasterServerDirectory = checkFileExistsUnderLocalMasterServerDG2(nodeType, nodeName, rncName, fileName);
            if (fileExistsFileExistsUnderSmrsStore && fileFileExistsUnderlocalMasterServerDirectory) {
                fileFound = true;
            } else {
                fileFound = false;
            }
        }
        return fileFound;
    }

    private boolean checkSpitfireFileExists(final NodeType nodeType, final String nodeName, final String spitfireDomain, final String fileName) {

        boolean fileFound = false;

        if (spitfireDomain == SpitfireDomain.TRANSPORT.toString()) {

            final boolean fileExistsFileExistsUnderSmrsStore = checkFileExistsUnderSmrsStoreSpitfire(nodeType, nodeName, spitfireDomain, fileName);

            final boolean fileFileExistsUnderlocalMasterServerDirectory = checkFileExistsUnderLocalMasterServerSpitfire(nodeType, nodeName, fileName);

            if (fileExistsFileExistsUnderSmrsStore && fileFileExistsUnderlocalMasterServerDirectory) {
                fileFound = true;
            } else {
                fileFound = false;
            }
        }
        return fileFound;
    }

    /**
     * Checks for File under SMRS Directory.
     * Returns true if file is found.
     * 
     * @param nodeType
     * @param nodeName
     * @param rncName
     * @param fileName
     * @param isIPv6
     * @return
     */
    private boolean checkFileExistsUnderLocalMasterServer(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String fileName,
            final boolean isIPv6) {
        final String filePath = getAITemplateMasterServerLocalFilePath(nodeType, nodeName, rncName, fileName, isIPv6);

        final boolean exist = checkFileExistBySsh(filePath);
        if (exist) {
            log.info("<font color=green>Check " + fileName + " file: Success ==>File " + fileName + " exists on Local master Server Directory</font>");
            return true;
        } else {
            log.error("Check " + fileName + " file: Failure ==> File " + fileName + " does not exist on Local master Server Directory</font>");
            return false;
        }
    }

    private boolean checkFileExistsUnderLocalMasterServerDG2(final NodeType nodeType, final String nodeName, final String rncName, final String fileName) {
        final String filePath = getAITemplateMasterServerLocalFilePathDG2(nodeType, nodeName, rncName, fileName);

        final boolean exist = checkFileExistBySsh(filePath);
        if (exist) {
            log.info("<font color=green>Check " + fileName + " file: Success ==>File " + fileName + " exists on Local master Server Directory</font>");
            return true;
        } else {
            log.error("Check " + fileName + " file: Failure ==> File " + fileName + " does not exist on Local master Server Directory</font>");
            return false;
        }
    }

    /**
     * Checks for File under SMRS Store Directory.
     * Returns true if file is found.
     * 
     * @param nodeType
     * @param nodeName
     * @param rncName
     * @param isIPv6
     * @return
     */
    private boolean checkFileExistsUnderSmrsStore(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String fileName,
            final boolean isIPv6) {

        final String filePath = getAITemplateSmrsStoreFilePath(nodeType, nodeName, rncName, fileName, isIPv6);

        final boolean exist = checkFileExistBySsh(filePath);
        if (exist) {
            log.info("<font color=green>Check " + fileName + " file: Success ==>File " + fileName + " exists on SMRS Store</font>");
            return true;
        } else {
            log.error("Check " + fileName + " file: Failure ==> File " + fileName + " doese not exist on SMRS Store</font>");
            return false;
        }
    }

    private boolean checkFileExistsUnderSmrsStoreDG2(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String dg2Domain,
            final String fileName) {

        final String filePath = getAITemplateSmrsStoreFilePathDG2(nodeType, nodeName, rncName, dg2Domain, fileName);

        final boolean exist = checkFileExistBySsh(filePath);
        if (exist) {
            log.info("<font color=green>Check " + fileName + " file: Success ==>File " + fileName + " exists on SMRS Store</font>");
            return true;
        } else {
            log.error("Check " + fileName + " file: Failure ==> File " + fileName + " doese not exist on SMRS Store</font>");
            return false;
        }
    }

    public boolean checkExportFile(final NodeType nodeType, final String nodeName, final String rncName) {

        final String directoryString = "/opt/ericsson/nms_umts_bsim_server/dat/";

        String fileName;
        if (rncName != null) {
            // for WCDMA
            fileName = String.format("bsim_export_%2$s_%1$s.xml", nodeName, rncName);
        } else {
            // for LTE
            fileName = String.format("bsim_export_%1$s.xml", nodeName);
        }
        int count = 0;
        boolean exist = false;
        do {
            if (checkFileExistBySsh(directoryString + fileName)) {
                exist = true;
                break;
            }
            count++;
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (count < 60);

        if (exist) {
            log.info("<font color=green>Check Export file: Success " + fileName + " found under " + directoryString + " </font>");
            return true;
        } else {
            log.error("Check Export file: ==> Export file: Failure " + fileName + " not found under " + directoryString);
            return false;
        }
    }

    public boolean checkExportFileDG2(final NodeType nodeType, final String nodeName, final String rncName) {

        final String directoryString = "/opt/ericsson/nms_umts_bsim_server/dat/";

        final String fileName;

        fileName = String.format("bsim_export_%1$s.xml", nodeName);

        int count = 0;
        boolean exist = false;
        do {
            if (checkFileExistBySsh(directoryString + fileName)) {
                exist = true;
                break;
            }
            count++;
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (count < 60);

        if (exist) {
            log.info("<font color=green>Check Export file: Success " + fileName + " found under " + directoryString + " </font>");
            return true;
        } else {
            log.error("Check Export file: ==> Export file: Failure " + fileName + " not found under " + directoryString);
            return false;
        }
    }

    public boolean checkExportFileSpitfire(final NodeType nodeType, final String nodeName) {

        final String directoryString = "/opt/ericsson/nms_umts_bsim_server/dat/";
        final String fileName;
        fileName = String.format("bsim_export_%1$s.xml", nodeName);
        int count = 0;
        boolean exist = false;
        do {
            if (checkFileExistBySsh(directoryString + fileName)) {
                exist = true;
                break;
            }
            count++;
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (count < 60);
        if (exist) {
            log.info("<font color=green>Check Spitfire Export file: Success " + fileName + " found under " + directoryString + " </font>");
            return true;
        } else {
            log.error("Check Spitfire Export file: ==> Export file: Failure " + fileName + " not found under " + directoryString);
            return false;
        }

    }

    public boolean checkFileExistBySsh(final String filePath) {

        final RemoteObjectHandler remoteFileHandler = BsimApiGetter.getRemoteFileHandler(BsimApiGetter.getHostMaster());

        final boolean result = remoteFileHandler.remoteFileExists(filePath);

        return result;
    }

    public boolean checkExportFileHasNetworkConfigurationBySshGrep(final String fileDirectory, final String fileName, final String networkConfiguration) {

        final String command = String.format("cd  %1$s; cat %2$s | grep -c 'Security networkConfiguration=\"%3$s\"'", fileDirectory, fileName,
                networkConfiguration);

        final String outputStr = executor.simpleExec(command).trim();

        if (outputStr != null && outputStr.equals("1")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns the AI Template File path based on the parameters passed to this method
     * 
     * @param nodeType
     * @param nodeName
     * @param rncName
     * @param fileName
     * @param isIPv6
     * @return filePath
     */
    private String getAITemplateSmrsStoreFilePath(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String fileName,
            final boolean isIPv6) {

        String filePath;
        if (nodeType == NodeType.WCDMA || nodeType == NodeType.MICRO_WCDMA) {
            filePath = "/var/opt/ericsson/smrsstore/WRAN/nedssv4/AIF/" + rncName + "/" + nodeName + "/" + fileName + ".xml";
        } else {
            if (isIPv6) {
                filePath = "/var/opt/ericsson/smrsstore/LRAN/nedssv6/AIF/" + nodeName + "/" + fileName + ".xml";
            } else {
                filePath = "/var/opt/ericsson/smrsstore/LRAN/nedssv4/AIF/" + nodeName + "/" + fileName + ".xml";
            }
        }

        return filePath;
    }

    private String getAITemplateSmrsStoreFilePathDG2(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String dg2Domain,
            final String fileName) {

        String filePath = "";
        // new changes : xriskas OSS-137737
        if (dg2Domain == DG2Domain.WRAN.toString()) {

            filePath = "/var/opt/ericsson/smrsstore/CORE/nedssv4/AIF/" + rncName + "/" + nodeName + "/" + fileName;
        } else {
            filePath = "/var/opt/ericsson/smrsstore/CORE/nedssv4/AIF/" + nodeName + "/" + fileName;
        }

        return filePath;
    }

    /**
     * Returns the AI Template File path based on the parameters passed to this method
     * 
     * @param nodeType
     * @param nodeName
     * @param rncName
     * @param fileName
     * @param isIPv6
     * @return filePath
     */
    private String getAITemplateMasterServerLocalFilePath(
            final NodeType nodeType,
            final String nodeName,
            final String rncName,
            final String fileName,
            final boolean isIPv6) {

        String filePath = "";
        if (nodeType == NodeType.WCDMA || nodeType == NodeType.MICRO_WCDMA) {
            filePath = "/opt/ericsson/nms_umts_bsim_server/dat/node/" + nodeName + "/" + fileName + ".xml";
        }

        return filePath;
    }

    private String getAITemplateMasterServerLocalFilePathDG2(final NodeType nodeType, final String nodeName, final String rncName, final String fileName) {

        String filePath = "";

        filePath = "/opt/ericsson/nms_umts_bsim_server/dat/node/" + nodeName + "/" + fileName;

        return filePath;
    }

    public String executeAddNodeCommandOnBsimServer(final String nodesToAdd) {
        log.info("BsimAddNodeApiOperator:  Invoking execute() to add : " + nodesToAdd + " Node(s)");
        return invokeGroovyMethodOnArgs("BsimAddNodeOperator", "execute", nodesToAdd);
    }

    public boolean checkIscfFile(final String nodeName, final boolean isIPv6) {

        String directoryString = "/var/opt/ericsson/smrsstore/LRAN/nedssv4/AIF/" + nodeName + "/SMDown";
        if (isIPv6) {
            directoryString = "/var/opt/ericsson/smrsstore/LRAN/nedssv6/AIF/" + nodeName + "/SMDown";
        }
        final String filesInSMDownDirectory = executor.simpleExec("ls " + directoryString);
        if (filesInSMDownDirectory.contains(nodeName)) {
            log.info("<font color=green>Check ISCF file: File does exist</font>");
            return true;
        } else {
            log.error("Check ISCF file: ==> File does not exist");
            return false;
        }
    }

    public boolean checkNetworkConfiguration(final NodeType nodeType, final String nodeName, final String rncName, final String networkConfiguration) {

        final String directoryString = "/opt/ericsson/nms_umts_bsim_server/dat/";

        String fileName;
        if (rncName != null) {
            // for WCDMA
            fileName = String.format("bsim_export_%2$s_%1$s.xml", nodeName, rncName);
        } else {
            // for LTE
            fileName = String.format("bsim_export_%1$s.xml", nodeName);
        }
        int count = 0;
        boolean exist = false;
        do {
            if (checkExportFileHasNetworkConfigurationBySshGrep(directoryString, fileName, networkConfiguration)) {
                exist = true;
                break;
            }
            count++;
            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } while (count < 60);

        if (exist) {
            log.info("<font color=green>Check Network Configuratuion is " + networkConfiguration + " : Network Configuration is correct in export file</font>");
            return true;
        } else {
            log.error("Check Network Configuratuion in export file: ==> Network Configuration is incorrect");
            return false;
        }
    }

    public boolean deleteSMRSAccount(final String smrsAccountName) {

        log.info("Deleting SMRS Account");

        final String command = "/opt/ericsson/nms_bismrs_mc/bin/del_aif.sh -a " + smrsAccountName;

        final String response = executor.simpleExec(command);

        try {
            Thread.sleep(5000);
        } catch (final InterruptedException e) {
        }

        if (response.contains("successfully deleted")) {
            return true;
        } else {
            return false;
        }
    }

    public boolean checkDoesSmrsAccountExistForNode(final BsimNodeData nodeData, final boolean expectedResult) {

        log.info("Checking does SMRS account exist for Node");

        Boolean smrsAccountExists = false;

        final String nodeName = nodeData.getNodeName();

        final String command = "cat /ericsson/smrs/etc/smrs_config | grep " + nodeName;

        int count = 0;

        final int maximumCount = 5;

        do {

            final String returnValue = infraServerCli.simpleExec(command);

            if (returnValue != null && !returnValue.equals("")) {

                smrsAccountExists = true;
            }

            count++;

            try {
                Thread.sleep(5000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount && smrsAccountExists == false);

        if (smrsAccountExists) {
            if (expectedResult) {
                log.info("Success ==> SMRS Account Found");
            } else {
                log.error("Failure ==> SMRS Account Found : Expected False");
            }
        } else {
            if (expectedResult) {
                log.error("Failure ==> SMRS Account Not Found : Expected True");
            } else {
                log.info("Success ==> SMRS Account Not Found");
            }
        }

        return smrsAccountExists;
    }

    public boolean checkSiteInstallationFileExists() {

        final String result = executor.simpleExec("find /home/nmsadm/TAF_SiteInstallation.xml");

        return result.contains("TAF_SiteInstallation.xml");
    }

    public boolean checkMoExists(final BsimNodeData bsimNodeData) {
        boolean moExists = false;
        final String nodeName = bsimNodeData.getNodeName();
        final String commandForSeg_masterservice_CS = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS lt OSSIntegration | grep "
                + nodeName;
        final String result = executor.simpleExec(commandForSeg_masterservice_CS);
        try {
            Thread.sleep(3000);
        } catch (final InterruptedException e) {
            // TODO Auto-generated catch block (Nov 19, 2015:11:33:31 AM by xsonaro)
            log.info("Exception has occurred while finding OSSIntegration MO" + e);

        }
        if (result != null && !result.equals("")) {
            moExists = true;
        }
        return moExists;

    }

    private boolean checkFileExistsUnderSmrsStoreSpitfire(final NodeType nodeType, final String nodeName, final String spitfireDomain, final String fileName) {

        final String filePath = getAITemplateSmrsStoreFilePathSpitfire(nodeType, nodeName, spitfireDomain, fileName);
        final boolean exist = checkFileExistBySsh(filePath);
        if (exist) {
            log.info("<font color=green>Check spitfire " + fileName + " file: Success ==>File " + fileName + " exists on SMRS Store</font>");
            return true;
        } else {
            log.error("Check spitfire " + fileName + " file: Failure ==> File " + fileName + " doese not exist on SMRS Store</font>");
            return false;
        }

    }

    private boolean checkFileExistsUnderLocalMasterServerSpitfire(final NodeType nodeType, final String nodeName, final String fileName) {

        final String filePath = getAITemplateMasterServerLocalFilePathSpitfire(nodeType, nodeName, fileName);
        final boolean exist = checkFileExistBySsh(filePath);
        if (exist) {
            log.info("<font color=green>Check spitfire " + fileName + " file: Success ==>File " + fileName + " exists on Local master Server Directory</font>");
            return true;
        } else {
            log.error("Check spitfire " + fileName + " file: Failure ==> File " + fileName + " does not exist on Local master Server Directory</font>");
            return false;
        }
    }

    private String getAITemplateSmrsStoreFilePathSpitfire(final NodeType nodeType, final String nodeName, final String spitfireDomain, final String fileName) {

        String filePath = "";
        if (spitfireDomain == SpitfireDomain.TRANSPORT.toString()) {
            // oss-137737
            filePath = "/var/opt/ericsson/smrsstore/CORE/nedssv4/AIF/" + nodeName + "/" + fileName;

        }

        return filePath;
    }

    private String getAITemplateMasterServerLocalFilePathSpitfire(final NodeType nodeType, final String nodeName, final String fileName) {

        String filePath = "";
        filePath = "/opt/ericsson/nms_umts_bsim_server/dat/node/" + nodeName + "/" + fileName;

        return filePath;

    }

}

package com.ericsson.oss.bsim.operators.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.Assert;

import com.ericsson.cifwk.taf.ApiOperator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.handlers.netsim.implementation.NetsimNE;
import com.ericsson.cifwk.taf.handlers.netsim.implementation.SshNetsimHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.netsim.RbsConfigLevelHelper;
import com.ericsson.oss.bsim.netsim.domain.RbsConfigLevel;
import com.ericsson.oss.bsim.utils.file.LocalTempFileConstants;

/**
 * @author efitrob
 */
/**
 * @author egavhug
 */
public class NetsimApiOperator implements ApiOperator {

    private static final String NETSIM_USER_CMDS_TAF_DIR = "/netsim/user_cmds_taf/";

    protected static Logger log;

    protected static SshNetsimHandler sshNetsimHandler;

    protected static BsimRemoteCommandExecutor netsimSshRemoteCommandExecutor;

    protected static BsimRemoteCommandExecutor bsimSshRemoteCommandExecutor;

    protected static CLICommandHelper omsasHostCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());

    protected static CLICommandHelper netsimHostCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostNetsim());

    protected static CLICommandHelper ossmasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    protected static RemoteObjectHandler omsasRemoteFileHandler = BsimApiGetter.getOmsasServerFileHandler();

    protected static RemoteObjectHandler netsimRemoteFileHandler = BsimApiGetter.getNetsimRemoteFileHandler();

    protected static String database;

    protected static String ONRM_CS;

    protected static String cstest;

    protected static final String SET_ATTRIBUTE = " sa ";

    protected static final String LIST_ATTRIBUTE = " la ";

    protected static final String ADJUST = " adjust ";

    protected static final String ATTACH = " attach ";

    protected static final String DETACH = " detach ";

    private String EUtranCellName;

    public static void prepareNetsim() {

        log = Logger.getLogger(NetsimApiOperator.class);
        sshNetsimHandler = BsimApiGetter.getSshNetsimHandler();
        netsimSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostNetsim());
        bsimSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());
        ossmasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());
        netsimRemoteFileHandler = BsimApiGetter.getNetsimRemoteFileHandler();
        database = "Seg_masterservice_CS";
        cstest = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ";
        ONRM_CS = "ONRM_CS";

    }

    /**
     * Check instance of Netsim on the Server is operational
     * 
     * @return True if Netsim is running
     */
    public boolean checkNetsimIsRunning() {

        return sshNetsimHandler.isNetsimRunning();
    }

    /**
     * Create a new Simulation in Netsim
     * 
     * @param simulationName
     *        - String
     * @return Output from the command line as a String
     */
    public String createNewSimulation(final String simulationName) {

        final String[] createNewSimulationCommand = { ".new simulation " + simulationName };

        log.info("Creating simulation! ==> The command being executed is ==> " + createNewSimulationCommand[0]);

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createNewSimulationCommand);

        final String errorMessage = "Create Simulation failed!";
        final String successfulMessage = "Create Simulation Successful";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    public void openSimulation(final String simulation) {

        sshNetsimHandler.executeCommand(".open " + simulation);

    }

    public Boolean checkSimulationExists(final String simulationName) {

        return sshNetsimHandler.getListOfSimulations().contains(simulationName);
    }

    /**
     * Delete a Simulation in Netsim
     * 
     * @param simulationName
     *        - String
     * @return Output from the command line - String
     */
    public String deleteSimulation(final String simulationName) {

        final String[] deleteSimulationCommand = { ".deletesimulation " + simulationName + ".tar.Z|" + simulationName + " force" };
        log.info("The command being executed is " + deleteSimulationCommand[0] + "checking");
        final Host obj1 = new Host();
        final List<User> userhost = obj1.getUsers();
	log.info("host users Size  " + userhost.size());
        for (int i = 0; i < userhost.size(); i++) {
            log.info("Credentials All: " + userhost.get(i).toString());
        }
        log.info("Credentials netsim: " + BsimApiGetter.getHostNetsim());
        final String outputFromCommandLine = sshNetsimHandler.executeCommand(deleteSimulationCommand);
        log.info("Reponse from delete simulation cmd: " + outputFromCommandLine);
        final String errorMessage = "Delete Simulation failed!";
        final String successfulMessage = "Delete Simulation Successful";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);
    }

    protected String getIPOffset(final String ipAddress, final boolean isIPv6) {

        int lastSeperator = 0;
        if (isIPv6) {
            lastSeperator = ipAddress.lastIndexOf(":");
        } else {
            lastSeperator = ipAddress.lastIndexOf(".");
        }
        final String lastOctedOfIP = ipAddress.substring(lastSeperator + 1, ipAddress.length());

        String ipOffset = "";
        if (isIPv6) {
            // IPv6 is hexadecimal, but the offset should be decimal
            ipOffset = Integer.toString(Integer.parseInt(lastOctedOfIP, 16) - 1);
        } else {
            ipOffset = Integer.toString(Integer.parseInt(lastOctedOfIP) - 1);
        }

        return ipOffset;
    }

    public String getLatestMimVersion(final NodeType nodeType) {

        final String latestMim = "";

        if (nodeType.equals(NodeType.LTE)) {

            final String mimsFromNetsimServer = netsimSshRemoteCommandExecutor.simpleExec("ls /netsim/inst/nedatabases/LTE.ERBS.*.simcmd");
            final String[] unfilteredMims = mimsFromNetsimServer.split(".simcmd*");
            final List<String> filteredMims = new ArrayList<String>();

            filterNetsimResultsFromDirectoryList(unfilteredMims, filteredMims);

            log.info("The last element is ==> " + filteredMims.get(filteredMims.size() - 1));

            return filteredMims.get(filteredMims.size() - 1);

        }
        if (nodeType.equals(NodeType.WCDMA)) {

            final String mimsFromNetsimServer = netsimSshRemoteCommandExecutor.simpleExec("ls /netsim/inst/nedatabases/WCDMA.RBS.*.simcmd");
            final String[] unfilteredMims = mimsFromNetsimServer.split(".simcmd*");
            final List<String> filteredMims = new ArrayList<String>();

            filterNetsimResultsFromDirectoryList(unfilteredMims, filteredMims);

            log.info("The last element is ==> " + filteredMims.get(filteredMims.size() - 1));

            return filteredMims.get(filteredMims.size() - 1);
        }
        return latestMim;
    }

    protected void filterNetsimResultsFromDirectoryList(final String[] unfilteredMims, final List<String> filteredMims) {
        for (final String sim : unfilteredMims) {

            String filteredResult = sim;
            filteredResult = sim.replace("/netsim/inst/nedatabases/", "").replaceAll("\\s+", "").replace(".", " ");
            filteredMims.add(filteredResult);
        }
    }

    /**
     * Start a node in NETSIM
     * 
     * @return True if the network element was started
     */
    public boolean startNode(final String simulationName, final String nodeName) {

        final NetsimNE myNode = new NetsimNE();
        myNode.setName(nodeName.replaceAll("\\s+", ""));

        log.info("Starting Node! ==> Node Name " + myNode.getName() + " in simulation " + simulationName);

        sshNetsimHandler.startNE(simulationName, myNode);
        sshNetsimHandler.executeCommand(".open " + simulationName);

        return sshNetsimHandler.isNeStarted(myNode.getName());

    }

    /**
     * Stop a node in NETSIM
     * 
     * @param simulation
     * @param node
     * @return False if the network element is stopped
     */
    public boolean stopNode(final String simulationName, final String nodeName) {

        final NetsimNE myNode = new NetsimNE();
        myNode.setName(nodeName.replaceAll("\\s+", ""));
        log.info("Node Name ==> " + nodeName);
        log.info("Stopping Node! ==> Node Name " + myNode.getName());

        sshNetsimHandler.stopNE(simulationName, myNode);
        sshNetsimHandler.executeCommand(".open " + simulationName);
        log.info("isNeStarted returns " + sshNetsimHandler.isNeStarted(myNode.getName()));
        return sshNetsimHandler.isNeStarted(myNode.getName());

    }

    /**
     * Utility method to transfer files from source to a Netsim instance
     * 
     * @param String
     *        foldertoCreate
     * @param String
     *        locationOfFilesToTransfer
     * @return String "Files transferred successfully" if successful or
     *         "File Transfer failed!" if unsuccessful
     */
    public String transferFilesFromTargetToNetsim(final String foldertoCreate, final String fileToTransfer) {

        String response = null;
        try {
            if (fileToTransfer.contains("target")) {
                netsimSshRemoteCommandExecutor.simpleExec("mkdir " + foldertoCreate);
                netsimRemoteFileHandler.copyLocalFileToRemote(fileToTransfer, "/netsim/" + foldertoCreate);
                log.info("File " + fileToTransfer + " will now be transferred into the folder: /netsim/" + foldertoCreate);

                final String filesTransferred = netsimSshRemoteCommandExecutor.simpleExec("ls /netsim/" + foldertoCreate);
                response = "File Transfer failed!";

                if (filesTransferred.contains(fileToTransfer.subSequence(fileToTransfer.length() - 14, fileToTransfer.length()))) {
                    response = "Files transferred successfully";
                }
            }
        } catch (final Exception e) {
            log.warn("Exception " + e.getMessage() + " thrown");
        }
        return response;
    }

    /**
     * Configure Node created in Netsim for BSIM by executing a kertayle
     * script transferred to the Netsim instance
     * 
     * @param simulationName
     * @param nodeName
     * @return String "Node configured successfully" if successful or
     *         "Node configuration failed!" if unsuccessful
     */
    public String executeConfigureNodeForBsimScript(final String simulationName, final String nodeName, final NodeType nodeType) {

        final String scriptToExecute = chooseConfigureNodeForBsimScriptToExecute(nodeName, nodeType);

        final String configureNodeForBsim = "echo 'kertayle:file=\"/netsim/user_cmds_taf/" + scriptToExecute + "\";'|/netsim/inst/netsim_pipe -sim "
                + simulationName + " -ne " + nodeName.replaceAll("\\s+", "");

        log.info("Command is: " + configureNodeForBsim);

        final String outputFromCommandLine = netsimSshRemoteCommandExecutor.simpleExec(configureNodeForBsim);
        final String errorMessage = "configureNodeForBsim command failed!";
        final String successfulMessage = "configureNodeForBsim command executed successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    private String chooseConfigureNodeForBsimScriptToExecute(final String nodeName, final NodeType nodeType) {

        String scriptToExecute = "";
        if (nodeName.contains("RNC")) {
            scriptToExecute = "configure_rnc.mo";
        } else if (nodeType.equals(NodeType.LTE) || nodeType.equals(NodeType.MICRO_LTE)) {
            scriptToExecute = "configure_lte_node_for_bsim.mo";
        } else if (nodeType.equals(NodeType.WCDMA) || nodeType.equals(NodeType.MICRO_WCDMA)) {
            scriptToExecute = "configure_rbs.mo";
        }
        log.info("BSIM script being executed is ==> " + scriptToExecute);
        return scriptToExecute;
    }

    /**
     * Activate the Planned Configuration on an RNC only <br>
     * </br>
     * Key expected output is <br>
     * </br>
     * Status for node RNC_FDN is COMPLETE <br>
     * </br> Output from Successful Plan Activation similar to: <br>
     * </br>
     * Included global environment settings
     * Included local environment settings
     * Report file not requested, logging will be directed to the console
     * OP: 03/02/15 17:34:57 About to start CLI activation
     * OP_END: 03/02/15 17:35:04 Activation progress 27% for plan MICRO_WCDMANode_TAF_1422984479028_PCA
     * OP_END: 03/02/15 17:35:06 Activation progress 60% for plan MICRO_WCDMANode_TAF_1422984479028_PCA
     * OP_END: 03/02/15 17:35:06 Activation PARTLY_REALIZED for plan MICRO_WCDMANode_TAF_1422984479028_PCA
     * OP_END: 03/02/15 17:35:06 Status for node SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=RNC01 is COMPLETE
     * OP_END: 03/02/15 17:35:06 Status for node SubNetwork=ONRM_ROOT_MO_R,SubNetwork=RNC01,MeContext=MICRO_WCDMANode_TAF_1422984479028 is
     * FAILED
     * OP_END: 03/02/15 17:35:06 Activation operation has ended <br>
     * </br>
     * 
     * @param planName
     * @return Activation Successful if Plan Activation is successful
     * @throws InterruptedException
     */
    public String activatePlannedConfiguration(final String planName, final String rncFdn, final String rncName) throws InterruptedException {

        final String successfulMessage = "Activation Successful";
        final String errorMessage = "Plan Activation Failed";

        log.info("Activating planned Configuration for plan " + planName);

        final CLICommandHelper ossMastercommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());
        final String executPlanCommand = "/opt/ericsson/nms_umts_wran_bcg/bin/bcgtool.sh -a " + planName;
        final String responseFromPlanActivation = ossMastercommandHelper.simpleExec(executPlanCommand);

        final String expectedStatusAfterPlanActivationOnRnc = "Status for node " + rncFdn + " is COMPLETE";
        log.info("Activate Plan command executed");
        if (responseFromPlanActivation.contains(expectedStatusAfterPlanActivationOnRnc)) {

            log.info("Plan Activation Successful");
            log.info("Repsonse from Plan cactivation: " + responseFromPlanActivation);
            return successfulMessage;
        } else {
            log.info("Repsonse from Plan cactivation: " + responseFromPlanActivation);
            log.error("Plan Activation Failed");
            return errorMessage;
        }

    }

    /**
     * Check to see if RNC is synchronized in the CS
     * 
     * @param rncFdn
     * @return true if RNC is Synchronized
     * @throws InterruptedException
     */
    public boolean checkRncIsSynchronized(final NodeType nodeType, final String rncFdn) throws InterruptedException {
        if (nodeType == NodeType.WCDMA || nodeType == NodeType.MICRO_WCDMA) {
            log.info("Checking mirrorMIBsynchStatus of RNC to see if it returns a value of 3 (synchronized) , Will activate plan if RNC is synchronized");
            int count = 0;
            final String checkMIBCommand = cstest + database + LIST_ATTRIBUTE + rncFdn + " mirrorMIBsynchStatus";

            do {
                if (bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand).contains("[1] mirrorMIBsynchStatus (enum SupportedMirrorMibSynchStatus r): 3")) {
                    log.info("mirrorMIBsynchStatus of RNC is 3 ==> OK");
                    return true;
                }
                count++;
                Thread.sleep(2000);
                log.info(bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand));
                log.info("Checking to see if RNC is synchronized");
            } while (count < 90);
            log.error("RNC synchronization failed!");
            return false;
        } else {
            return true;
        }
    }

    /**
     * Execute the "Adjust" command on the node created in the
     * Seg_masterservice database. This speeds up the synchronization process
     * 
     * @param fullNodeName
     *        - String
     * @throws InterruptedException
     */
    public void adjustNodeInSeg(final String fullNodeName) throws InterruptedException {

        bsimSshRemoteCommandExecutor.simpleExec(cstest + database + " adjust " + fullNodeName);
        log.info("Adjust Node command executed on: " + fullNodeName);
    }

    /**
     * Begins the integration of a Node in Netsim with a node already created in
     * BSIM
     * 
     * @param fullNodeName
     *        - (Fdn) String
     * @return Output from the command line as a String
     * @throws InterruptedException
     */
    public String executeNetsimStart(final String fullNodeName) throws InterruptedException {

        adjustNodeInSeg(fullNodeName);
        return checkNodeIsSynchronizedInSeg(fullNodeName);

    }

    public boolean attachDG2NodeToMibAdapter(final BsimNodeData nodeData) throws InterruptedException {
        final String nodeFdn = nodeData.getNodeFdn();
        // final String nodeName = nodeData.getDG2NodeNameForNetsim().replace(" ", "");
        log.info("DG2 Node being attached to MIB adapter");
        final String attachDG2NodetoMIB = cstest + database + ATTACH + nodeFdn + " nma1";
        log.info("attachDG2NodetoMIB-->" + attachDG2NodetoMIB);
        final String checkAttachDG2NodetoMIB = cstest + database + " mi " + nodeFdn;
        log.info("checkAttachPicoWRANNodetoMIB-->" + checkAttachDG2NodetoMIB);
        final String expectedOutput = "MibAdapter Name : nma1";
        return checkAttributeIsAppliedToDG2Node(attachDG2NodetoMIB, checkAttachDG2NodetoMIB, expectedOutput);
        /* return checkNodeIsSynchronizedInSeg(nodeName); */
    }

    private boolean checkAttributeIsAppliedToDG2Node(final String attachDG2NodetoMIB, final String checkAttachDG2NodetoMIB, final String expectedOutput) {
        boolean returnValue;
        ossmasterCLICommandHelper.simpleExec(attachDG2NodetoMIB);

        if (ossmasterCLICommandHelper.simpleExec(checkAttachDG2NodetoMIB).contains(expectedOutput)) {
            log.info("Attribute has been set correctly");
            returnValue = true;
        } else {
            log.error("Attribute has NOT been set correctly");
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Check a Node is synchronized in the Seg_masterService_CS
     * 
     * @param nodeFdn
     * @return
     * @throws InterruptedException
     */
    public String checkNodeIsSynchronizedInSeg(final String fullNodeName) throws InterruptedException {
        Thread.sleep(3000);
        final int maximumCount = 20;
        return checkIfNodeIsSyncedInCs(fullNodeName, maximumCount);

    }

    /**
     * Check to see if a Node is synchronized in the CS
     * 
     * @param fullNodeName
     * @param maximumCount
     * @return Node synchronization successful if Node is synced in cs
     * @throws InterruptedException
     */
    protected String checkIfNodeIsSyncedInCs(final String fullNodeName, final int maximumCount) throws InterruptedException {
        int count = 0;
        final String checkMIBCommand = cstest + database + LIST_ATTRIBUTE + fullNodeName + " mirrorMIBsynchStatus";
        log.info("Checking mirrorMIBsynchStatus of Node, executing ==> " + checkMIBCommand);

        do {
            if (bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand).contains("3")) {
                log.info("Output is ==> " + checkMIBCommand);
                log.info("Node is Synchronized, mirrorMIBsynchStatus is 3 ==> OK");
                return "Node synchronization successful";
            }
            count++;
            Thread.sleep(3000);
            log.info(bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand));
            log.info("Checking to see if Node is synchronized");
        } while (count < maximumCount);
        log.error("Node synchronization failed!");
        return "Node synchronization failed!";
    }

    /**
     * Check to see if a Node is NOT synchronized in the CS
     * 
     * @param fullNodeName
     * @param maximumCount
     * @return Node synchronization successful if Node is synced in cs
     * @throws InterruptedException
     */
    public boolean checkIfNodeIsNotSyncedInCs(final String fullNodeName) throws InterruptedException {
        int count = 0;
        final String checkMIBCommand = cstest + database + LIST_ATTRIBUTE + fullNodeName + " mirrorMIBsynchStatus";
        log.info("Checking mirrorMIBsynchStatus of Node, executing ==> " + checkMIBCommand);

        do {
            if (bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand).contains("4")) {
                log.info("Output is ==> " + checkMIBCommand);
                log.info("Node is not synchronized, mirrorMIBsynchStatus is 4 ==> OK");
                return true;
            }
            count++;
            Thread.sleep(3000);
            log.info(bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand));
            log.info("Checking to see if Node is synchronized");
        } while (count < 30);
        log.error("Node is still synchronized");
        return false;
    }

    /**
     * Execute Kertayle script to set Rbs Config Level to required level.
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

        String setRbsConfigLevelCommand = null;
        final RbsConfigLevelHelper rbsConfigLevelHelper = new RbsConfigLevelHelper();
        log.info("checking");
        final File rbsConfigLevelScript = rbsConfigLevelHelper.writeRbsConfigLevelFile(rbsConfigLevel.getRbsConfigLevel(), nodeData.getNodeType(), nodeData);

        copyFileToRemoteHost(rbsConfigLevelScript.getAbsolutePath(), NETSIM_USER_CMDS_TAF_DIR);
        final String fileName = rbsConfigLevelScript.getName();

        log.info("rbsConfigFile to be executed is " + fileName);
        if (nodeData.getNodeType() == NodeType.DG2) {
            setRbsConfigLevelCommand = "echo 'kertayle:file=\"" + NETSIM_USER_CMDS_TAF_DIR + fileName + "\";'|/netsim/inst/netsim_pipe -sim " + simulationName
                    + " -ne " + nodeData.getDG2NodeNameForNetsim().replaceAll("\\s+", "");
        } else if (nodeData.getNodeType() == NodeType.BasebandT) {
            setRbsConfigLevelCommand = "echo 'kertayle:file=\"" + NETSIM_USER_CMDS_TAF_DIR + fileName + "\";'|/netsim/inst/netsim_pipe -sim " + simulationName
                    + " -ne " + nodeData.getNodeName().replaceAll("\\s+", "");
        } else {
            setRbsConfigLevelCommand = "echo 'kertayle:file=\"" + NETSIM_USER_CMDS_TAF_DIR + fileName + "\";'|/netsim/inst/netsim_pipe -sim " + simulationName
                    + " -ne " + nodeData.getNodeNameForNetsim().replaceAll("\\s+", "");
        }
        log.info("Command to be executed is " + setRbsConfigLevelCommand);

        String result = "No Result Returned from Netsim, most likely command failed to execute";
        try {
            result = netsimSshRemoteCommandExecutor.simpleExec(setRbsConfigLevelCommand);
            log.info("Result is :" + result);
        } catch (final Exception ex) {
            log.error("netsim RbsConfigLevel update failed for: " + rbsConfigLevel + ".  Exception: " + ex.getMessage());
        } finally {

            if (rbsConfigLevelScript.exists()) {
                rbsConfigLevelScript.delete();
                deleteFileFromRemoteHost(NETSIM_USER_CMDS_TAF_DIR + fileName);
                log.info("rbsconfiglevelScript DEleted ");
            }
        }

        return result;
    }

    /**
     * Check Rbs Config Level is set to Integration Complete in the
     * Seg_masterservice_CS database for the
     * 
     * @param fullNodeName
     *        - (Fdn) String
     * @param nodeType
     * @param rbsConfigLevel
     *        RbsConfigLevel to set the node to
     * @return Output from the command line as a String
     * @throws InterruptedException
     */
    public String checkRbsConfigLevelIsSetToConfigLevel(
            final BsimNodeData nodeData,
            final String fullNodeName,
            final NodeType nodeType,
            final RbsConfigLevel rbsConfigLevel) throws InterruptedException {
        String rbsLevel = rbsConfigLevel.getRbsConfigLevel().toString();
        log.info("rbs level is ::::::::" + rbsLevel);
        String moAttributes = ",ManagedElement=1,NodeManagementFunction=1,RbsConfiguration=1 rbsConfigLevel";
        log.info("Mo attribute is ::::: for micrilte" + moAttributes);
        log.info("RBS Level is ::::::microLTE" + rbsLevel);
        if (nodeType.equals(NodeType.WCDMA) || nodeType.equals(NodeType.MICRO_WCDMA)) {
            rbsLevel = String.format("rbsConfigLevel (enum RbsConfiguration_ConfigLevels): %1$s", rbsConfigLevel.getRbsConfigLevel());
            moAttributes = ",ManagedElement=1,NodeBFunction=1,RbsConfiguration=1";
        } else if (nodeType.equals(NodeType.DG2) || nodeType.equals(NodeType.BasebandT)) {
            rbsLevel = String.format("rbsConfigLevel (enum ConfigLevel): %1$s", rbsConfigLevel.getRbsConfigLevel());
            moAttributes = ",ManagedElement=" + nodeData.getNodeName() + ",NodeSupport=1,AutoProvisioning=1";
        }

        int count = 0;
        final String myCommand = cstest + database + LIST_ATTRIBUTE + nodeData.getNodeFdn() + moAttributes;
        // final String myCommand = cstest + database + LIST_ATTRIBUTE + fullNodeName + moAttributes;
        // System.out.println(myCommand);

        String returnVal = "";
        do {
            if (nodeData.getNodeType() == NodeType.BasebandT || nodeData.getNodeType() == NodeType.DG2) {
                final String adjustCommand = cstest + database + ADJUST + fullNodeName;
                log.info(" ==> Node type is DG2/BASEBANDT executing adjust command :  " + adjustCommand + "");
                bsimSshRemoteCommandExecutor.simpleExec(adjustCommand);
            }
            log.info("Executing... => " + myCommand);
            returnVal = bsimSshRemoteCommandExecutor.simpleExec(myCommand);
            log.info("Output => " + returnVal);
            if (returnVal.contains(rbsLevel)) {
                log.info("<class =\"passed\">Node synchronization successful, Output returned from command line ==> "
                        + bsimSshRemoteCommandExecutor.simpleExec(myCommand));
                return "RbsConfigLevel checked ==> Correct level reached: " + rbsConfigLevel;
            }
            count++;

            Thread.sleep(100000);

            log.info(" ==> Checking to see if RbsConfigLevel is " + rbsConfigLevel + ", Count is ==> " + count);

        } while (count < 5);

        log.error("Node Integration failed! The RBSConfigLevel is: " + returnVal + "\n but was expecting => " + rbsLevel);

        return "RbsConfigLevel check failed!";

    }

    /**
     * Check that the Cells are unlocked in the Seg_masterservice_CS database
     * 
     * @param fullNodeName
     * @param nodeType
     * @return Output from the command line as a String
     * @throws InterruptedException
     */
    public String checkAreCellsUnlocked(final BsimNodeData nodeData) throws InterruptedException {

        int count = 0;
        EUtranCellName = checkNodeTypeAndUpdateEutranCellName(nodeData.getNodeFdn(), EUtranCellName, nodeData.getNodeType(),
                nodeData.CriticalData.getRadioTemplateName(), nodeData.getNodeName(), nodeData.CriticalData.getRncFdn());

        log.info("EUtranCellName is :" + EUtranCellName);
        final String checkAdministrativeState = cstest + database + " la " + EUtranCellName + " administrativeState";
        log.info("Checking are cells unlocked executing ==>" + checkAdministrativeState);

        do {
            if (nodeData.getNodeType() == NodeType.LTE || nodeData.getNodeType() == NodeType.MICRO_LTE || nodeData.getNodeType() == NodeType.MICRO_MACRO_LTE) {
                if (bsimSshRemoteCommandExecutor.simpleExec(checkAdministrativeState).contains("[1] administrativeState (enum AdminState): 1")) {
                    log.info("Cells Unlocked, Output returned from command line ==> " + bsimSshRemoteCommandExecutor.simpleExec(checkAdministrativeState));
                    return "Cells Unlocked";
                }

            } else if (nodeData.getNodeType() == NodeType.WCDMA || nodeData.getNodeType() == NodeType.MICRO_WCDMA) {
                if (bsimSshRemoteCommandExecutor.simpleExec(checkAdministrativeState).contains("[1] administrativeState (enum AdmStateRn): 1")) {
                    log.info("Cells Unlocked, Output returned from command line ==> " + bsimSshRemoteCommandExecutor.simpleExec(checkAdministrativeState));
                    return "Cells Unlocked";
                }
            }
            count++;
            Thread.sleep(3000);
            log.info(" ==> Checking to see if Cells are unlocked, Count is ==> " + count);
            log.info("Return from check ==> " + bsimSshRemoteCommandExecutor.simpleExec(checkAdministrativeState));

        } while (count < 20);
        return "Cells  did not unlock!";
    }

    /**
     * Update the EUtranCellName depending on Node Type
     * 
     * @param fullNodeName
     *        - String
     * @param EUtranCellName
     *        - String
     * @param nodeType
     *        - String
     * @return Updated EUtranCellName - String
     */
    private String checkNodeTypeAndUpdateEutranCellName(
            final String fullNodeName,
            String EUtranCellName,
            final NodeType nodeType,
            final String radioTemplateName,
            final String nodeName,
            final String rncFdn) {

        if (nodeType.equals(NodeType.LTE) || nodeType.equals(NodeType.MICRO_LTE)) {
            log.info("EutranCell: NodeType is LTE");
            if (radioTemplateName.equals("1Cell_FDD_RNBulkCMExample") || radioTemplateName.equals("3Cell_FDD_RNBulkCMExample_L13")) {
                EUtranCellName = fullNodeName + ",ManagedElement=1,ENodeBFunction=1,EUtranCellFDD=EUtranCellFDD_1";
            }
            if (radioTemplateName.equals("1Cell_TDD_RNBulkCMExample") || radioTemplateName.equals("3Cell_TDD_RNBulkCMExample_L13")) {
                EUtranCellName = fullNodeName + ",ManagedElement=1,ENodeBFunction=1,EUtranCellTDD=EUtranCellTDD_1";
            }
            return EUtranCellName;
        } else if (nodeType.equals(NodeType.WCDMA) || nodeType.equals(NodeType.MICRO_WCDMA)) {
            log.info("EutranCell: NodeType is WCDMA");
            return EUtranCellName = rncFdn + ",ManagedElement=1,RncFunction=1,UtranCell=" + nodeName + "_UtranCell_1";

        } else {
            log.info("EutranCell: Unknown Node Type!");
            return EUtranCellName = fullNodeName;
        }
    }

    public boolean checkAndStopIpAddressIfStartedInNetsim(final String ipAddress) throws IOException {

        log.info("Checking if Ip Address " + ipAddress + " is started in Netsim");
        final String startedIps = sshNetsimHandler.executeCommand(".show started");

        if (startedIps.contains(ipAddress)) {
            log.info("The ip " + ipAddress + " is taken and NE is started, now attempting to stop the node");
            final BufferedReader bufferedReader = new BufferedReader(new StringReader(startedIps));
            String line;
            String simulation = "";
            String NE = "";
            while ((line = bufferedReader.readLine()) != null) {

                if (line.contains(ipAddress + " ")) {

                    final String tempLine = line.replaceAll(" ", "");

                    NE = tempLine.substring(0, tempLine.indexOf("192."));
                    simulation = tempLine.substring(tempLine.lastIndexOf("/") + 1, tempLine.length());
                    stopNode(simulation, NE);

                    if (sshNetsimHandler.isNeStarted(NE) == false) {
                        log.info("Ip Address" + ipAddress + " has been successfully stopped in Netsim");
                        return true;
                    } else {
                        log.error("Ip Address" + ipAddress + " has not been stopped in Netsim");
                        return false;
                    }
                }

            }
        }
        log.info("Ip Address " + ipAddress + " is not taken");
        return true;
    }

    /**
     * Method to check output from a Command line execution on Netsim
     * 
     * @param outputFromCommandLine
     * @param errorMessage
     * @param successfulMessage
     * @return Returns Successful/Unsuccessful message back to the test case
     */
    public String outputToReturnToTestCase(final String outputFromCommandLine, final String errorMessage, final String successfulMessage) {

        if (outputFromCommandLine != null && outputFromCommandLine.contains("OK")) {
            log.info(successfulMessage);
            return successfulMessage;
        } else {
            log.error(errorMessage);
            return errorMessage;
        }
    }

    public void copyFileToRemoteHost(final String localFilePath, final String remoteFilePath) {
        boolean success = true;
        try {
            netsimRemoteFileHandler.copyLocalFileToRemote(localFilePath, remoteFilePath, LocalTempFileConstants.getLocalTempDirName());
        } catch (final Exception e) {
            log.error("Exception " + e.getMessage() + " thrown");
            success = false;
        }
        Assert.assertEquals(success, true);
    }

    public void deleteFileFromRemoteHost(final String remoteFileFullPath) {
        boolean success = true;
        try {
            netsimRemoteFileHandler.deleteRemoteFile(remoteFileFullPath);
        } catch (final Exception e) {
            log.error("Exception " + e.getMessage() + " thrown");
            success = false;
        }
        Assert.assertEquals(success, true);
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
     * @param simulationZipFile
     * @return true if all simulations are imported successfully
     */
    public boolean importSimulations(final List<String> simulations) {
        boolean isSimulationsImported = false;

        for (final String simulation : simulations) {

            isSimulationsImported = false;

            String simulationZipFile = "";

            if (simulation.contains("\\")) {

                simulationZipFile = simulation.substring(simulation.lastIndexOf("\\") + 1, simulation.length());
            } else if (simulation.contains("/")) {

                simulationZipFile = simulation.substring(simulation.lastIndexOf("/") + 1, simulation.length());
            } else {

                simulationZipFile = simulation;
            }

            final String newSimulationName = simulationZipFile.replace(".zip", "") + "_TAF";

            final String[] importSimulationCommand = new String[2];
            importSimulationCommand[0] = ".open " + simulationZipFile;
            importSimulationCommand[1] = ".uncompressandopen " + simulationZipFile + " /netsim/netsimdir/" + newSimulationName + " tryforce";
		log.info("Attempting to Import Simulation " + simulationZipFile);
			final String outputFromCommandLine = sshNetsimHandler.executeCommand(importSimulationCommand);
		
            final String errorMessage = "Import Simulation failed!";
            final String successfulMessage = "Import Simulation Successful";
            deleteSimulation(newSimulationName);

            if (outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage).equals(successfulMessage)) {
                isSimulationsImported = true;
            }

        }
        return isSimulationsImported;
    }

    public boolean importSimulations(final String simulation) {
        boolean isSimulationsImported = false;
        String simulationZipFile = "";

        if (simulation.contains("\\")) {

            simulationZipFile = simulation.substring(simulation.lastIndexOf("\\") + 1, simulation.length());
        } else if (simulation.contains("/")) {

            simulationZipFile = simulation.substring(simulation.lastIndexOf("/") + 1, simulation.length());
        } else {

            simulationZipFile = simulation;
        }

        final String newSimulationName = simulationZipFile.replace(".zip", "");

        final String[] importSimulationCommand = new String[1];

        importSimulationCommand[0] = ".uncompressandopen " + simulationZipFile + " /netsim/netsimdir/" + newSimulationName + " tryforce";
	log.info("clear lock command");
		 String outputFromCommandLine2=sshNetsimHandler.executeCommand(".uncompressandopen clear_lock");

        //log.info("Attempting to Import Simulation " + simulationZipFile);
	String outputFromCommandLine="";
	    if((outputFromCommandLine2.contains("OK"))){
		log.info("Attempting to Import Simulation " + simulationZipFile);
			 outputFromCommandLine = sshNetsimHandler.executeCommand(importSimulationCommand);
		}
        //final String outputFromCommandLine = sshNetsimHandler.executeCommand(importSimulationCommand);
        log.info(outputFromCommandLine);
        final String errorMessage = "Import Simulation failed!";
        final String successfulMessage = "Import Simulation Successful";

        if (outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage).equals(successfulMessage)) {
            isSimulationsImported = true;
        }

        return isSimulationsImported;
    }
}

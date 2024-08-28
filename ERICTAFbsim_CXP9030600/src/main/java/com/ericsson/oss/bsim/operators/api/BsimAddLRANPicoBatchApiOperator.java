package com.ericsson.oss.bsim.operators.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.ApiOperator;
import com.ericsson.cifwk.taf.osgi.client.ApiClient;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;

public class BsimAddLRANPicoBatchApiOperator implements ApiOperator {

    private final Logger log = Logger.getLogger(BsimAddLRANPicoBatchApiOperator.class);

    private final ApiClient client = ClientHelper.getClient();

    final BsimRemoteCommandExecutor executor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    final RemoteObjectHandler fileHandler = BsimApiGetter.getMasterHostFileHandler();

    private static String checkNodeFdnInONRM = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt ManagedElement | grep -c ";

    public String addLRANPicoBatch(final MockLRANPicoBatch mockLRANPicoBatch) {

        invokeGroovyMethodOnArgs("BsimAddLRANPicoBatchOperator", "createLRANPicoBatch", mockLRANPicoBatch.getName(),
                Integer.toString(mockLRANPicoBatch.getSize()));

        // General data
        setAttributesForGeneral(mockLRANPicoBatch);

        // Transport data **
        setAttributesForTransport(mockLRANPicoBatch);

        // log.info("BsimAddLranPicoBatchApiOperator.addLranPicoBatch : setAttributesForTransport called on " +
        // mockLRANPicoBatch.getName());
        // Radio data **
        setAttributesForRadio(mockLRANPicoBatch);
        // log.info("BsimAddLranPicoBatchApiOperator.addLranPicoBatch : setAttributesForRadio called on " + mockLRANPicoBatch.getName());
        // Auto Integrate
        setAttributesForAutoIntegrate(mockLRANPicoBatch);

        // Call Bsim Service to Add Batch
        final String result = invokeGroovyMethodOnArgs("BsimAddLRANPicoBatchOperator", "runAddBatch");

        // wait for the process of adding node
        try {
            Thread.sleep(10 * 1000);
        } catch (final InterruptedException e) {
        }

        return result;
    }

    public List<String> executeLRANPicoBindJob(final String batchName, final List<String> nodeFdns, final int numberOfBinds, final ArrayList<String> hardwareID) {
        return executeLRANPicoBindJob(batchName, nodeFdns, numberOfBinds, hardwareID, 1);
    }

    /**
     * @param batchName
     * @param nodeFdns
     * @param numberOfBinds
     * @param siteInstallAttrs
     * @param templateName
     * @return
     *         Starts a NoHardware Bind for a batch.
     */
    public List<String> executeLRANPicoNoHardwareBindJob(
            final String batchName,
            final List<String> nodeFdns,
            final int numberOfBinds,
            final Map<String, String> siteInstallAttrs,
            final String templateName) {

        return executeLRANPicoNoHardwareBindJob(batchName, nodeFdns, numberOfBinds, siteInstallAttrs, 1, templateName);
    }

    public List<String> executeLRANPicoBindJob(
            final String batchName,
            final List<String> nodeFdns,
            final int numberOfBinds,
            final ArrayList<String> serials,
            final int batchNumber) {

        final List<String> boundNodeFdns = new ArrayList<String>();
        int successfulBinds = 0;
        String bindResult = "";

        for (int i = 0; i < numberOfBinds; i++) {

            final String serialNumber = serials.get(i);

            log.info("Invoking Hardware Bind for batch name - " + batchName + ", serial number - " + serialNumber);
            bindResult = invokeGroovyMethodOnArgs("BindExecutor", "executeHardwareBindOnBatch", batchName, serialNumber);

            if (bindResult.equals("Successful")) {
                log.info("Node Added: " + nodeFdns.get(i) + " With Serial: " + serialNumber);
                successfulBinds++;
                boundNodeFdns.add(nodeFdns.get(i));
            } else {
		// New Logging Added:-
                final String fdn = nodeFdns.get(i);
                final int index = fdn.lastIndexOf("=");
                final String nodename = fdn.substring(index + 1);
                final CLICommandHelper clicommand = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());
                final String filename = clicommand.simpleExec("ls -r /var/opt/ericsson/log | grep -i osgifwk_debug | tail -1");
                log.info("==================================" + filename.trim() + "==========================");
                final String message = clicommand.simpleExec("cat /var/opt/ericsson/log/" + filename.trim() + "|grep -i " + nodename);
                if (message
                        .contains("IDL:com/ericsson/nms/arne/idl/common/exceptions/ARNESessionExecutionException:1.0,Configuration locked by user nmsadm with action ")) {
                    log.info("Transcation is locked by ANRE hence Node addition failed");
                }
                return boundNodeFdns;
            }
        }
        if (successfulBinds == numberOfBinds) {
            bindResult = "Successful";
        }
        return boundNodeFdns;
    }

    public List<String> executeLRANPicoNoHardwareBindJob(
            final String batchName,
            final List<String> nodeFdns,
            final int numberOfBinds,
            final Map<String, String> siteInstallAttrs,
            final int batchNumber,
            final String templateName) {

        final List<String> boundNodeFdns = new ArrayList<String>();
        int successfulBinds = 0;
        String bindResult = "";

        for (int i = 0; i < numberOfBinds; i++) {

            log.info("Invoking No Hardware Bind for batch name - " + batchName);

            if (siteInstallAttrs.get("Integration Outer IP Address") != null) {
                bindResult = invokeGroovyMethodOnArgs("BindExecutor", "executeNoHardwareBindOnBatch", batchName, "/home/nmsadm/noHardwareBindTAF_CCF.xml",
                        templateName, siteInstallAttrs.get("Integration OAM IP Address"), siteInstallAttrs.get("Integration Outer IP Address"));
            } else {
                bindResult = invokeGroovyMethodOnArgs("BindExecutor", "executeNoHardwareBindOnBatch", batchName, "/home/nmsadm/noHardwareBindTAF_CCF.xml",
                        templateName, siteInstallAttrs.get("Integration OAM IP Address"));
            }

            if (bindResult.equals("Successful")) {
                log.info("Node Added: " + nodeFdns.get(i));
                successfulBinds++;
                boundNodeFdns.add(nodeFdns.get(i));
            } else {
                return boundNodeFdns;
            }
        }
        if (successfulBinds == numberOfBinds) {
            bindResult = "Successful";
        }
        return boundNodeFdns;
    }

    public boolean checkBoundNodesDeletedInCS(final List<String> boundNodes) {

        boolean nodeDeleted = false;

        final long time = System.currentTimeMillis();

        final long timeout = time + 60000 * boundNodes.size();

        for (String nodeFdn : boundNodes) {

            nodeDeleted = false;

            while (!nodeDeleted && System.currentTimeMillis() < timeout) {

                final CLI cli = new CLI(BsimApiGetter.getHostInfraServer());

                Shell smrsMasterShell = cli.openShell(Terminal.VT100);

                nodeFdn = nodeFdn.replace("ONRM_ROOT_MO_R", "ONRM_ROOT_MO").replace("MeContext", "ManagedElement");

                final String command = checkNodeFdnInONRM + nodeFdn;

                log.info("Executing command " + command);

                smrsMasterShell = cli.executeCommand(Terminal.VT100, command);

                String commandOutput = smrsMasterShell.read();

                final String[] outputSplit = commandOutput.split("\\r?\\n");

                commandOutput = outputSplit[1];

                final int returnValue = Integer.parseInt(commandOutput);

                if (returnValue == 0) {
                    nodeDeleted = true;
                    log.info(nodeFdn + " has been removed from ONRM!");
                    continue;
                }

                try {
                    Thread.sleep(2000);
                } catch (final InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }

        return nodeDeleted;
    }

    public String moExist(final String s) {

        return client.invoke("MoDetailsRetriever", "moExists", s).getValue();
    }

    public String executeLRANPicoDeleteNodes(final List<String> nodesToDelete) {

        invokeGroovyMethodOnList("DeleteNodesExecutor", "addFdnsForNodesToBeDeleted", nodesToDelete);
        final String result = invokeGroovyMethodOnArgs("DeleteNodesExecutor", "runPicoDeleteNodes");
        return result;
    }

    public String deleteLRANPicoBatch(final String batchName) {

        return invokeGroovyMethodOnArgs("BsimAddLRANPicoBatchOperator", "deleteBatch", batchName);

    }

    private void setAttributesForGeneral(final MockLRANPicoBatch mockLRANPicoBatch) {

        // set attributes for AddNodeData object
        invokeGroovyMethodOnAttributesMap("BsimAddLRANPicoBatchOperator", "setAttributeForLRANPicoBatchObject", mockLRANPicoBatch.getAddLRANBatchDataAttrs());

        // set attributes for node template
        invokeGroovyMethodOnAttributesMap("BsimAddLRANPicoBatchOperator", "setAttributeForNodeTemplate", mockLRANPicoBatch.getNodeTemplateAttrs());
    }

    private void setAttributesForTransport(final MockLRANPicoBatch mockLRANPicoBatch) {

        invokeGroovyMethodOnAttributesMap("BsimAddLRANPicoBatchOperator", "setAttributeForTransportTemplate", mockLRANPicoBatch.getTransportTemplateAttrs());

    }

    private void setAttributesForRadio(final MockLRANPicoBatch mockLRANPicoBatch) {

        invokeGroovyMethodOnAttributesMap("BsimAddLRANPicoBatchOperator", "setAttributeForRadioTemplate", mockLRANPicoBatch.getRadioTemplateAttrs());
    }

    private void setAttributesForAutoIntegrate(final MockLRANPicoBatch mockLRANPicoBatch) {

        // set attributes for ICF Template
        invokeGroovyMethodOnAttributesMap("BsimAddLRANPicoBatchOperator", "setAttributeForIcfTemplate", mockLRANPicoBatch.getIcfTemplateAttrs());

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
     * @return - a string that represents the response of the invocation
     */
    private String invokeGroovyMethodOnArgs(final String className, final String method, final String... args) {

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
     *        the hashmap containing the attributegetValues
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
     * Generic method to invoke groovy method for the List
     * 
     * @param className
     *        the name of groovy class
     * @param method
     *        the name of groovy method
     * @param List
     */
    private void invokeGroovyMethodOnList(final String className, final String method, final List<String> fdns) {

        String respVal = null;
        for (int i = 0; i < fdns.size(); i++) {
            respVal = client.invoke(className, method, fdns.get(i)).getValue();
        }
        if (fdns.size() > 0) {
            log.info(String.format("Invoking %1$s: %2$s", method, respVal));
        }
    }

    /**
     * @return
     *         Returns true if the Combined Configuration File exists on the server.
     */
    public boolean checkCCFFileExists() {

        final String result = executor.simpleExec("find /home/nmsadm/noHardwareBindTAF_CCF.xml");

        return result.equals("/home/nmsadm/noHardwareBindTAF_CCF.xml");
    }

}

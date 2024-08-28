package com.ericsson.oss.bsim.operators.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.ApiOperator;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.osgi.client.ApiClient;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.batch.data.model.MockWRANPicoBatch;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.operators.BsimOperator;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;

public class BsimAddWRANPicoBatchApiOperator implements ApiOperator {

    private final Logger log = Logger.getLogger(BsimAddWRANPicoBatchApiOperator.class);

    private final ApiClient client = ClientHelper.getClient();

    final BsimRemoteCommandExecutor ossMasterRootCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMasterRoot());

    final RemoteObjectHandler masterHostFileHandler = BsimApiGetter.getMasterHostFileHandler();

    final Host infraServer = BsimApiGetter.getHostInfraServer();

    public String addWRANPicoBatch(final MockWRANPicoBatch mockWRANPicoBatch) {

        invokeGroovyMethodOnArgs("BsimAddWRANPicoBatchOperator", "createWRANPicoBatch", mockWRANPicoBatch.getName(),
                Integer.toString(mockWRANPicoBatch.getSize()));

        // General data
        setAttributesForGeneral(mockWRANPicoBatch);

        // Transport data
        setAttributesForTransport(mockWRANPicoBatch);

        // Radio data
        setAttributesForRadio(mockWRANPicoBatch);

        // Auto Integrate
        setAttributesForAutoIntegrate(mockWRANPicoBatch);

        // Call Bsim Service to Add Batch
        final String result = invokeGroovyMethodOnArgs("BsimAddWRANPicoBatchOperator", "runAddBatch");

        try {
            Thread.sleep(10 * 1000);
        } catch (final InterruptedException e) {
        }

        return result;
    }

    public List<String> executeWRANPicoBindJob(final String batchName, final List<String> nodeFdns, final int numberOfBinds) {

        final List<String> boundNodeFdns = new ArrayList<String>();
        int successfulBinds = 0;
        String bindResult = "";
        for (int i = 0; i < numberOfBinds; i++) {
            bindResult = invokeGroovyMethodOnArgs("BindExecutor", "executeHardwareBindOnBatch", batchName, BsimOperator.generateSerialNumber());
            if (bindResult.equals("Successful")) {
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
	 log.info("nodes:: " + boundNodeFdns);
                return boundNodeFdns;
            }
        }
        if (successfulBinds == numberOfBinds) {
            bindResult = "Successful";
        }
        return boundNodeFdns;
    }

    public String executeWRANPicoDeleteNodes(final List<String> nodesToDelete) {

        invokeGroovyMethodOnList("DeleteNodesExecutor", "addFdnsForNodesToBeDeleted", nodesToDelete);
        final String result = invokeGroovyMethodOnArgs("DeleteNodesExecutor", "runPicoDeleteNodes");

        return result;
    }

    public String deleteWRANPicoBatch(final String batchName) {

        return invokeGroovyMethodOnArgs("BsimAddWRANPicoBatchOperator", "deleteBatch", batchName);

    }

    private void setAttributesForGeneral(final MockWRANPicoBatch mockWRANPicoBatch) {

        // set attributes for AddNodeData object
        invokeGroovyMethodOnAttributesMap("BsimAddWRANPicoBatchOperator", "setAttributeForWRANPicoBatchObject", mockWRANPicoBatch.getAddWRANBatchDataAttrs());

        // set attributes for node template
        invokeGroovyMethodOnAttributesMap("BsimAddWRANPicoBatchOperator", "setAttributeForNodeTemplate", mockWRANPicoBatch.getNodeTemplateAttrs());
    }

    private void setAttributesForTransport(final MockWRANPicoBatch mockWRANPicoBatch) {

        invokeGroovyMethodOnAttributesMap("BsimAddWRANPicoBatchOperator", "setAttributeForTransportTemplate", mockWRANPicoBatch.getTransportTemplateAttrs());

    }

    private void setAttributesForRadio(final MockWRANPicoBatch mockWRANPicoBatch) {

        invokeGroovyMethodOnAttributesMap("BsimAddWRANPicoBatchOperator", "setAttributeForRadioTemplate", mockWRANPicoBatch.getRadioTemplateAttrs());
    }

    private void setAttributesForAutoIntegrate(final MockWRANPicoBatch mockWRANPicoBatch) {

        // set attributes for ICF Template
        invokeGroovyMethodOnAttributesMap("BsimAddWRANPicoBatchOperator", "setAttributeForIcfTemplate", mockWRANPicoBatch.getIcfTemplateAttrs());

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
}

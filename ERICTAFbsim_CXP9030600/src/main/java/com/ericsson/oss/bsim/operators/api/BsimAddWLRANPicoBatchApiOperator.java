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
import com.ericsson.oss.bsim.batch.data.model.MockWLRANPicoBatch;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class BsimAddWLRANPicoBatchApiOperator implements ApiOperator {

    private final Logger log = Logger.getLogger(BsimAddWLRANPicoBatchApiOperator.class);

    private final ApiClient client = ClientHelper.getClient();

    final BsimRemoteCommandExecutor ossMasterRootCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMasterRoot());

    final RemoteObjectHandler masterHostFileHandler = BsimApiGetter.getMasterHostFileHandler();

    final Host infraServer = BsimApiGetter.getHostInfraServer();

    public String addWLRANPicoBatch(final MockWLRANPicoBatch mockWLRANPicoBatch) {

        invokeGroovyMethodOnArgs("BsimAddWLRANPicoBatchOperator", "createWLRANPicoBatch", mockWLRANPicoBatch.getName(),
                Integer.toString(mockWLRANPicoBatch.getSize()));

        // General data
        setAttributesForGeneral(mockWLRANPicoBatch);

        // Transport data
        setAttributesForTransport(mockWLRANPicoBatch);

        // Radio data
        setAttributesForRadio(mockWLRANPicoBatch);

        // Auto Integrate
        setAttributesForAutoIntegrate(mockWLRANPicoBatch);

        // Call Bsim Service to Add Batch
        final String result = invokeGroovyMethodOnArgs("BsimAddWLRANPicoBatchOperator", "runAddBatch");

        try {
            Thread.sleep(10 * 1000);
        } catch (final InterruptedException e) {
        }

        return result;
    }

    public List<String> executeWLRANPicoBindJob(final String batchName, final List<String> nodeFdns, final int numberOfBinds) {

        final List<String> boundNodeFdns = new ArrayList<String>();
        int successfulBinds = 0;
        String bindResult = "";
        for (int i = 0; i < numberOfBinds; i++) {
            bindResult = invokeGroovyMethodOnArgs("BindExecutor", "executeHardwareBindOnBatch", batchName, BsimOperator.generateSerialNumber());
            if (bindResult.equals("Successful")) {
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

    public String executeWLRANPicoDeleteNodes(final List<String> nodesToDelete) {

        invokeGroovyMethodOnList("DeleteNodesExecutor", "addFdnsForNodesToBeDeleted", nodesToDelete);
        final String result = invokeGroovyMethodOnArgs("DeleteNodesExecutor", "runPicoDeleteNodes");

        return result;
    }

    public String deleteWLRANPicoBatch(final String batchName) {

        return invokeGroovyMethodOnArgs("BsimAddWLRANPicoBatchOperator", "deleteBatch", batchName);

    }

    private void setAttributesForGeneral(final MockWLRANPicoBatch mockWLRANPicoBatch) {

        // set attributes for AddNodeData object
        invokeGroovyMethodOnAttributesMap("BsimAddWLRANPicoBatchOperator", "setAttributeForWLRANPicoBatchObject",
                mockWLRANPicoBatch.getAddWLRANBatchDataAttrs());

        // set attributes for node template
        invokeGroovyMethodOnAttributesMap("BsimAddWLRANPicoBatchOperator", "setAttributeForNodeTemplate", mockWLRANPicoBatch.getNodeTemplateAttrs());
    }

    private void setAttributesForTransport(final MockWLRANPicoBatch mockWLRANPicoBatch) {

        invokeGroovyMethodOnAttributesMap("BsimAddWLRANPicoBatchOperator", "setAttributeForTransportTemplate", mockWLRANPicoBatch.getTransportTemplateAttrs());

    }

    private void setAttributesForRadio(final MockWLRANPicoBatch mockWLRANPicoBatch) {

        invokeGroovyMethodOnAttributesMap("BsimAddWLRANPicoBatchOperator", "setAttributeForRadioTemplate", mockWLRANPicoBatch.getRadioTemplateAttrs());
    }

    private void setAttributesForAutoIntegrate(final MockWLRANPicoBatch mockWLRANPicoBatch) {

        // set attributes for ICF Template
        invokeGroovyMethodOnAttributesMap("BsimAddWLRANPicoBatchOperator", "setAttributeForIcfTemplate", mockWLRANPicoBatch.getIcfTemplateAttrs());

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

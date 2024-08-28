package com.ericsson.oss.bsim.operators.api;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.ApiOperator;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.Operator;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;

/**
 * Operator class to handle add/delete operations of Node Clusters
 * 
 * @author xananan
 */
@Operator(context = Context.API)
public class AddDeleteNodeClusterOperator implements ApiOperator, IClusterOperator {

    private final Logger log = Logger.getLogger(AddDeleteNodeClusterOperator.class);

    private String clusterId = "";

    private List<String> elementIdList;

    private static final String CLUSTER_GROOVY = "AddModifyDeleteCluster";

    private static final String SUCCESS = "success";

    private static final String DELIMITER_COLON = "::";

    private static final String DELIMITER_HASH = "##";

    final BsimAddNodeApiOperator groovyOperator = new BsimAddNodeApiOperator();

    /**
     * @param clusterType
     * @param noOfRbs
     * @param noOfERbs
     * @param noOfRnc
     * @return elements as String to be added to cluster.
     */
    @Override
    public String getElementIds(final String clusterType, final int noOfRbs, final int noOfERbs, final int noOfRnc, final int noOfPrbs, final int noOfPerbs) {
        String elements = "";
        elementIdList = new ArrayList<String>();
        if (clusterType.toLowerCase().contains("rnc")) {
            elements = groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "getRncIds", "" + noOfRnc);
            if (elements.contains("NO_RNC")) {
                // elements = null;
                log.error("No RNCs with ClusterIdentity MO exist.");
            } else {
                for (final String rncId : elements.split(DELIMITER_COLON)) {
                    elementIdList.add(rncId);
                }
            }
        } else {
            elements = groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "getErbsRbsIds", "" + noOfRbs, "" + noOfERbs, "" + noOfPrbs, "" + noOfPerbs);
            boolean noRbs = false;
            final String[] elementsArray = elements.split(DELIMITER_HASH);
            if (noOfRbs != 0 && elementsArray[0].contains("NO_RBS")) {
                if (noOfRbs != 0) {
                    log.error("No RBSs exist.");
                }
                noRbs = true;
            } else {
                for (final String rbsId : elementsArray[0].split(DELIMITER_COLON)) {
                    elementIdList.add(rbsId);
                }
            }
            if (elementsArray[1].contains("NO_ERBS")) {
                if (noOfERbs != 0) {
                    log.error("No ERBSs exist.");
                }
                if (noRbs) {
                    elements = null;
                }
            } else {
                for (final String erbsId : elementsArray[1].split(DELIMITER_COLON)) {
                    elementIdList.add(erbsId);
                }
            }
        }

        return elements;
    }

    public static String clustername = "";

    /**
     * @param clusterType
     * @param elementIds
     * @return true if cluster create is returns success.
     */
    @Override
    public boolean createCluster(final String clusterType, final String elementIds) {
        boolean clusterStatus = false;
        final String status = groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "createCluster", clusterType, elementIds);

        final String status1 = status.substring(0, 4);
        clustername = status.substring(4);
        log.info("cluster name" + clustername);
        if (status1.equals("succ")) {
            clusterStatus = true;
        }
        //final MockLRANPicoBatch mockLRANPicoBatch = new MockLRANPicoBatch();
        //mockLRANPicoBatch.setClusterName(clustername);

        return clusterStatus;
    }

    /**
     * @param clusterType
     * @param elementIds
     * @return true if cluster edit is returns success.
     */
    @Override
    public boolean editCluster(final String clusterType, final String elementIds) {
        boolean clusterStatus = true;
        final String status = groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "editCluster", clusterType, clusterId, elementIds);
        if (status.equals(SUCCESS)) {
            clusterStatus = true;
        }
        return clusterStatus;
    }

    /**
     * method to call groovy method to delete cluster.
     */

    @Override
    public void deleteCluster() {
        groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "deleteCluster", clusterId);
    }

    /**
     * @param clusterType
     * @param elementIds
     * @return true if cluster create/edit is verified in cex domain, false otherwise.
     */
    @Override
    public boolean verifyChangesInDomain(final String clusterType, final String elementIds) {
        boolean domainResult = true;
        int retryCount = 0;
        if (!elementIds.isEmpty()) {
            while (retryCount < 3) {
                clusterId = groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "getClusterId", clusterType);

                if (clusterId != null && !clusterId.equals("")) {
                    break;
                }
                waitForTopologyUpdate(5000);
                retryCount++;
            }
        }
        if (clusterId != null && !clusterId.equals("")) {
            do {
                final String result = groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "verifyClusterElementsinDomain", clusterId, clusterType,
                        elementIds);
                domainResult = new Boolean(result);
                if (domainResult) {
                    break;
                }
                waitForTopologyUpdate(5000);
                retryCount++;
            } while (retryCount < 3);
        }
        return domainResult;
    }

    /**
     * return true if cluster is deleted from cex domain, false otherwise.
     */

    @Override
    public boolean verifyClusterDeletedInDomain() {
        boolean domainResult = false;
        int retryCount = 0;
        while (retryCount < 5) {
            final String result = groovyOperator.invokeGroovyMethodOnArgs(CLUSTER_GROOVY, "verifyClusterDeletedInDomain", clusterId);
            domainResult = new Boolean(result);
            if (domainResult) {
                break;
            } else {
                waitForTopologyUpdate(1000);
                retryCount++;
            }
        }
        return true;
    }

    private void waitForTopologyUpdate(final int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (final Exception ex) {
            log.info("Exception occured while Topology Update");
        }
    }

}

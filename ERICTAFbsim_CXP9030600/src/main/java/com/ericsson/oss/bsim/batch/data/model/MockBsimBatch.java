package com.ericsson.oss.bsim.batch.data.model;

import java.util.List;
import java.util.Map;

public abstract class MockBsimBatch {

    private int size;

    private String batchName;

    private String ipAddress;

    // Needed for End to end Test case
    private String nodeName;

    private String planName;

    private String site;

    private String nodeVersion;

    private boolean isNoHardwareBind;

    private String siteInstallName;

    private Map<String, String> siteInstallAttrs;

    public abstract RanType getRantype();

    public abstract String getRncName();

    public abstract String getRncFdn();

    public abstract String getRncIpAddress();

    public abstract List<String> getNodeFdnValues();

    /**
     * Get RNC Name for Netsim as a String
     *
     * @return
     */
    public abstract String getRncNameForNetsim();

    public void setName(final String name) {

        batchName = name;
    }

    public String getName() {

        return batchName;
    }

    public int getSize() {

        return size;
    }

    public void setSize(final int size) {

        this.size = size;
    }

    public String getNodeNameForNetsim() {

        // When creating a node in Netsim, there needs to be a string followed by a space followed by a number as a String
        // eg if the node name = BSIM_WRAN_BATCH_003_Node_001 in the CSV we pass in "BSIM_WRAN_BATCH_003_Node_0 01"
        final String netsimNodeName = nodeName.substring(0, nodeName.length() - 2) + " " + nodeName.substring(nodeName.length() - 2, nodeName.length());
        return netsimNodeName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(final String nodeName) {
        this.nodeName = nodeName;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(final String planName) {
        this.planName = planName;
    }

    public String getSite() {
        return site;
    }

    public void setSite(final String site) {
        this.site = site;
    }

    public String getNodeVersion() {
        return nodeVersion;
    }

    public void setNodeVersion(final String nodeVersion) {
        this.nodeVersion = nodeVersion;
    }

    public void setIsNoHardwareBind(final boolean noHardware) {

        this.isNoHardwareBind = noHardware;
    }

    public boolean getIsNoHardwareBind() {

        return isNoHardwareBind;
    }

    public void setSiteInstallTemplateName(final String siteInstallName) {

        this.siteInstallName = siteInstallName;
    }

    public String getSiteInstallTemplateName() {

        return siteInstallName;
    }

    public void setSiteInstallTemplateAttrs(final Map<String, String> siteInstallAttrs) {

        this.siteInstallAttrs = siteInstallAttrs;
    }

    public Map<String, String> getSiteInstallTemplateAttrs() {

        return siteInstallAttrs;
    }

}
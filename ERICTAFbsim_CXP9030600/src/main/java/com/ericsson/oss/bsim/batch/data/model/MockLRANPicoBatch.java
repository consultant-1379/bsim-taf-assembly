package com.ericsson.oss.bsim.batch.data.model;

import java.util.LinkedHashMap;
import java.util.List;

public class MockLRANPicoBatch extends MockBsimPicoBatch {

    private LinkedHashMap<String, String> addLRANBatchDataAttrs;

    private LinkedHashMap<String, String> transportTemplateAttrs;

    private LinkedHashMap<String, String> radioTemplateAttrs;

    private LinkedHashMap<String, String> nodeTemplateAttrs;

    private LinkedHashMap<String, String> icfTemplateAttrs;

    private List<String> nodeFdnValues;

    private final RanType ran = RanType.LRAN;

    private String subNetworkName;

    private String pciProfileName;

    private boolean usePciProfile = false;

    @Override
    public RanType getRantype() {

        return ran;
    }

    public LinkedHashMap<String, String> getAddLRANBatchDataAttrs() {

        return addLRANBatchDataAttrs;
    }

    public void setAddNodeDataAttrs(final LinkedHashMap<String, String> addLRANBatchDataObjectAttrs) {

        addLRANBatchDataAttrs = addLRANBatchDataObjectAttrs;
    }

    public LinkedHashMap<String, String> getNodeTemplateAttrs() {

        return nodeTemplateAttrs;
    }

    public void setNodeTemplateAttrs(final LinkedHashMap<String, String> nodeTemplateAttrs) {

        this.nodeTemplateAttrs = nodeTemplateAttrs;
    }

    public LinkedHashMap<String, String> getIcfTemplateAttrs() {

        return icfTemplateAttrs;
    }

    public void setIcfTemplateAttrs(final LinkedHashMap<String, String> icfTemplateAttrs) {

        this.icfTemplateAttrs = icfTemplateAttrs;
    }

    @Override
    public List<String> getNodeFdnValues() {
        return nodeFdnValues;
    }

    public void setNodeFdnValues(final List<String> nodeFdnValues) {
        this.nodeFdnValues = nodeFdnValues;
    }

    /**
     * @return the subNetworkName
     */
    public String getSubNetworkName() {
        return subNetworkName;
    }

    /**
     * @param subNetworkName
     *        the subNetworkName to set
     */
    public void setSubNetworkName(final String subNetworkName) {
        this.subNetworkName = subNetworkName;
    }

    /**
     * @return the transportTemplateAttrs
     */
    public LinkedHashMap<String, String> getTransportTemplateAttrs() {
        return transportTemplateAttrs;
    }

    /**
     * @param transportTemplateAttrs
     *        the transportTemplateAttrs to set
     */
    public void setTransportTemplateAttrs(final LinkedHashMap<String, String> transportTemplateAttrs) {
        this.transportTemplateAttrs = transportTemplateAttrs;
    }

    /**
     * @return the radioTemplateAttrs
     */
    public LinkedHashMap<String, String> getRadioTemplateAttrs() {
        return radioTemplateAttrs;
    }

    /**
     * @param radioTemplateAttrs
     *        the radioTemplateAttrs to set
     */
    public void setRadioTemplateAttrs(final LinkedHashMap<String, String> radioTemplateAttrs) {
        this.radioTemplateAttrs = radioTemplateAttrs;
    }

    /**
     * Abstract Method declared in MockBsimBatch to allow retrieval of RNC Name for Netsim
     * This is not applicable for PICO LRAN
     * So return "N/A"
     **/
    @Override
    public String getRncNameForNetsim() {

        return "N/A";
    }

    /**
     * Abstract Method declared in MockBsimBatch to allow retrieval of RNC Name
     * This is not applicaple for PICO LRAN
     * So return "N/A"
     **/
    @Override
    public String getRncName() {
        return "N/A";
    }

    /**
     * Abstract Method declared in MockBsimBatch to allow retrieval of RNC Fdn
     * This is not applicaple for PICO LRAN
     * So return "N/A"
     **/
    @Override
    public String getRncFdn() {
        return "N/A";
    }

    /**
     * Abstract Method declared in MockBsimBatch to allow retrieval of RNC Ip Address
     * This is not applicaple for PICO LRAN
     * So return "N/A"
     **/
    @Override
    public String getRncIpAddress() {
        return "N/A";
    }

    /**
     * @return the pciProfileName
     */
    public String getPciProfileName() {
        return pciProfileName;
    }

    /**
     * @param pciProfileName
     *        the pciProfileName to set
     */
    public void setPciProfileName(final String pciProfileName) {
        this.pciProfileName = pciProfileName;
    }

    /**
     * @return the usePciProfile
     */
    public boolean isUsePciProfile() {
        return usePciProfile;
    }

    /**
     * @param usePciProfile
     *        the usePciProfile to set
     */
    public void setUsePciProfile(final boolean usePciProfile) {
        this.usePciProfile = usePciProfile;
    }

}

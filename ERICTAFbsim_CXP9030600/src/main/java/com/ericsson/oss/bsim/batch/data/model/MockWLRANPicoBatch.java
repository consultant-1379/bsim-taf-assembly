package com.ericsson.oss.bsim.batch.data.model;

import java.util.LinkedHashMap;
import java.util.List;

public class MockWLRANPicoBatch extends MockBsimPicoBatch {

    private LinkedHashMap<String, String> addWLRANBatchDataAttrs;

    private LinkedHashMap<String, String> nodeTemplateAttrs;

    private LinkedHashMap<String, String> transportTemplateAttrs;

    private LinkedHashMap<String, String> radioTemplateAttrs;

    private LinkedHashMap<String, String> icfTemplateAttrs;

    private List<String> nodeFdnValues;

    private final RanType ran = RanType.WLRAN;

    private boolean useScAssignment = false;

    private String scProfileName;

    private String rncName;

    private String rncFdn;

    private String rncIpAddress;

    private String pciProfileName;

    private boolean usePciProfile = false;

    private boolean isEndToEnd = false;

    private boolean isAutoPlan = false;

    // riskas
    private String associatedRnc;

    /**
     * @return the associatedRnc
     */
    public String getAssociatedRnc() {
        return associatedRnc;
    }

    /**
     * @param associatedRnc
     *        the associatedRnc to set
     */
    public void setAssociatedRnc(final String associatedRnc) {
        this.associatedRnc = associatedRnc;
    }

    @Override
    public RanType getRantype() {

        return ran;
    }

    public LinkedHashMap<String, String> getAddWLRANBatchDataAttrs() {

        return addWLRANBatchDataAttrs;
    }

    public void setAddNodeDataAttrs(final LinkedHashMap<String, String> addWLRANBatchDataObjectAttrs) {

        addWLRANBatchDataAttrs = addWLRANBatchDataObjectAttrs;
    }

    public LinkedHashMap<String, String> getNodeTemplateAttrs() {

        return nodeTemplateAttrs;
    }

    public void setNodeTemplateAttrs(final LinkedHashMap<String, String> nodeTemplateAttrs) {

        this.nodeTemplateAttrs = nodeTemplateAttrs;
    }

    public LinkedHashMap<String, String> getTransportTemplateAttrs() {

        return transportTemplateAttrs;
    }

    public void setTransportTemplateAttrs(final LinkedHashMap<String, String> transportTemplateAttrs) {

        this.transportTemplateAttrs = transportTemplateAttrs;
    }

    public LinkedHashMap<String, String> getRadioTemplateAttrs() {

        return radioTemplateAttrs;
    }

    public void setRadioTemplateAttrs(final LinkedHashMap<String, String> radioTemplateAttrs) {

        this.radioTemplateAttrs = radioTemplateAttrs;
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

    public boolean isUseScAssignment() {
        return useScAssignment;
    }

    public void setUseScAssignment(final boolean useScAssignment) {
        this.useScAssignment = useScAssignment;
    }

    public String getScProfileName() {
        return scProfileName;
    }

    public void setScProfileName(final String scProfileName) {
        this.scProfileName = scProfileName;
    }

    public boolean IsEndToEnd() {

        return isEndToEnd;
    }

    public void setIsEndToEnd(final boolean isEndToEnd) {

        this.isEndToEnd = isEndToEnd;
    }

    @Override
    public String getRncNameForNetsim() {

        return ran + "_RNC_ 01";

    }

    @Override
    public String getRncName() {
        return rncName;
    }

    public void setRncName(final String rncName) {
        this.rncName = rncName;
    }

    @Override
    public String getRncFdn() {
        return rncFdn;
    }

    public void setRncFdn(final String rncFdn) {
        this.rncFdn = rncFdn;
    }

    @Override
    public String getRncIpAddress() {
        return rncIpAddress;
    }

    public void setRncIpAddress(final String rncIpAddress) {
        this.rncIpAddress = rncIpAddress;
    }

    /**
     * @param isAutoPlan
     */
    public void setIsAutoPlan(final boolean isAutoPlan) {
        this.isAutoPlan = isAutoPlan;

    }

    public boolean getIsAutoPlan() {
        return this.isAutoPlan;

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

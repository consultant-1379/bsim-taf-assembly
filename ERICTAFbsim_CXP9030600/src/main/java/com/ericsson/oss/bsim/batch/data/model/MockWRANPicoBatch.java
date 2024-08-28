package com.ericsson.oss.bsim.batch.data.model;
import java.util.LinkedHashMap;
import java.util.List;

public class MockWRANPicoBatch extends MockBsimPicoBatch {

    private LinkedHashMap<String, String> addWRANBatchDataAttrs;

    private LinkedHashMap<String, String> nodeTemplateAttrs;

    private LinkedHashMap<String, String> transportTemplateAttrs;

    private LinkedHashMap<String, String> radioTemplateAttrs;

    private LinkedHashMap<String, String> icfTemplateAttrs;

    private List<String> nodeFdnValues;

    private final RanType ran = RanType.WRAN;

    private boolean useScAssignment = false;

    private String scProfileName;

    private String rncName;

    private String rncFdn;

    private String rncIpAddress;

    private boolean isEndToEnd = false;

    private boolean isAutoPlan = false;

    @Override
    public RanType getRantype() {

        return ran;
    }

    public LinkedHashMap<String, String> getAddWRANBatchDataAttrs() {

        return addWRANBatchDataAttrs;
    }

    public void setAddNodeDataAttrs(final LinkedHashMap<String, String> addWRANBatchDataObjectAttrs) {

        addWRANBatchDataAttrs = addWRANBatchDataObjectAttrs;
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
}


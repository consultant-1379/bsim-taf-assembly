package com.ericsson.oss.bsim.data.model;

/**
 * The <code>BsimCriticalData</code> class presents the critical node data for
 * the test case. Normally these data are used for validation and verification.
 * <p>
 * The purpose is to separate the data for adding node functionality and robustness improvement feature. This class can help to maintain
 * these data and provide a better data encapsulation.
 * <p>
 * It is part of {@link BsimNodeData}.
 * 
 * @author exuuguu
 */
public class BsimCriticalData {

    // indicate the RNC for WCDMA
    private String rncName;

    private String rncIpAddress;

    private String rbsGroup;

    private String rncFdn = ""; // set to empty String to avoid null pointer for LTE

    private String site;

    // for end-to-end case only
    private String ipAddress; // for netsim case, it requires a hard-coded IP
                              // address currently

    private String ossNodeAndMimMapping;

    private String ossMimVersion;

    private String netsimMimVersion;

    private String transportTemplateName;

    private String radioTemplateName;

    // For GRAN Bsc
    private String bscName;

    private String AssociatedBsc;

    public BsimCriticalData(final BsimCriticalData criticalData) {
        this.site = criticalData.getSite();

        this.ipAddress = criticalData.getIpAddress();

        this.rncName = criticalData.getRncName();

        this.rncFdn = criticalData.getRncFdn();

        this.rncIpAddress = criticalData.getRncIpAddress();

        this.rbsGroup = criticalData.getRbsGroup();

        this.ossNodeAndMimMapping = criticalData.getOssNodeAndMimMapping();

        this.ossMimVersion = criticalData.getOssMimVersion();

        this.netsimMimVersion = criticalData.getNetsimMimVersion();

        this.transportTemplateName = criticalData.getTransportTemplateName();

        this.radioTemplateName = criticalData.getRadioTemplateName();
        // Adding bsc Fron GRAN GSM
        this.bscName = criticalData.getBscName();

        this.AssociatedBsc = criticalData.getassociatedBsc();
    }

    public BsimCriticalData() {
        // TODO Auto-generated constructor stub
    }

    public String getRncName() {

        return rncName;
    }

    public void setRncName(final String rncName) {

        this.rncName = rncName;
    }

    public String getSite() {

        return site;
    }

    public void setSite(final String site) {

        this.site = site;
    }

    public String getIpAddress() {

        return ipAddress;
    }

    public void setIpAddress(final String ipAddress) {

        this.ipAddress = ipAddress;
    }

    public String getOssNodeAndMimMapping() {

        return ossNodeAndMimMapping;
    }

    public void setOssNodeAndMimMapping(final String ossNodeAndMimMapping) {

        this.ossNodeAndMimMapping = ossNodeAndMimMapping;
    }

    public String getOssMimVersion() {

        return ossMimVersion;
    }

    public void setOssMimVersion(final String mimVersion) {

        ossMimVersion = mimVersion;
    }

    public String getNetsimMimVersion() {

        return netsimMimVersion;
    }

    public void setNetsimMimVersion(final String netsimMimVersion) {

        this.netsimMimVersion = netsimMimVersion;
    }

    public String getTransportTemplateName() {

        return transportTemplateName;
    }

    public void setTransportTemplateName(final String transportTemplateName) {

        this.transportTemplateName = transportTemplateName;
    }

    public String getRadioTemplateName() {

        return radioTemplateName;
    }

    public void setRadioTemplateName(final String radioTemplateName) {

        this.radioTemplateName = radioTemplateName;
    }

    public String getRncIpAddress() {
        return rncIpAddress;
    }

    public void setRncIpAddress(final String rncIpAddress) {
        this.rncIpAddress = rncIpAddress;
    }

    public String getRbsGroup() {
        return rbsGroup;
    }

    public void setRbsGroup(final String rbsGroup) {
        this.rbsGroup = rbsGroup;
    }

    public String getRncFdn() {
        return rncFdn;
    }

    public void setRncFdn(final String rncFdn) {
        this.rncFdn = rncFdn;
    }

    /**
     * @return the bscName
     */
    public String getBscName() {
        return bscName;
    }

    /**
     * @param bscName
     *        the bscName to set
     */
    public void setBscName(final String bscName) {
        this.bscName = bscName;
    }

    public String getassociatedBsc() {

        return AssociatedBsc;
    }

    public void setassociatedBsc(final String AssociatedBsc) {
        this.AssociatedBsc = AssociatedBsc;
    }
}

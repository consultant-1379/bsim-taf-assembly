package com.ericsson.oss.bsim.data.model;

import java.util.LinkedHashMap;

import org.testng.SkipException;

/**
 * The <code>BsimNodeData</code> class stores all the data for the test case of
 * adding a BSIM node data.
 * <p>
 * In this class, it contains the basic node data information such as node name, node fdn, TN & RN templates substitutions and etc. In
 * addition, it has two objects of {@link BsimNodeAifData} and {@link BsimCriticalData}.
 * 
 * @author exuuguu
 */
public class BsimNodeData {

    private final NodeType nodeType;

    private boolean isEndToEnd = false;

    private boolean isIPv6 = false;

    private String planName;

    private String nodeTemplate;

    private String ipConfiguration;

    private String upgradePackageName;

    private String upgradePackageLocation;

    private String nodeName;

    private DG2Domain dg2Domain;

    private SpitfireDomain spitfireDomain;

    private String nodeVersion;

    /**
     * @return the nodeVersion
     */
    public String getNodeVersion() {
        return nodeVersion;
    }

    /**
     * @param nodeVersion
     *        the nodeVersion to set
     */
    public void setNodeVersion(final String nodeVersion) {
        this.nodeVersion = nodeVersion;
    }

    private String nodeFdn;

    private String rncName;

    private String scProfile;

    private boolean isUseSCProfile;

    private LinkedHashMap<String, String> addNodeDataBasicAttrs;

    private LinkedHashMap<String, String> nodeTemplateAttrs;

    private boolean importTransportConfiguration;

    private LinkedHashMap<String, String> transportTemplateAttrs;

    private boolean importRadioConfiguration;

    private LinkedHashMap<String, String> radioTemplateAttrs;

    private boolean geoRequired;

    private BsimNodeAifData aifData = new BsimNodeAifData();

    public BsimCriticalData CriticalData = new BsimCriticalData();

    private boolean antennaSystemConfiguration;

    private boolean rbrQosFactor;

    private boolean generateDHCPExport;

    private boolean bulkPostInstall;

    private boolean updateDHCP;

    private boolean enablePCIAssignment;

    private boolean generateDNSExport;

    private boolean associateToSTN;

    // Adding bsc bean property for GRAN Radio
    private String bscName;

    private boolean isSaveTo;

    private String associatedBSC;

    public BsimNodeData(final NodeType nodeType) {

        this.nodeType = nodeType;
    }

    public BsimNodeData(final BsimNodeData nodeData) {
        nodeType = nodeData.getNodeType();

        isEndToEnd = nodeData.isEndToEnd;

        isIPv6 = nodeData.isIPv6;

        planName = nodeData.getPlanName();

        nodeTemplate = nodeData.getNodeTemplate();

        nodeName = nodeData.getNodeName();

        nodeFdn = nodeData.getNodeFdn();

        rncName = nodeData.getRncName();

        scProfile = nodeData.getScProfile();

        isUseSCProfile = nodeData.isUseSCProfile;

        addNodeDataBasicAttrs = nodeData.getAddNodeDataAttrs();

        nodeTemplateAttrs = nodeData.getNodeTemplateAttrs();

        importTransportConfiguration = nodeData.isImportTransportConfiguration();

        transportTemplateAttrs = nodeData.getTransportTemplateAttrs();

        importRadioConfiguration = nodeData.isImportRadioConfiguration();

        radioTemplateAttrs = nodeData.getRadioTemplateAttrs();

        aifData = nodeData.getAifData();

        antennaSystemConfiguration = nodeData.isantennaSystemConfiguration();

        rbrQosFactor = nodeData.isrbrQosFactor();

        bulkPostInstall = nodeData.isbulkPostInstall();

        enablePCIAssignment = nodeData.isenablePCIAssignment();

        generateDNSExport = nodeData.isgenerateDNSExport();

        associateToSTN = nodeData.isassociateToSTN();

        updateDHCP = nodeData.isupdateDHCP();

        isSaveTo = nodeData.isSaveTo();

        generateDHCPExport = nodeData.isgenerateDHCPExport();

        CriticalData = new BsimCriticalData(nodeData.CriticalData);
        // Setting value for bscc here
        bscName = nodeData.getBscName();

        associatedBSC = nodeData.getAssociatedBSc();

    }

    public NodeType getNodeType() {

        return nodeType;
    }

    public boolean isEndToEnd() {

        return isEndToEnd;
    }

    public void setEndToEnd(final boolean isEndToEnd) {

        this.isEndToEnd = isEndToEnd;
    }

    public boolean isIPv6() {

        return isIPv6;
    }

    public void setIPv6(final boolean isIPv6) {

        this.isIPv6 = isIPv6;
    }

    public void setisSaveTo(final boolean saveTo) {
        this.isSaveTo = saveTo;
    }

    public boolean getisSaveTo() {
        return isSaveTo;
    }

    public boolean isSaveTo() {
        return isSaveTo;
    }

    public String getPlanName() {

        return planName;
    }

    public void setPlanName(final String planName) {

        this.planName = planName;
    }

    public void setupgradePackageName(final String upgradePackageName) {
        this.upgradePackageName = upgradePackageName;
    }

    public void setupgradePackageLocation(final String upgradePackageLocation) {
        this.upgradePackageLocation = upgradePackageLocation;
    }

    public String getupgradePackageName() {
        return this.upgradePackageName;
    }

    public String getupgradePackageLocation() {
        return this.upgradePackageLocation;
    }

    public String getNodeTemplate() {

        return nodeTemplate;
    }

    public String getIpConfiguration() {
        return ipConfiguration;
    }

    public void setNodeTemplate(final String nodeTemplate) {

        this.nodeTemplate = nodeTemplate;
    }

    public void setIpConfiguration(final String ipConfiguration) {

        this.ipConfiguration = ipConfiguration;
    }

    public void setDg2Domain(final DG2Domain dg2Domain) {

        this.dg2Domain = dg2Domain;
    }

    public DG2Domain getDg2Domain() {

        return dg2Domain;
    }

    public String getNodeName() {

        return nodeName;
    }

    // for end-to-end case only
    public String getNodeNameForNetsim() {

        return nodeName + " 999";
    }

    public String getDG2NodeNameForNetsim() {

        return nodeName.substring(0, 25) + " 999";
    }

    // for end-to-end case only
    public String getRncNameForNetsim() {

        return nodeType + "_RNC_ 01";
    }

    public void setNodeName(final String nodeName) {

        this.nodeName = nodeName;
    }

    public String getNodeFdn() {

        return nodeFdn;
    }

    public String getRncName() {

        return rncName;
    }

    public void setRncName(final String rncName) {

        this.rncName = rncName;
    }

    public void setNodeFdn(final String nodeFdn) {

        this.nodeFdn = nodeFdn;
    }

    public LinkedHashMap<String, String> getAddNodeDataAttrs() {

        return addNodeDataBasicAttrs;
    }

    public void setAddNodeDataAttrs(final LinkedHashMap<String, String> addNodeDataObjectAttrs) {

        addNodeDataBasicAttrs = addNodeDataObjectAttrs;
    }

    public LinkedHashMap<String, String> getNodeTemplateAttrs() {

        return nodeTemplateAttrs;
    }

    public void setNodeTemplateAttrs(final LinkedHashMap<String, String> nodeTemplateAttrs) {

        this.nodeTemplateAttrs = nodeTemplateAttrs;
    }

    public boolean isImportTransportConfiguration() {

        return importTransportConfiguration;
    }

    public boolean isbulkPostInstall() {

        return bulkPostInstall;
    }

    public void setbulkPostInstall(final boolean bulkPostInstall) {

        this.bulkPostInstall = bulkPostInstall;
    }

    public boolean isenablePCIAssignment() {

        return enablePCIAssignment;
    }

    public void setenablePCIAssignment(final boolean enablePCIAssignment) {

        this.enablePCIAssignment = enablePCIAssignment;
    }

    public boolean isgenerateDNSExport() {

        return generateDNSExport;
    }

    public void setgenerateDNSExport(final boolean generateDNSExport) {

        this.generateDNSExport = generateDNSExport;
    }

    public boolean isassociateToSTN() {

        return associateToSTN;
    }

    public void setassociateToSTN(final boolean associateToSTN) {

        this.associateToSTN = associateToSTN;
    }

    public boolean isupdateDHCP() {

        return updateDHCP;
    }

    public void setupdateDHCP(final boolean updateDHCP) {

        this.updateDHCP = updateDHCP;
    }

    public boolean isgenerateDHCPExport() {

        return generateDHCPExport;
    }

    public void setgenerateDHCPExport(final boolean generateDHCPExport) {

        this.generateDHCPExport = generateDHCPExport;
    }

    public boolean isrbrQosFactor() {

        return rbrQosFactor;
    }

    public void setrbrQosFactor(final boolean rbrQosFactor) {

        this.rbrQosFactor = rbrQosFactor;
    }

    public boolean isantennaSystemConfiguration() {

        return antennaSystemConfiguration;
    }

    public void setantennaSystemConfiguration(final boolean antennaSystemConfiguration) {

        this.antennaSystemConfiguration = antennaSystemConfiguration;
    }

    public void setImportTransportConfiguration(final boolean importTransportConfiguration) {

        this.importTransportConfiguration = importTransportConfiguration;
    }

    public LinkedHashMap<String, String> getTransportTemplateAttrs() {

        return transportTemplateAttrs;
    }

    public void setTransportTemplateAttrs(final LinkedHashMap<String, String> transportTemplateAttrs) {

        this.transportTemplateAttrs = transportTemplateAttrs;
    }

    public boolean isImportRadioConfiguration() {

        return importRadioConfiguration;
    }

    public void setImportRadioConfiguration(final boolean importRadioConfiguration) {

        this.importRadioConfiguration = importRadioConfiguration;
    }

    public LinkedHashMap<String, String> getRadioTemplateAttrs() {

        return radioTemplateAttrs;
    }

    public void setRadioTemplateAttrs(final LinkedHashMap<String, String> radioTemplateAttrs) {

        this.radioTemplateAttrs = radioTemplateAttrs;
    }

    public BsimNodeAifData getAifData() {

        return aifData;
    }

    @Override
    public String toString() {

        return nodeFdn;
    }

    public String getScProfile() {

        return scProfile;
    }

    public String getAssociatedBSc() {

        return associatedBSC;
    }

    public void setScProfile(final String scProfile) {

        this.scProfile = scProfile;
    }

    public void setAssociatedBSc(final String associatedBSC) {

        this.associatedBSC = associatedBSC;
    }

    public boolean isUseSCProfile() {

        return isUseSCProfile;
    }

    public void setUseSCProfile(final boolean isUseSCProfile) {

        this.isUseSCProfile = isUseSCProfile;
    }

    public Boolean isGeoRequired() {
        return geoRequired;
    }

    public void setGeoRequired(final boolean geoRequired) {
        this.geoRequired = geoRequired;
    }

    /**
     * @return the spitfireDomain
     */
    public SpitfireDomain getSpitfireDomain() {
        return spitfireDomain;
    }

    /**
     * @param spitfireDomain
     *        the spitfireDomain to set
     */
    public void setSpitfireDomain(final SpitfireDomain spitfireDomain) {
        this.spitfireDomain = spitfireDomain;
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

    public void setTemplateAttrs(final LinkedHashMap<String, String> dataMap, final TemplateType templateType) {

        switch (templateType) {

            case BASIC:
                this.setAddNodeDataAttrs(dataMap);
                break;
            case NODE:
                this.setNodeTemplateAttrs(dataMap);
                break;
            case TRANSPORT:
                this.setTransportTemplateAttrs(dataMap);
                break;
            case RADIO:
                this.setRadioTemplateAttrs(dataMap);
                break;
            case AUTOINTEGRATE:
                this.getAifData().setAifDataOptionAttrs(dataMap);
                break;
            case SITE_BASIC:
                this.getAifData().setSiteBasicTemplateAttrs(dataMap);
                break;
            case SITE_INSTALL:
                this.getAifData().setSiteInstallationTemplateAttrs(dataMap);
                break;
            case SITE_EQUIPMENT:
                this.getAifData().setSiteEquipmentTemplateAttrs(dataMap);
                break;
            default:
                throw new SkipException("Invalid Template Type: " + templateType);
        }

    }

}

package com.ericsson.oss.bsim.data.model;

import java.util.LinkedHashMap;

/**
 * The <code>BsimNodeAifData</code> class stores AI-related data for adding node. It is part of {@link BsimNodeData}.
 * 
 * @author exuuguu
 */
public class BsimNodeAifData {

    private boolean autoIntegrate = false;

    private boolean isWithoutLaptop = false;

    private boolean security = false;

    private boolean unlockCells = false;

    private String siteBasicFileName;

    private String siteEquipmentFileName;

    private String ossNodeProtocolFileName;

    private boolean usingOssNodeProtocolFile;

    private boolean isNoHardwareBind;

    private String siteInstallName;

    private boolean isManualBind;

    protected LinkedHashMap<String, Object> ossNodeProtocolTemplateAttrs;

    /**
     * @return the isManualBind
     */
    public boolean isManualBind() {
        return isManualBind;
    }

    /**
     * @param isManualBind
     *        the isManualBind to set
     */
    public void setManualBind(final boolean isManualBind) {
        this.isManualBind = isManualBind;
    }

    // ----------templates----------
    protected LinkedHashMap<String, String> aifDataOptionAttrs;

    protected LinkedHashMap<String, String> siteBasicTemplateAttrs;

    protected LinkedHashMap<String, String> siteEquipmentTemplateAttrs;

    protected LinkedHashMap<String, String> siteInstallationTemplateAttrs;

    protected LinkedHashMap<String, String> cabinetEquipmentTemplateAttrs;

    protected NetworkConfiguration networkConfiguration = NetworkConfiguration.TRUSTED;

    public boolean isAutoIntegrate() {

        return autoIntegrate;
    }

    public void setAutoIntegrate(final boolean autoIntegrate) {

        this.autoIntegrate = autoIntegrate;
    }

    public void setSecurity(final boolean security) {

        this.security = security;
    }

    public boolean getSecurity() {

        return security;
    }

    public LinkedHashMap<String, String> getAifDataOptionAttrs() {

        return aifDataOptionAttrs;
    }

    public void setAifDataOptionAttrs(final LinkedHashMap<String, String> aifDataOptionAttrs) {

        this.aifDataOptionAttrs = aifDataOptionAttrs;
    }

    public String getSiteBasicFileName() {

        return siteBasicFileName;
    }

    public void setSiteBasicFileName(final String siteBasicFileName) {

        this.siteBasicFileName = siteBasicFileName;
    }

    public String getSiteEquipmentFileName() {

        return siteEquipmentFileName;
    }

    public void setSiteEquipmentFileName(final String siteEquipmentFileName) {

        this.siteEquipmentFileName = siteEquipmentFileName;
    }

    public String getOssNodeProtocolFileName() {

        return ossNodeProtocolFileName;
    }

    public void setOssNodeProtocolFileName(final String ossNodeProtocolFileName) {

        this.ossNodeProtocolFileName = ossNodeProtocolFileName;

    }

    public boolean isUsingOssNodeProtocolFile() {
        return usingOssNodeProtocolFile;
    }

    public void setUsingOssNodeProtocolFile(final boolean usingOssNodeProtocolFile) {
        this.usingOssNodeProtocolFile = usingOssNodeProtocolFile;
    }

    public LinkedHashMap<String, String> getSiteBasicTemplateAttrs() {

        return siteBasicTemplateAttrs;
    }

    public void setSiteBasicTemplateAttrs(final LinkedHashMap<String, String> siteBasicTemplateAttrs) {

        this.siteBasicTemplateAttrs = siteBasicTemplateAttrs;
    }

    public LinkedHashMap<String, String> getSiteEquipmentTemplateAttrs() {

        return siteEquipmentTemplateAttrs;
    }

    public void setSiteEquipmentTemplateAttrs(final LinkedHashMap<String, String> siteEquipmentTemplateAttrs) {

        this.siteEquipmentTemplateAttrs = siteEquipmentTemplateAttrs;
    }

    public LinkedHashMap<String, String> getSiteInstallationTemplateAttrs() {

        return siteInstallationTemplateAttrs;
    }

    public void setSiteInstallationTemplateAttrs(final LinkedHashMap<String, String> siteInstallationTemplateAttrs) {

        this.siteInstallationTemplateAttrs = siteInstallationTemplateAttrs;
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

    public LinkedHashMap<String, String> getCabinetEquipmentTemplateAttrs() {
        return cabinetEquipmentTemplateAttrs;
    }

    public void setCabinetEquipmentTemplateAttrs(final LinkedHashMap<String, String> cabinetEquipmentTemplateAttrs) {
        this.cabinetEquipmentTemplateAttrs = cabinetEquipmentTemplateAttrs;
    }

    public boolean isWithoutLaptop() {
        return isWithoutLaptop;
    }

    public void setWithoutLaptop(final boolean isWithoutLaptop) {
        this.isWithoutLaptop = isWithoutLaptop;
    }

    public boolean isUnlockCells() {
        return unlockCells;
    }

    public void setUnlockCells(final boolean unlockCells) {
        this.unlockCells = unlockCells;
    }

    /**
     * @return the networkConfiguration
     */
    public NetworkConfiguration getNetworkConfiguration() {
        return networkConfiguration;
    }

    /**
     * @param networkConfiguration
     *        the networkConfiguration to set
     */
    public void setNetworkConfiguration(final NetworkConfiguration networkConfiguration) {
        this.networkConfiguration = networkConfiguration;
    }

   /**
     * @return the ossNodeProtocolTemplateAttrs
     */
    public LinkedHashMap<String, Object> getOssNodeProtocolTemplateAttrs() {
        return ossNodeProtocolTemplateAttrs;
    }

    /**
     * @param ossNodeProtocolTemplateAttrs
     *        the ossNodeProtocolTemplateAttrs to set
     */
    public void setOssNodeProtocolTemplateAttrs(final LinkedHashMap<String, Object> ossNodeProtocolTemplateAttrs) {
        this.ossNodeProtocolTemplateAttrs = ossNodeProtocolTemplateAttrs;
    }
	
}

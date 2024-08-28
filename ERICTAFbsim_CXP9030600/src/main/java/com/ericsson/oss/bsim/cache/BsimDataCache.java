package com.ericsson.oss.bsim.cache;

import java.util.List;
import java.util.Map;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.BsimDataGetter;

/**
 * @DESCRIPTION BsimDataCache holds persistent data that is required across multiple templates. 
 *              Any data that is required for a specific template is stored in a csv file in the directory csv_data_files.             
 *
 * @author ecilosh
 */
public class BsimDataCache {

    private List<Map<String, String>> listOfSupportedTemplateMaps;

    private Map<String, String> row;

    private NodeType nodeType = null;

    private String nodeVersion = null;

    private String numberOfNodes = null;

    private String isIPv6 = null;

    private String isEndtoEnd = null;

    private String ossNodeTemplate = null;

    private String nodeName = null;

    private String planName = null;

    private String userName = null;

    private String enablePCIAssignment = null;

    private String subNetworkGroup = null;

    private String mimVersion = null;

    private String site = null;

    private String ipAddress = null;

    private String importTransportConfiguration = null;

    private String useTransportTemplate = null;

    private String transportTemplateName = null;

    private String importRadioConfiguration = null;

    private String useRadioTemplate = null;

    private String radioTemplateName = null;

    private String ftpBackup = null;

    private String ftpLicense = null;

    private String ftpSoftware = null;

    private String ftpAI = null;

    private String siteBasicTemplateName = null;

    private String siteInstallTemplateName = null;

    private String siteEquipmentTemplateName = null;

    public BsimDataCache(Map<String, String> row, List<Map<String, String>> listOfSupportedTemplateMaps) {
        this.listOfSupportedTemplateMaps = listOfSupportedTemplateMaps;
        this.row = row;
    }

    /*
     * The following methods use the row input to generate their data
     */
     public NodeType getNodeType(){
         if(this.nodeType == null){
             this.nodeType = NodeType.valueOf(row.get("Node Type"));
         }
         return nodeType;
     }

     public String getNodeVersion(){
         if(this.nodeVersion == null){
             this.nodeVersion = row.get("Node Version");
         }
         return nodeVersion;
     }

     public String getNumberOfNodes(){
         if(this.numberOfNodes == null){
             this.numberOfNodes = row.get("Number Of Nodes");
         }
         return numberOfNodes;
     }

     public String getIsIPv6(){
         if(this.isIPv6 == null){
             this.isIPv6 = row.get("isIPv6");
         }
         return isIPv6;
     }

     public String getIsEndtoEnd(){
         if(this.isEndtoEnd == null){
             this.isEndtoEnd = row.get("isEndtoEnd");
         }
         return isEndtoEnd;
     }


     /*
      * The following methods use listOfSupportedTemplateMaps to generate their data.
      */

     public String getOssNodeTemplate() {
         if(this.ossNodeTemplate == null){
             this.ossNodeTemplate = BsimDataGetter.getOssNodeTemplate(listOfSupportedTemplateMaps);
         }
         return ossNodeTemplate;
     }

     public String getImportTransportConfiguration() {
         if(this.importTransportConfiguration == null){
             cacheTransportData();
         }
         return importTransportConfiguration;
     }

     public String getUseTransportTemplate() {
         if(this.useTransportTemplate == null){
             cacheTransportData();
         }
         return useTransportTemplate;
     }

     public String getTransportTemplateName() {
         if(this.transportTemplateName == null){
             cacheTransportData();
         }
         return transportTemplateName;
     }

     public String getImportRadioConfiguration() {
         if(this.importRadioConfiguration == null){
             cacheRadioData();
         }
         return importRadioConfiguration;
     }

     public String getUseRadioTemplate() {
         if(this.useRadioTemplate == null){
             cacheRadioData();
         }
         return useRadioTemplate;
     }

     public String getRadioTemplateName() {
         if(this.radioTemplateName == null){
             cacheRadioData();
         }
         return radioTemplateName;
     }

     public String getSiteBasicTemplateName() {
         if(this.siteBasicTemplateName == null){
             cacheAITemplateName();
         }
         return siteBasicTemplateName;
     }

     public String getSiteEquipmentTemplateName() {
         if(this.siteEquipmentTemplateName == null){
             cacheAITemplateName();
         }
         return siteEquipmentTemplateName;
     }

     public String getSiteInstallationTemplateName() {
         if(this.siteInstallTemplateName == null){
             cacheAITemplateName();
         }
         return siteInstallTemplateName;
     }

     private void cacheRadioData() {
         String[] data = BsimDataGetter.getRadioData(listOfSupportedTemplateMaps);
         this.radioTemplateName = data[0];
         this.importRadioConfiguration = data[1];
         this.useRadioTemplate = data[2];
     }
     private void cacheTransportData() {
         String[] data = BsimDataGetter.getTransportData(listOfSupportedTemplateMaps);
         this.transportTemplateName = data[0];
         this.importTransportConfiguration = data[1];
         this.useTransportTemplate = data[2];
     }
     private void cacheAITemplateName() {
         String[] data = BsimDataGetter.getAITemplateNames(listOfSupportedTemplateMaps);
         this.siteBasicTemplateName = data[0];
         this.siteInstallTemplateName = data[1];
         this.siteEquipmentTemplateName = data[2];
     }

     /*
      * The following methods generate data per test case. They can use other data in the data cache to do this.
      */

     public String getNodeName() {
         if(this.nodeName == null){
             this.nodeName = BsimDataGetter.getNodeName();
         }
         return nodeName;
     }

     public String getPlanName() {
         if(this.planName == null){
             this.planName = BsimDataGetter.getPlanName();
         }
         return planName;
     }

     public String getUserName() {
         if(this.userName == null){
             this.userName = BsimDataGetter.getUserName();
         }
         return userName;
     }

     public String getEnablePCIAssignment() {
         if(this.enablePCIAssignment == null){
             this.enablePCIAssignment = BsimDataGetter.getEnablePCIAssignment();
         }
         return enablePCIAssignment;
     }

     public String getSubnetworkGroup() {
         if(this.subNetworkGroup == null){
             this.subNetworkGroup = BsimDataGetter.getSubNetworkGroup(this.getNodeType());
         }
         return subNetworkGroup;
     }

     public String getMimVersion() {

         if(this.mimVersion == null){
             this.mimVersion = BsimDataGetter.getMimVersion(this.getNodeType(), this.getNodeVersion());
         }
         return mimVersion;
     }

     public String getSite() {
         if(this.site == null){
             this.site = BsimDataGetter.getSite(this.getOssNodeTemplate());
         }
         return site;
     }

     public String getIpAddress() {
         if(this.ipAddress == null){
             this.ipAddress = BsimDataGetter.getIpAddress(this.getNumberOfNodes(), this.getIsIPv6());
         }
         return ipAddress;
     }

     public String getFtpBackup() {
         if(this.ftpBackup == null){
             cacheFTPServices();
         }
         return ftpBackup;
     }
     public String getFtpLicense() {
         if(this.ftpLicense == null){
             cacheFTPServices();
         }
         return ftpLicense;
     }

     public String getFtpSofware() {
         if(this.ftpSoftware == null){
             cacheFTPServices();
         }
         return ftpSoftware;
     }

     public String getFtpAI() {
         if(this.ftpAI == null){
             cacheFTPServices();
         }
         return ftpAI;
     }
     private void cacheFTPServices() {
         String[] ftpServices = BsimDataGetter.getFtpServices(this.getNodeType(), Boolean.parseBoolean(this.getIsIPv6()));
         this.ftpBackup = ftpServices[0];
         this.ftpLicense = ftpServices[1];
         this.ftpSoftware = ftpServices[2];
         this.ftpAI = ftpServices[3];
     }

}

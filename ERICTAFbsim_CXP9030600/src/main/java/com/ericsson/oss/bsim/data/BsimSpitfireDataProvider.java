/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.data;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.bsim.data.model.NodeType;

/**
 * @author xnehmis
 */
public class BsimSpitfireDataProvider extends BsimDataProvider {

    private static Logger log = Logger.getLogger(BsimSpitfireDataProvider.class);

    public static final String DATA_FILE = DataHandler.getAttribute("dataprovider.ADD_Spitfire.location").toString();

    public List<Object[]> getTestDataList() {
        final String dataFileSelected = selectDataFile();
        log.info("CSV File chosen ==> " + dataFileSelected);
        initialization(dataFileSelected);
        return generateTestDataForAddNode();
    }

    private String selectDataFile() {

        log.info("Tests are running in KGB or CDB");
        return DATA_FILE;
    }

    /**
     * @param templateName
     *        - String
     * @return addNodeDataObjectAttrs - HashMap : Spitfire Node Data Objects
     */
    @Override
    protected LinkedHashMap<String, String> createAddNodeDataBasicAttrs(final String templateName) {

        log.info(String.format("Invoking : BsimSpitfireDataProvider.createAddNodeDataBasicAttrs(%s)", templateName));
        final LinkedHashMap<String, String> addNodeDataObjectAttrs = new LinkedHashMap<String, String>();
        addNodeDataObjectAttrs.put(CSVColumns.OSS_NODE_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.Spitfire_Domain, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_VERSION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_TYPE, EMPTY_STRING_PLACEHOLDER);
        return addNodeDataObjectAttrs;

    }

    /**
     * @param templateName
     *        - String
     * @return nodeTemplateAttrs - HashMap of Node Template Attributes
     */
    @Override
    public LinkedHashMap<String, String> createNodeTemplateAttributes(final String templateName) {
        log.info(String.format("Invoking : BsimSpitfireDataProvider.createNodeTemplateAttributes(%s)", templateName));
        final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();
        // SubNetwork Group
        nodeTemplateAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
        nodeTemplateAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
        // IP Address
        nodeTemplateAttrs.put(CSVColumns.IP_ADDRESS, EMPTY_STRING_PLACEHOLDER);
        // ftpBackUpStore
        nodeTemplateAttrs.put(CSVColumns.FTP_BACKUP_STORE, EMPTY_STRING_PLACEHOLDER);
        // ftpSwStore
        nodeTemplateAttrs.put(CSVColumns.FTP_SW_STORE, EMPTY_STRING_PLACEHOLDER);
        // ftpConfigStore
        nodeTemplateAttrs.put(CSVColumns.FTP_CONFIG_STORE, EMPTY_STRING_PLACEHOLDER);
        // ftpLicenseKey
        nodeTemplateAttrs.put(CSVColumns.FTP_LICENSE_KEY, EMPTY_STRING_PLACEHOLDER);
        // ftpAutoIntegration
        nodeTemplateAttrs.put(CSVColumns.FTP_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);
        // ftpUplink
        nodeTemplateAttrs.put(CSVColumns.FTP_UPLINK, EMPTY_STRING_PLACEHOLDER);
        // Site
        nodeTemplateAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
        // Location
        nodeTemplateAttrs.put(CSVColumns.LOCATION, EMPTY_STRING_PLACEHOLDER);
        // worldTimeZoneId
        nodeTemplateAttrs.put(CSVColumns.WORLD_TIME_ZONE_ID, EMPTY_STRING_PLACEHOLDER);
        // nodeVersion
        nodeTemplateAttrs.put(CSVColumns.NODE_VERSION, EMPTY_STRING_PLACEHOLDER);
        // User Name
        nodeTemplateAttrs.put("User Name", "nmsadm");

        return nodeTemplateAttrs;

    }

    // BSIM Spitfire AI tab code

    @Override
    protected LinkedHashMap<String, String> createAifDataOptionAttributes() {

        final LinkedHashMap<String, String> aifDataOptionAttrs = new LinkedHashMap<String, String>();
        aifDataOptionAttrs.put(CSVColumns.AUTO_INTEGRATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_BASIC_FILE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_EQUIPMENT_FILE, EMPTY_STRING_PLACEHOLDER);
        return aifDataOptionAttrs;

    }

    @Override
    protected LinkedHashMap<String, String> getSiteBasicAttributesByTemplateName(final String templateName, final String aifFtpService, final String nodeName) {

        return null;
    }

    @Override
    public boolean isValidNodeType(final String s) {

        if (s.equals(NodeType.Router_6672.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected NodeType getNodeType() {

        return NodeType.Router_6672;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.data.BsimDataProvider#getTransportTemplateAttributesByTemplateName(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getTransportTemplateAttributesByTemplateName(
            final String templateName,
            final String subNetwork,
            final String nodeName,
            final String planName) {
        // TODO Auto-generated method stub (May 6, 2016:8:35:00 AM by xnehmis)
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.data.BsimDataProvider#getRadioTemplateAttributesByTemplateName(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getRadioTemplateAttributesByTemplateName(
            final String templateName,
            final String subNetwork,
            final String nodeName,
            final String planName) {
        // TODO Auto-generated method stub (May 6, 2016:8:35:00 AM by xnehmis)
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.data.BsimDataProvider#getSiteEquipmentAttributesByTemplateName(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getSiteEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName,
            final String site) {
        // TODO Auto-generated method stub (May 6, 2016:8:35:00 AM by xnehmis)
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.data.BsimDataProvider#getSiteInstallationAttributesByTemplateName(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getSiteInstallationAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName) {
        // TODO Auto-generated method stub (May 6, 2016:8:35:00 AM by xnehmis)
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.data.BsimDataProvider#getCabinetEquipmentAttributesByTemplateName(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    protected LinkedHashMap<String, String> getCabinetEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName) {
        // TODO Auto-generated method stub (May 6, 2016:8:35:00 AM by xnehmis)
        return null;
    }

}


package com.ericsson.oss.bsim.data;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.bsim.data.model.NodeType;

public class BsimLTEStCDBDataProvider extends BsimDataProvider {

    private static Logger log = Logger.getLogger(BsimLTEStCDBDataProvider.class);

    public static final String DATA_FILE = DataHandler.getAttribute("dataprovider.STCDB.location").toString();

    public List<Object[]> getTestDataList() {
        final String dataFileSelected = selectDataFile();
        log.info("CSV File chosen ==> " + dataFileSelected);
        initialization(dataFileSelected);
        return generateTestDataForAddNode();
    }

    /**
     * <p>
     * Used to select data file to use when running in quick feedback loop. Selects data file with greater number of tests when running in
     * KGB and CDB
     * <p/>
     * 
     * @return data file to use in tests
     */
    private String selectDataFile() {
        /*
         * final Host serverHost = DataHandler.getHostByType(HostType.RC);
         * if (serverHost.getIp().contains("atvts")) {
         * log.info("Tests are running in Quick feed back loop");
         * return DATA_FILE_QUICK;
         * }
         */
        log.info("Tests are running in KGB or CDB");
        return DATA_FILE;
    }

    /**
     * @param templateName
     *        - String
     * @return addNodeDataObjectAttrs - HashMap : LTE Node Data Objects
     */
    @Override
    protected LinkedHashMap<String, String> createAddNodeDataBasicAttrs(final String templateName) {

        log.info(String.format("Invoking : BsimLTEDataProvider.createAddNodeDataBasicAttrs(%s)", templateName));

        final LinkedHashMap<String, String> addNodeDataObjectAttrs = new LinkedHashMap<String, String>();
        addNodeDataObjectAttrs.put(CSVColumns.OSS_NODE_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.PLAN_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.ENABLE_PCI_ASSIGNMENT, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.PCI_PROFILE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.MIM_VERSION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_TYPE, EMPTY_STRING_PLACEHOLDER);             // Ja Added extra Column

        addNodeDataObjectAttrs.put(CSVColumns.IMPORT_TRANSPORT_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.USE_TRANSPORT_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.TRANSPORT_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);

        addNodeDataObjectAttrs.put(CSVColumns.IMPORT_RADIO_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.USE_RADIO_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.RADIO_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);

        return addNodeDataObjectAttrs;
    }

    /**
     * @param templateName
     *        - String
     * @return nodeTemplateAttrs - HashMap of Node Template Attributes
     */
    @Override
    public LinkedHashMap<String, String> createNodeTemplateAttributes(final String templateName) {

        log.info(String.format("Invoking : BsimLTEDataProvider.createNodeTemplateAttributes(%s)", templateName));

        final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();

        // SubNetwork Group
        nodeTemplateAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
        // IP Address
        nodeTemplateAttrs.put(CSVColumns.IP_ADDRESS, EMPTY_STRING_PLACEHOLDER);
        // ftpBackUpStore
        nodeTemplateAttrs.put(CSVColumns.FTP_BACKUP_STORE, EMPTY_STRING_PLACEHOLDER);
        // ftpSwStore
        nodeTemplateAttrs.put(CSVColumns.FTP_SW_STORE, EMPTY_STRING_PLACEHOLDER);
        // ftpLicenseKey
        nodeTemplateAttrs.put(CSVColumns.FTP_LICENSE_KEY, EMPTY_STRING_PLACEHOLDER);
        // ftpAutoIntegration
        nodeTemplateAttrs.put(CSVColumns.FTP_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);
        // MIM Version
        nodeTemplateAttrs.put(CSVColumns.MIM_VERSION, EMPTY_STRING_PLACEHOLDER);
        // Site
        nodeTemplateAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
        // Location
        nodeTemplateAttrs.put(CSVColumns.LOCATION, EMPTY_STRING_PLACEHOLDER);
        // worldTimeZoneId
        nodeTemplateAttrs.put(CSVColumns.WORLD_TIME_ZONE_ID, EMPTY_STRING_PLACEHOLDER);
        // Associated STN
        nodeTemplateAttrs.put(CSVColumns.ASSOCIATED_STN, EMPTY_STRING_PLACEHOLDER);
        // User Name
        nodeTemplateAttrs.put("User Name", "nmsadm");
        return nodeTemplateAttrs;
    }

    /**
     * Method to replace Transport Template substitution attributes depending on
     * what Transport template is selected in General tab BSIM Transport Tab
     * 
     * @param templateName
     *        - String
     * @param subNetwork
     *        - String
     * @param nodeName
     *        - String
     * @param planName
     *        - String
     * @return transportTemplateAttrs - HashMap : TN template substitution
     *         attributes
     */
    @Override
    protected LinkedHashMap<String, String> getTransportTemplateAttributesByTemplateName(
            final String templateName,
            final String subNetwork,
            final String nodeName,
            final String planName) {

        // createNodeTemplateAttributes
        final LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();

        if (templateName == null || templateName == "") {
            log.warn("Invoking : BsimLTEDataProvider.getTransportTemplateAttributesByTemplateName : No template received");
        } else {
            log.info(String.format("Invoking : BsimLTEDataProvider.getTransportTemplateAttributesByTemplateName(%s)", templateName));

            transportTemplateAttrs.put(CSVColumns.SUBNETWORK_GROUP, subNetwork);
            transportTemplateAttrs.put(CSVColumns.NODE_NAME, nodeName);
            transportTemplateAttrs.put(CSVColumns.PLAN_NAME, planName);
            transportTemplateAttrs.put("User Name", "nmsadm");

            switch (templateName.toLowerCase()) {
                case "tnbulkcmexample":
                    transportTemplateAttrs.put("eNB ID", generateENodeBId());
                    break;
                case "configureipseconintegratedenodebs":
                    transportTemplateAttrs.put("ipAddress", "1.1.1.11");
                    transportTemplateAttrs.put("ntpServerIpAddress", "11111");
                    transportTemplateAttrs.put("networkPrefixLength", "11111");
                    transportTemplateAttrs.put("aclAction", "11111");
                    transportTemplateAttrs.put("icmpType", "11111");
                    transportTemplateAttrs.put("localIpAddress", "11111");
                    transportTemplateAttrs.put("localIpAddressMask", "11111");
                    transportTemplateAttrs.put("localPort", "11111");
                    transportTemplateAttrs.put("localIpAddressMask", "11111");
                    transportTemplateAttrs.put("protocol", "11111");
                    transportTemplateAttrs.put("remoteIpAddress", "1.1.1.12");
                    transportTemplateAttrs.put("remoteIpAddressMask", "255.255.255.0");
                    transportTemplateAttrs.put("remotePort", "11111");
                    transportTemplateAttrs.put("remotePortFiltering", "11111");
                    transportTemplateAttrs.put("mask", "11111");
                    transportTemplateAttrs.put("idType", "11111");
                    transportTemplateAttrs.put("idFqdn", "11111");
                    transportTemplateAttrs.put("ipv4Address", "11111");
                    transportTemplateAttrs.put("peerIpAddress", "11111");
                    break;
                default:
                    log.warn(String.format(
                            "Invoking : BsimLTEDataProvider.getTransportTemplateAttributesByTemplateName(%s) : invalid template name, not processed",
                            templateName));
                    break;
            }
        }
        return transportTemplateAttrs;
    }

    /**
     * Method to replace Radio Template substitution attributes depending on
     * what Transport template is selected in BSSIM
     * 
     * @param templateName
     *        - String
     * @param subNetwork
     *        - String
     * @param nodeName
     *        - String
     * @param planName
     *        - String
     * @return transportTemplateAttrs - HashMap : TN template substitution
     *         attributes
     */
    @Override
    protected LinkedHashMap<String, String> getRadioTemplateAttributesByTemplateName(
            final String templateName,
            final String subNetwork,
            final String nodeName,
            final String planName) {

        final LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();

        if (templateName == null || templateName == "") {
            log.warn("Invoking : BsimLTEDataProvider.getRadioTemplateAttributesByTemplateName : No template received");
        } else {
            log.info(String.format("Invoking : BsimLTEDataProvider.getRadioTemplateAttributesByTemplateName(%s)", templateName));
            radioTemplateAttrs.put(CSVColumns.SUBNETWORK_GROUP, subNetwork);
            radioTemplateAttrs.put(CSVColumns.NODE_NAME, nodeName);
            radioTemplateAttrs.put(CSVColumns.PLAN_NAME, planName);
            radioTemplateAttrs.put("User Name", "nmsadm");
            switch (templateName.toLowerCase()) {
                case "1cellrnbulkcmexample_l11b":
                case "1cellrnbulkcmexample_l11a":
                case "1cell_fdd_rnbulkcmexample_l12a":
                case "1cell_fdd_rnbulkcmexample_l12b":
                case "1cell_tdd_rnbulkcmexample_l12b":
                case "1cell_tdd_rnbulkcmexample_l12a":
                    radioTemplateAttrs.put("Cell ID 1", "1");
                    radioTemplateAttrs.put("tac", "1");
                    break;
                case "1cell_fdd_rnbulkcmexample_l13":
                case "1cell_fdd_rnbulkcmexample_l13a":
                case "1cell_tdd_rnbulkcmexample_l13":
                case "1cell_tdd_rnbulkcmexample_l13a":
                    radioTemplateAttrs.put("SectorCarrier ID 1", "5");
                    radioTemplateAttrs.put("SectorEquipmentFunction Id", "S2");
                    radioTemplateAttrs.put("Cell ID 1", "4");
                    radioTemplateAttrs.put("tac", "4");
                    break;
                case "3cellrnbulkcmexample_l11b":
                case "3cellrnbulkcmexample_l11a":
                case "3cell_fdd_rnbulkcmexample_l12b":
                case "3cell_fdd_rnbulkcmexample_l12a":
                case "3cell_tdd_rnbulkcmexample_l12b":
                case "3cell_tdd_rnbulkcmexample_l12a":
                    radioTemplateAttrs.put("Cell ID 1", "5");
                    radioTemplateAttrs.put("tac", "5");
                    radioTemplateAttrs.put("Cell ID 2", "12");
                    radioTemplateAttrs.put("Cell ID 3", "13");
                    break;
                case "3cell_fdd_rnbulkcmexample_l13":
                case "3cell_fdd_rnbulkcmexample_l13a":
                case "3cell_tdd_rnbulkcmexample_l13":
                case "3cell_tdd_rnbulkcmexample_l13a":
                    radioTemplateAttrs.put("SectorCarrier ID 1", "1");
                    radioTemplateAttrs.put("SectorEquipmentFunction Id", "S2");
                    radioTemplateAttrs.put("Cell ID 1", "1");
                    radioTemplateAttrs.put("tac", "1");
                    radioTemplateAttrs.put("Cell ID 2", "1");
                    radioTemplateAttrs.put("Cell ID 3", "1");
                    break;
                default:
                    log.warn(String.format(
                            "Invoking : BsimLTEDataProvider.getRadioTemplateAttributesByTemplateName(%s) : invalid template name, not processed", templateName));
                    break;
            }
        }
        return radioTemplateAttrs;
    }

    // BSIM LTE AI tab code

    @Override
    protected LinkedHashMap<String, String> createAifDataOptionAttributes() {

        final LinkedHashMap<String, String> aifDataOptionAttrs = new LinkedHashMap<String, String>();
        aifDataOptionAttrs.put(CSVColumns.AUTO_INTEGRATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.UNLOCK_CELLS, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.UPLOAD_CV_AFTER_PLAN_ACTIVATION, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.UPLOAD_CV_AFTER_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.INSTALL_LICENSE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.ACTIVATE_LICENSE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.WITHOUT_LAPTOP, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_BASIC_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_EQUIPMENT_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_INSTALLATION_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SECURITY, EMPTY_STRING_PLACEHOLDER);

        return aifDataOptionAttrs;
    }

    /**
     * @param templateName
     * @param aifFtpService
     * @param nodeName
     * @param ipAddress
     * @return
     */
    @Override
    protected LinkedHashMap<String, String> getSiteBasicAttributesByTemplateName(final String templateName, final String aifFtpService, final String nodeName) {

        final LinkedHashMap<String, String> siteBasicAttrs = new LinkedHashMap<String, String>();

        switch (templateName) {
            case "SiteBasicExample_L13A":
                siteBasicAttrs.put(CSVColumns.NODE_NAME, nodeName);
                siteBasicAttrs.put(CSVColumns.USER_NAME, "nmsadm");
                siteBasicAttrs.put("FTP Service", aifFtpService);

                // the below are template-specific attributes
                // the ip address should be different between IPv4 and IPv6. As it does
                // not affect the test result, therefore no change here for convenience
                siteBasicAttrs.put("trafficIpAddess", "1.2.3.4");
                break;
            case "SiteBasicExample_L14B":                    // GEO Location Check Template

                // hidden substitution attributes
                siteBasicAttrs.put(CSVColumns.NODE_NAME, nodeName);
                siteBasicAttrs.put(CSVColumns.USER_NAME, "nmsadm");
                siteBasicAttrs.put("FTP Service", aifFtpService);

                // <WantedPosition latitude="%latitude%" longitude="%longitude%" altitude="%altitude%" tolerance="%tolerance%" />
                // <IpAccessHostEt ipAccessHostEtId="1" ipInterfaceMoRef="DU-1-IP-2" ipAddress="%trafficIpAddess%">
                siteBasicAttrs.put("latitude", "2");
                siteBasicAttrs.put("longitude", "2");
                siteBasicAttrs.put("altitude", "2");
                siteBasicAttrs.put("tolerance", "2");
                siteBasicAttrs.put("trafficIpAddess", "2.2.2.2");
                break;
        }

        return siteBasicAttrs;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.data.BsimDataProvider#
     * getSiteEquipmentAttributesByTemplateName(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    /**
     * @param templateName
     * @param aifFtpService
     * @param nodeName
     * @param site
     * @return
     */
    @Override
    protected LinkedHashMap<String, String> getSiteEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName,
            final String site) {

        final LinkedHashMap<String, String> siteEquipmentAttrs = new LinkedHashMap<String, String>();

        siteEquipmentAttrs.put(CSVColumns.NODE_NAME, nodeName);
        siteEquipmentAttrs.put(CSVColumns.USER_NAME, "nmsadm");
        siteEquipmentAttrs.put(CSVColumns.SITE, site);
        siteEquipmentAttrs.put("FTP Service", aifFtpService);

        // the below are template-specific attributes
        siteEquipmentAttrs.put("dlAttenuation1", "1");
        siteEquipmentAttrs.put("ulAttenuation1", "1");
        siteEquipmentAttrs.put("dlTrafficDelay1", "1");
        siteEquipmentAttrs.put("ulTrafficDelay1", "1");
        siteEquipmentAttrs.put("dlAttenuation2", "1");
        siteEquipmentAttrs.put("ulAttenuation2", "1");
        siteEquipmentAttrs.put("dlTrafficDelay2", "1");
        siteEquipmentAttrs.put("ulTrafficDelay2", "1");
        siteEquipmentAttrs.put("dlAttenuation3", "1");
        siteEquipmentAttrs.put("ulAttenuation3", "1");
        siteEquipmentAttrs.put("dlTrafficDelay3", "1");
        siteEquipmentAttrs.put("ulTrafficDelay3", "1");
        siteEquipmentAttrs.put("dlAttenuation4", "1");
        siteEquipmentAttrs.put("ulAttenuation4", "1");
        siteEquipmentAttrs.put("dlTrafficDelay4", "1");
        siteEquipmentAttrs.put("ulTrafficDelay4", "1");
        siteEquipmentAttrs.put("dlAttenuation5", "1");
        siteEquipmentAttrs.put("ulAttenuation5", "1");
        siteEquipmentAttrs.put("dlTrafficDelay5", "1");
        siteEquipmentAttrs.put("ulTrafficDelay5", "1");
        siteEquipmentAttrs.put("dlAttenuation6", "1");
        siteEquipmentAttrs.put("ulAttenuation6", "1");
        siteEquipmentAttrs.put("dlTrafficDelay6", "1");
        siteEquipmentAttrs.put("ulTrafficDelay6", "1");

        return siteEquipmentAttrs;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.data.BsimDataProvider#
     * getSiteInstallationAttributesByTemplateName(java.lang.String,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    /**
     * @param templateName
     * @param aifFtpService
     * @param ipAddress
     * @param nodeName
     * @return
     */
    @Override
    protected LinkedHashMap<String, String> getSiteInstallationAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName) {

        final LinkedHashMap<String, String> siteInstallationAttrs = new LinkedHashMap<String, String>();

        siteInstallationAttrs.put(CSVColumns.NODE_NAME, nodeName);
        siteInstallationAttrs.put(CSVColumns.USER_NAME, "nmsadm");
        siteInstallationAttrs.put("FTP Service", aifFtpService);

        siteInstallationAttrs.put("Save as", String.format("/home/nmsadm/%1$sSiteInstall.xml", nodeName));
        // the ip address should be different between IPv4 and IPv6. As it does
        // not affect the test result, therefore no change here for convenience
        siteInstallationAttrs.put("Integration OaM IP Address", "1.2.3.4");

        return siteInstallationAttrs;
    }

    @Override
    protected LinkedHashMap<String, String> getCabinetEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName) {
        // Implemented from Interface - Not required for LTE
        return null;
    }

    @Override
    public boolean isValidNodeType(final String s) {

        if (s.equals(NodeType.MICRO_LTE.toString()) || s.equals(NodeType.LTE.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected NodeType getNodeType() {

        return null;
    }

}


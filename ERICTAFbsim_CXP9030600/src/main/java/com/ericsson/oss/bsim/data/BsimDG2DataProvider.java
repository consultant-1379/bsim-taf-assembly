package com.ericsson.oss.bsim.data;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.oss.bsim.data.model.NodeType;

public class BsimDG2DataProvider extends BsimDataProvider {

    private static Logger log = Logger.getLogger(BsimDG2DataProvider.class);

    public static final String DATA_FILE = DataHandler.getAttribute("dataprovider.ADD_DG2.location").toString();

    /* public static final String DATA_FILE_QUICK = DataHandler.getAttribute("dataprovider.ADD_DG2_QUICK_FEEDBACK.location").toString(); */

    public List<Object[]> getTestDataList() {
        final String dataFileSelected = selectDataFile();
        log.info("CSV File chosen ==> " + dataFileSelected);
        initialization(dataFileSelected);
        return generateTestDataForAddNode();
    }

    /** check in */
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
     * @return addNodeDataObjectAttrs - HashMap : DG2 Node Data Objects
     */
    @Override
    protected LinkedHashMap<String, String> createAddNodeDataBasicAttrs(final String templateName) {
        log.info(String.format("Invoking : BsimDG2DataProvider.createAddNodeDataBasicAttrs(%s)", templateName));

        final LinkedHashMap<String, String> addNodeDataObjectAttrs = new LinkedHashMap<String, String>();
        addNodeDataObjectAttrs.put(CSVColumns.OSS_NODE_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.PLAN_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.ENABLE_PCI_ASSIGNMENT, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.PCI_PROFILE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.DG2_Domain, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_VERSION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.MIM_VERSION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.UPGRADE_PACKAGE, DG2_UPGRADE_PATH);

        addNodeDataObjectAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_TYPE, EMPTY_STRING_PLACEHOLDER);             // Ja Added extra Column

        addNodeDataObjectAttrs.put(CSVColumns.IMPORT_TRANSPORT_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.USE_TRANSPORT_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.TRANSPORT_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);

        addNodeDataObjectAttrs.put(CSVColumns.IMPORT_RADIO_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.USE_RADIO_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.RADIO_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);

        // Adding CSV Columns(bsc) for GRAN
        addNodeDataObjectAttrs.put(CSVColumns.Bsc_Name, EMPTY_STRING_PLACEHOLDER);
	addNodeDataObjectAttrs.put(CSVColumns.OSS_NODE_PROTOCOL_TEMPLATE, EMPTY_STRING_PLACEHOLDER);

        return addNodeDataObjectAttrs;
    }

    /**
     * @param templateName
     *        - String
     * @return nodeTemplateAttrs - HashMap of Node Template Attributes
     */
    @Override
    public LinkedHashMap<String, String> createNodeTemplateAttributes(final String templateName) {

        log.info(String.format("Invoking : BsimDG2DataProvider.createNodeTemplateAttributes(%s)", templateName));

        final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();

        // SubNetwork Group
        nodeTemplateAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
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
        // MIM Version
        nodeTemplateAttrs.put(CSVColumns.MIM_VERSION, EMPTY_STRING_PLACEHOLDER);
        // Site
        nodeTemplateAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
        // Location
        nodeTemplateAttrs.put(CSVColumns.LOCATION, EMPTY_STRING_PLACEHOLDER);
        // worldTimeZoneId
        nodeTemplateAttrs.put(CSVColumns.WORLD_TIME_ZONE_ID, EMPTY_STRING_PLACEHOLDER);
        // nodeVersion
        nodeTemplateAttrs.put(CSVColumns.NODE_VERSION, EMPTY_STRING_PLACEHOLDER);
        // rnc association
        nodeTemplateAttrs.put(CSVColumns.ASSOCIATED_RNC, EMPTY_STRING_PLACEHOLDER);
        // User Name
        nodeTemplateAttrs.put("User Name", "nmsadm");
        // BSC FOR GRAN
        nodeTemplateAttrs.put(CSVColumns.Bsc_Name, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.Associated_bsc, EMPTY_STRING_PLACEHOLDER);
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



  protected static LinkedHashMap<String, Object> getOssNodeProtocolTemplateAttributes() {

        final LinkedHashMap<String, Object> ossNodeTemplateAttrs = new LinkedHashMap<String, Object>();

        ossNodeTemplateAttrs.put("subjectName", "subject");
        ossNodeTemplateAttrs.put("subjectAltName", "altNameDummy");
        ossNodeTemplateAttrs.put("ldapIpAddress", "12.12.13.24");
        ossNodeTemplateAttrs.put("fallbackLdapIpAddress", "123.2.3.4");
        ossNodeTemplateAttrs.put("bindDn", "1");
        ossNodeTemplateAttrs.put("password", "1");
        ossNodeTemplateAttrs.put("baseDn", "1");
        ossNodeTemplateAttrs.put("address", "120.23.3.4");

        return ossNodeTemplateAttrs;
    }

    // BSIM LTE AI tab code

    @Override
    protected LinkedHashMap<String, String> createAifDataOptionAttributes() {

        final LinkedHashMap<String, String> aifDataOptionAttrs = new LinkedHashMap<String, String>();
        aifDataOptionAttrs.put(CSVColumns.AUTO_INTEGRATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.UNLOCK_CELLS, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.INSTALL_LICENSE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.ACTIVATE_LICENSE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_BASIC_FILE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_EQUIPMENT_FILE, EMPTY_STRING_PLACEHOLDER);
       // aifDataOptionAttrs.put(CSVColumns.OSS_NODE_PROTOCOL_FILE, EMPTY_STRING_PLACEHOLDER);
	aifDataOptionAttrs.put(CSVColumns.OSS_NODE_PROTOCOL_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_INSTALLATION_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.is_Using_OssNodeProtocolFile, EMPTY_STRING_PLACEHOLDER);

        return aifDataOptionAttrs;
    }

    @Override
    protected LinkedHashMap<String, String> getSiteBasicAttributesByTemplateName(final String templateName, final String aifFtpService, final String nodeName) {

        return null;
    }

    @Override
    protected LinkedHashMap<String, String> getSiteEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName,
            final String site) {
        return null;
    }

    @Override
    protected LinkedHashMap<String, String> getCabinetEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName) {
        // Implemented from Interface - Not required for LTE
        return null;
    }

    /**
     * @param templateName
     * @param aifFtpService
     * @param nodeName
     * @param ipAddress
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
    public boolean isValidNodeType(final String s) {

        if (s.equals(NodeType.DG2.toString()) || s.equals(NodeType.BasebandT.toString())) {
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

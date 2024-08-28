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
 * @author xmaakui
 */
public class BsimMicroMacroLTEDataProvider_KGB extends BsimDataProvider {
    private static Logger log = Logger.getLogger(BsimMicroMacroLTEDataProvider.class);

    public static final String DATA_FILE = DataHandler.getAttribute("dataprovider.ADDMicroMacroLTE_KGB.location").toString();

    public List<Object[]> getTestDataList() {

        initialization(DATA_FILE);
        return generateTestDataForAddNode();
    }

    /**
     * @param templateName
     *        - String
     * @return addNodeDataObjectAttrs - HashMap : LTE Node Data Objects
     */
    @Override
    protected LinkedHashMap<String, String> createAddNodeDataBasicAttrs(final String templateName) {

        log.info(String.format("Invoking : BsimMicroMacroDataProvider_KGB.createAddNodeDataBasicAttrs(%s)", templateName));

        final LinkedHashMap<String, String> addNodeDataObjectAttrs = new LinkedHashMap<String, String>();
        addNodeDataObjectAttrs.put(CSVColumns.OSS_NODE_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.PLAN_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.ENABLE_PCI_ASSIGNMENT, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.PCI_PROFILE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.MIM_VERSION, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);

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

        log.info(String.format("Invoking : BsimMicroMacroDataProvider_KGB.createNodeTemplateAttributes(%s)", templateName));

        final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();

        nodeTemplateAttrs.put(CSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.IP_ADDRESS, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.FTP_BACKUP_STORE, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.FTP_SW_STORE, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.FTP_LICENSE_KEY, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.FTP_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.MIM_VERSION, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.LOCATION, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.WORLD_TIME_ZONE_ID, EMPTY_STRING_PLACEHOLDER);

        nodeTemplateAttrs.put(CSVColumns.ASSOCIATED_STN, EMPTY_STRING_PLACEHOLDER);

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
            log.error("Invoking : BsimMicroMacroDataProvider_KGB.getTransportTemplateAttributesByTemplateName : No template received");
        } else {
            log.info(String.format("Invoking : BsimMicroMacroDataProvider_KGB.getTransportTemplateAttributesByTemplateName(%s)", templateName));

            transportTemplateAttrs.put(CSVColumns.SUBNETWORK_GROUP, subNetwork);
            transportTemplateAttrs.put(CSVColumns.NODE_NAME, nodeName);
            transportTemplateAttrs.put(CSVColumns.PLAN_NAME, planName);
            transportTemplateAttrs.put("User Name", "nmsadm");

            switch (templateName.toLowerCase()) {
                case "tnbulkcmexample":
                    transportTemplateAttrs.put("eNB ID", "355");
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
                    log.error(String
                            .format("Invoking : BsimMicroMacroDataProvider_KGB.getTransportTemplateAttributesByTemplateName(%s) : invalid template name, not processed",
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
            log.error("Invoking : BsimMicroMacroDataProvider_KGB.getRadioTemplateAttributesByTemplateName : No template received");
        } else {
            log.info(String.format("Invoking : BsimMicroMacroDataProvider_KGB.getRadioTemplateAttributesByTemplateName(%s)", templateName));
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
                case "1cell_fdd_rnbulkcmexample":
                case "1cell_tdd_rnbulkcmexample_l13":
                case "1cell_tdd_rnbulkcmexample":
                    radioTemplateAttrs.put("SectorCarrier ID 1", "1");
                    radioTemplateAttrs.put("SectorEquipmentFunction Id", "S2");
                    radioTemplateAttrs.put("Cell ID 1", "1");
                    radioTemplateAttrs.put("tac", "1234");
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
                    log.error(String.format(
                            "Invoking : BsimMicroMacroDataProvider_KGB.getRadioTemplateAttributesByTemplateName(%s) : invalid template name, not processed",
                            templateName));
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
        aifDataOptionAttrs.put(CSVColumns.NETWORK_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);

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

        if (templateName == null || templateName == "") {
            log.error("Invoking : BsimMicroMacroDataProvider_KGB.getSiteBasicAttributesByTemplateName : No template received");
        } else {
            log.info(String.format("Invoking : BsimMicroMacroDataProvider_KGB.getSiteBasicAttributesByTemplateName(%s)", templateName));

            switch (templateName.toLowerCase()) {

                case "sitebasicexampleipsec_mrbs":
                    siteBasicAttrs.put(CSVColumns.NODE_NAME, nodeName);
                    // siteBasicAttrs.put(CSVColumns.USER_NAME, "nmsadm");
                    siteBasicAttrs.put("FTP Service", aifFtpService);
                    siteBasicAttrs.put("ntpServerAddressThird", "1.2.3.4");
                    siteBasicAttrs.put("ntpServerAddressSecondary", "1.2.3.4");
                    siteBasicAttrs.put("ntpServerAddressPrimary", "1.2.3.4");
                    siteBasicAttrs.put("ipVersion", "IP_V4");
                    siteBasicAttrs.put("ntpServerIpAddress_1", "1.2.3.4");
                    siteBasicAttrs.put("ntpServerIpAddress_2", "1.2.3.4");
                    siteBasicAttrs.put("peerIpAddress", "1.2.3.4");
                    siteBasicAttrs.put("PeerOaMIpAddress", "1.2.3.4");

                    // Hidden Value nodeIdType that needs to be set
                    siteBasicAttrs.put("nodeIdType", "FDN");
                    siteBasicAttrs.put("nodeIdValue", "SubNetwork=ONRM_ROOT_MO_R");
                    break;
                case "sitebasicexample":
                    siteBasicAttrs.put(CSVColumns.NODE_NAME, nodeName);
                    siteBasicAttrs.put(CSVColumns.USER_NAME, "nmsadm");
                    siteBasicAttrs.put("FTP Service", aifFtpService);
                    siteBasicAttrs.put("latitude", "2");
                    siteBasicAttrs.put("longitude", "2");
                    siteBasicAttrs.put("altitude", "2");
                    siteBasicAttrs.put("tolerance", "2");
                    siteBasicAttrs.put("trafficIpAddess", "2.2.2.2");
                    // Hidden Value nodeIdType that needs to be set
                    // siteBasicAttrs.put("nodeIdType", "FDN");
                    // siteBasicAttrs.put("nodeIdValue", "SubNetwork=ONRM_ROOT_MO_R");
                    break;
                case "sitebasicexample_l13a":
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

                default:
                    log.error(String.format(
                            "Invoking : BsimMicroMacroDataProvider_KGB.getSiteBasicAttributesByTemplateName(%s) : invalid template name, not processed",
                            templateName));
                    break;
            }
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

        if (templateName == null || templateName == "") {
            log.error("Invoking : BsimMicroMacroDataProvider_KGB.getSiteEquipmentAttributesByTemplateName : No template received");
        } else {
            log.info(String.format("Invoking : BsimMicroMacroDataProvider_KGB.getSiteEquipmentAttributesByTemplateName(%s)", templateName));
            siteEquipmentAttrs.put(CSVColumns.NODE_NAME, nodeName);
            siteEquipmentAttrs.put(CSVColumns.USER_NAME, "nmsadm");
            siteEquipmentAttrs.put(CSVColumns.SITE, site);
            siteEquipmentAttrs.put("FTP Service", aifFtpService);

            switch (templateName.toLowerCase()) {

                case "siteequipment_3sectordualtx_l13a":

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
                    break;
                case "siteequipmentexample_3sectormimo":
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
                    siteEquipmentAttrs.put("auMechanicalAntennaTilt1", "1");
                    siteEquipmentAttrs.put("auMechanicalAntennaTilt2", "1");
                    siteEquipmentAttrs.put("tmaDlAttenuation1", "1");
                    siteEquipmentAttrs.put("tmaDlTrafficDelay1", "1");
                    siteEquipmentAttrs.put("tmaUlTrafficDelay1", "1");
                    siteEquipmentAttrs.put("tmaDlAttenuation2", "1");
                    siteEquipmentAttrs.put("tmaDlTrafficDelay2", "1");
                    siteEquipmentAttrs.put("tmaUlTrafficDelay2", "1");
                    siteEquipmentAttrs.put("retElectricalAntennaTilt1", "1");
                    // siteEquipmentAttrs.put("retMechanicalAntennaTilt2", "1");
                    siteEquipmentAttrs.put("tmaDlAttenuation3", "1");
                    siteEquipmentAttrs.put("tmaDlTrafficDelay3", "1");
                    siteEquipmentAttrs.put("tmaUlTrafficDelay3", "1");
                    siteEquipmentAttrs.put("tmaDlAttenuation4", "1");
                    siteEquipmentAttrs.put("tmaDlTrafficDelay4", "1");
                    siteEquipmentAttrs.put("tmaUlTrafficDelay4", "1");
                    siteEquipmentAttrs.put("auMechanicalAntennaTilt3", "1");
                    siteEquipmentAttrs.put("tmaDlAttenuation5", "1");
                    siteEquipmentAttrs.put("tmaDlTrafficDelay5", "1");
                    siteEquipmentAttrs.put("tmaUlTrafficDelay5", "1");
                    siteEquipmentAttrs.put("tmaDlAttenuation6", "1");
                    siteEquipmentAttrs.put("tmaUlTrafficDelay6", "1");
                    siteEquipmentAttrs.put("retElectricalAntennaTilt3", "1");
                    siteEquipmentAttrs.put("tmaDlTrafficDelay6", "1");
                    break;
                default:
                    log.error(String.format(
                            "Invoking : BsimMicroMacroDataProvider_KGB.getSiteEquipmentAttributesByTemplateName(%s) : invalid template name, not processed",
                            templateName));
                    break;
            }
        }
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

        if (templateName == null || templateName == "") {
            log.error("Invoking : BsimMicroMacroDataProvider_KGB.getSiteInstallationAttributesByTemplateName : No template received");
        } else {
            log.info(String.format("Invoking : BsimMicroMacroDataProvider_KGB.getSiteInstallationAttributesByTemplateName(%s)", templateName));

            switch (templateName.toLowerCase()) {

                case "siteinstall_micro":

                    siteInstallationAttrs.put(CSVColumns.NODE_NAME, nodeName);
                    siteInstallationAttrs.put(CSVColumns.USER_NAME, "nmsadm");
                    siteInstallationAttrs.put("FTP Service", aifFtpService);
                    siteInstallationAttrs.put("Save as", String.format("/home/nmsadm/%1$sSiteInstall.xml", nodeName));
                    siteInstallationAttrs.put("vlan Id", "1");
                    siteInstallationAttrs.put("Outer Ip Address", "1.2.3.4");
                    siteInstallationAttrs.put("subnetMask", "255.255.255.0");
                    siteInstallationAttrs.put("defaultRouter0", "1.2.3.4");
                    siteInstallationAttrs.put("Outer Dns Server Address", "1.2.3.4");
                    siteInstallationAttrs.put("Scurity Gateway", "192.1.3.4");
                    siteInstallationAttrs.put("Inner Ip Address", "1.2.3.5");
                    siteInstallationAttrs.put("Inner Dns Server Address", "12.12.3.4");
                    break;

                case "siteinstallexampleipsec_mrbs":

                    siteInstallationAttrs.put(CSVColumns.NODE_NAME, nodeName);
                    siteInstallationAttrs.put(CSVColumns.USER_NAME, "nmsadm");
                    siteInstallationAttrs.put("Save as", String.format("/home/nmsadm/%1$sSiteInstall.xml", nodeName));
                    siteInstallationAttrs.put("FTP Service", aifFtpService);
                    siteInstallationAttrs.put("vLan Id", "1");
                    siteInstallationAttrs.put("Outer Ip Address", "1.2.3.4");
                    siteInstallationAttrs.put("Subnet Mask", "1.2.3.4");
                    siteInstallationAttrs.put("Default Router0 IP Address", "1.2.3.4");
                    siteInstallationAttrs.put("Outer Dns Server Address", "1.2.3.4");
                    siteInstallationAttrs.put("Security Gateway", "1.2.3.4");
                    siteInstallationAttrs.put("Inner Ip Address", "1.2.3.4");
                    siteInstallationAttrs.put("Inner Dns Server Address", "1.2.3.4");
                    break;

                case "siteinstallexample_l13a":
                case "siteinstallexample_l12a":
                    siteInstallationAttrs.put(CSVColumns.NODE_NAME, nodeName);
                    siteInstallationAttrs.put(CSVColumns.USER_NAME, "nmsadm");
                    siteInstallationAttrs.put("FTP Service", aifFtpService);
                    siteInstallationAttrs.put("Save as", String.format("/home/nmsadm/%1$sSiteInstall.xml", nodeName));
                    siteInstallationAttrs.put("Integration OaM IP Address", "1.2.3.4");
                    break;
                default:
                    log.error(String.format(
                            "Invoking : BsimMicroMacroDataProvider_KGB.getSiteInstallationAttributesByTemplateName(%s) : invalid template name, not processed",
                            templateName));
                    break;
            }
        }
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

        if (s.equals(NodeType.MICRO_MACRO_LTE.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected NodeType getNodeType() {
        return NodeType.MICRO_LTE;
    }

}



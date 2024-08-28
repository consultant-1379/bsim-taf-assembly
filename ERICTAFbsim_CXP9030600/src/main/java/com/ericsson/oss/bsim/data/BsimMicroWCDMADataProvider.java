/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.data;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

/**
 * Data Provider for WCDMA Micro Test Case
 * 
 * @author efitrob
 */
public class BsimMicroWCDMADataProvider extends BsimDataProvider {

    private static Logger log = Logger.getLogger(BsimMicroWCDMADataProvider.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static String SEG_MASTER_SERVICE = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS ";

    private static String CHECK_LAST_UTRANCELL = "lt UtranCell | tail -1";

    public static final String DATA_FILE = DataHandler.getAttribute("dataprovider.ADD_MICRO_WCDMA.location").toString();

    /**
     * @return
     */
    public List<Object[]> getTestDataList() {

        initialization(DATA_FILE);
        return generateTestDataForAddNode();
    }

    @Override
    protected LinkedHashMap<String, String> createAddNodeDataBasicAttrs(final String templateName) {

        final LinkedHashMap<String, String> addNodeDataObjectAttrs = new LinkedHashMap<String, String>();

        log.info("Invoking BsimMicroWCDMADataProvider.createAddNodeDataBasicAttrs() templateName == " + templateName);

        addNodeDataObjectAttrs.put(CSVColumns.OSS_NODE_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.SC_PROFILE, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
        addNodeDataObjectAttrs.put(CSVColumns.PLAN_NAME, EMPTY_STRING_PLACEHOLDER);
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

    @Override
    protected LinkedHashMap<String, String> createNodeTemplateAttributes(final String templateName) {

        final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();
        final LinkedHashMap<String, String> nodeTemplateAttrsAdditionalAttributes = new LinkedHashMap<String, String>();

        log.info("Invoking BsimMicroWCDMADataProvider.createNodeTemplateAttributes() templateName == " + templateName);

        if (templateName.equals("AddWcdmaNodeandSiteWithSTNExample") || templateName.equals("AddWcdmaNodeWithSTNExample")
                || templateName.equals("AddWcdmaNodeandSiteExample") || templateName.equals("AddWcdmaNodeExample")) {

            nodeTemplateAttrs.put(CSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.IP_ADDRESS, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.FTP_BACKUP_STORE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.FTP_SW_STORE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.FTP_CONFIG_STORE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.FTP_LICENSE_KEY, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.FTP_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.MIM_VERSION, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(CSVColumns.USER_NAME, "nmsadm");

        }

        if (templateName.equals("AddWcdmaNodeandSiteWithSTNExample")) {

            nodeTemplateAttrsAdditionalAttributes.putAll(nodeTemplateAttrs);
            nodeTemplateAttrsAdditionalAttributes.put(CSVColumns.ASSOCIATED_STN, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrsAdditionalAttributes.put(CSVColumns.LOCATION, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrsAdditionalAttributes.put(CSVColumns.WORLD_TIME_ZONE_ID, EMPTY_STRING_PLACEHOLDER);

        }
        if (templateName.equals("AddWcdmaNodeWithSTNExample")) {

            nodeTemplateAttrsAdditionalAttributes.putAll(nodeTemplateAttrs);
            nodeTemplateAttrsAdditionalAttributes.put(CSVColumns.ASSOCIATED_STN, EMPTY_STRING_PLACEHOLDER);

        }
        if (templateName.equals("AddWcdmaNodeandSiteExample")) {

            nodeTemplateAttrsAdditionalAttributes.putAll(nodeTemplateAttrs);
            nodeTemplateAttrsAdditionalAttributes.put(CSVColumns.LOCATION, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrsAdditionalAttributes.put(CSVColumns.WORLD_TIME_ZONE_ID, EMPTY_STRING_PLACEHOLDER);

        }

        if (templateName.equals("AddWcdmaNodeExample")) {
            return nodeTemplateAttrs;
        } else {
            return nodeTemplateAttrsAdditionalAttributes;
        }

    }

    @Override
    protected LinkedHashMap<String, String> getTransportTemplateAttributesByTemplateName(
            final String templateName,
            final String rnc,
            final String nodeName,
            final String planName) {

        log.info("Invoking BsimMicroWCDMADataProvider.getTransportTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();
        transportTemplateAttrs.put(CSVColumns.RNC_NAME, rnc);
        transportTemplateAttrs.put(CSVColumns.NODE_NAME, nodeName);
        transportTemplateAttrs.put(CSVColumns.PLAN_NAME, planName);
        transportTemplateAttrs.put(CSVColumns.USER_NAME, "nmsadm");

        if (templateName == null || templateName == "") {
            log.error("ERROR:  BsimMicroWCDMADataProvider.getTransportTemplateAttributesByTemplateName() templateName is Null");
        } else {

            switch (templateName.toLowerCase()) {
                case "wcdmatnbulkcmtemplatefile":
                    transportTemplateAttrs.put("IUB ID", "1");
                    transportTemplateAttrs.put("rbsId", "1");
                    transportTemplateAttrs.put("remoteCpIpAddress1", "88.88.88.88");
                    transportTemplateAttrs.put("IpAccessHostPool", "77.78.78.87");
                case "wcdmarnctnbulkcmexample":
                    transportTemplateAttrs.put("IUB ID", "1");
                    transportTemplateAttrs.put("rbsId", "1");
                    transportTemplateAttrs.put("remoteCpIpAddress1", "88.88.88.88");
                    break;
                default:
                    log.error("ERROR: BsimMicroWCDMADataProvider.getTransportTemplateAttributesByTemplateName() invalid templateName received " + templateName);
                    break;
            }
        }

        return transportTemplateAttrs;
    }

    @Override
    protected LinkedHashMap<String, String> getRadioTemplateAttributesByTemplateName(
            final String templateName,
            final String rnc,
            final String nodeName,
            final String planName) {

        log.info("Invoking BsimMicroWCDMADataProvider.getRadioTemplateAttributesByTemplateName() templateName == " + templateName);

        final LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();
        radioTemplateAttrs.put(CSVColumns.RNC_NAME, rnc);
        radioTemplateAttrs.put(CSVColumns.NODE_NAME, nodeName);
        radioTemplateAttrs.put(CSVColumns.PLAN_NAME, planName);
        radioTemplateAttrs.put(CSVColumns.USER_NAME, "nmsadm");

        if (templateName == null) {
            log.error("ERROR:  BsimMicroWCDMADataProvider.getRadioTemplateAttributesByTemplateName() templateName is Null");
        } else {
            switch (templateName.toLowerCase()) {
                case "wcdmarnbulkcmtemplatefile":
                    radioTemplateAttrs.put("Cell 1: localCellId", "11");
                    radioTemplateAttrs.put("Cell 1: uarfcnUl", "1");
                    radioTemplateAttrs.put("Cell 1: uarfcnDl", "1");
                    radioTemplateAttrs.put("Cell 1: primaryScramblingCode", "1");
                    radioTemplateAttrs.put("Cell 1: cId", getNextAvailableCidValue(1));
                    radioTemplateAttrs.put("Cell 1: User Label", "1");
                    radioTemplateAttrs.put("Cell 1: lac", "1");
                    radioTemplateAttrs.put("Cell 1: sac", "1");
                    radioTemplateAttrs.put("IUB ID", "1");
                    radioTemplateAttrs.put("Cell 1: tCell", "1");
                    radioTemplateAttrs.put("Cell 1: sib1PlmnScopeValueTag", "1");
                    radioTemplateAttrs.put("Cell 2: localCellId", "12");
                    radioTemplateAttrs.put("Cell 2: uarfcnUl", "1");
                    radioTemplateAttrs.put("Cell 2: uarfcnDl", "1");
                    radioTemplateAttrs.put("Cell 2: primaryScramblingCode", "1");
                    radioTemplateAttrs.put("Cell 2: cId", getNextAvailableCidValue(2));
                    radioTemplateAttrs.put("Cell 2: User Label", "1");
                    radioTemplateAttrs.put("Cell 2: lac", "1");
                    radioTemplateAttrs.put("Cell 2: sac", "1");
                    radioTemplateAttrs.put("Cell 2: tCell", "1");
                    radioTemplateAttrs.put("Cell 2: sib1PlmnScopeValueTag", "1");
                    radioTemplateAttrs.put("Cell 3: localCellId", "13");
                    radioTemplateAttrs.put("Cell 3: uarfcnUl", "1");
                    radioTemplateAttrs.put("Cell 3: uarfcnDl", "1");
                    radioTemplateAttrs.put("Cell 3: primaryScramblingCode", "1");
                    radioTemplateAttrs.put("Cell 3: cId", getNextAvailableCidValue(3));
                    radioTemplateAttrs.put("Cell 3: User Label", "1");
                    radioTemplateAttrs.put("Cell 3: lac", "1");
                    radioTemplateAttrs.put("Cell 3: sac", "1");
                    radioTemplateAttrs.put("Cell 3: tCell", "1");
                    radioTemplateAttrs.put("Cell 3: sib1PlmnScopeValueTag", "1");
                    break;
                case "1cellwcdmarnbulkcmexample":
                    radioTemplateAttrs.put("Cell 1: localCellId", "1");
                    radioTemplateAttrs.put("Cell 1: uarfcnUl", "1");
                    radioTemplateAttrs.put("Cell 1: uarfcnDl", "1");
                    radioTemplateAttrs.put("Cell 1: primaryScramblingCode", "1");
                    radioTemplateAttrs.put("Cell 1: cId", "1");
                    radioTemplateAttrs.put("Cell 1: User Label", "1");
                    radioTemplateAttrs.put("Cell 1: lac", "1");
                    radioTemplateAttrs.put("Cell 1: sac", "1");
                    radioTemplateAttrs.put("IUB ID", "1");
                    radioTemplateAttrs.put("Cell 1: tCell", "1");
                    radioTemplateAttrs.put("Cell 1: sib1PlmnScopeValueTag", "1");
                    break;
                default:
                    log.error("ERROR: BsimMicroWCDMADataProvider.getTransportTemplateAttributesByTemplateName() invalid templateName received " + templateName);
                    break;
            }
        }
        return radioTemplateAttrs;
    }

    @Override
    protected LinkedHashMap<String, String> getSiteBasicAttributesByTemplateName(final String templateName, final String aifFtpService, final String nodeName) {

        log.info("Invoking BsimMicroWCDMADataProvider.getSiteBasicAttributesByTemplateName() templateName == " + templateName);

        final LinkedHashMap<String, String> siteBasicAttrs = new LinkedHashMap<String, String>();
        siteBasicAttrs.put(CSVColumns.NODE_NAME, nodeName);
        siteBasicAttrs.put(CSVColumns.USER_NAME, "nmsadm");

        if (templateName == null) {
            log.error("ERROR:  BsimMicroWCDMADataProvider.getSiteBasicAttributesByTemplateName() templateName is Null");
        } else {
            switch (templateName.toLowerCase()) {
                case "sitebasic_wcdma":
                    siteBasicAttrs.put("FTP Service", aifFtpService);
                    siteBasicAttrs.put("Local Directory", "");
                    siteBasicAttrs.put("Store File Locally", "");
                    break;
                case "sitebasic_mrbs":
                    siteBasicAttrs.put("FTP Service", aifFtpService);
                    siteBasicAttrs.put("Local Directory", "");
                    siteBasicAttrs.put("Store File Locally", "");
                    siteBasicAttrs.put("IP Eth", "1");
                    siteBasicAttrs.put("Iub gateway", "1");
                    siteBasicAttrs.put("Iub IP", "1");
                    siteBasicAttrs.put("Iub Subnet mask", "1");
                    siteBasicAttrs.put("Iub VLAN", "1");
                    siteBasicAttrs.put("IpSyncRef", "1");
                    siteBasicAttrs.put("Oam default gateway", "1");
                    siteBasicAttrs.put("OamIp Address", "1");
                    siteBasicAttrs.put("oamSubnetMask", "1");
                    siteBasicAttrs.put("oamVid", "1");
                    siteBasicAttrs.put("defaultDomainName", "1");
                    siteBasicAttrs.put("dhcpServerPrimaryIpAddress", "1");
                    siteBasicAttrs.put("dnsServerIpAddress", "1");
                    siteBasicAttrs.put("documentServerWebAddress", "1");
                    siteBasicAttrs.put("primaryNtpServerIpAddress", "1");
                    siteBasicAttrs.put("secondaryNtpServerIpAddress", "1");
                    siteBasicAttrs.put("ossCorbaNameServer", "1");
                    siteBasicAttrs.put("ossCorbaNameServerPort", "1");
                    siteBasicAttrs.put("hopIpAddress", "1");
                    siteBasicAttrs.put("routeMetric", "1");
                    break;
                default:
                    log.error("ERROR: BsimMicroWCDMADataProvider.getSiteBasicAttributesByTemplateName() invalid templateName received " + templateName);
                    break;
            }
        }

        return siteBasicAttrs;
    }

    @Override
    protected LinkedHashMap<String, String> getSiteEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            final String nodeName,
            final String site) {

        log.info("Invoking BsimMicroWCDMADataProvider.getSiteEquipmentAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> siteEquipmentAttrs = new LinkedHashMap<String, String>();

        siteEquipmentAttrs.put(CSVColumns.NODE_NAME, nodeName);
        siteEquipmentAttrs.put(CSVColumns.USER_NAME, "nmsadm");

        if (templateName == null) {
            log.error("ERROR:  BsimMicroWCDMADataProvider.getSiteEquipmentAttributesByTemplateName() templateName is Null");
        } else {
            switch (templateName.toLowerCase()) {
                case "siteequipment_wcdma":
                    siteEquipmentAttrs.put("FTP Service", aifFtpService);
                    siteEquipmentAttrs.put("Local Directory", "");
                    siteEquipmentAttrs.put("Store File Locally", "");
                    break;
                case "siteequipment_mrbs":
                    siteEquipmentAttrs.put("Local Directory", "");
                    siteEquipmentAttrs.put("Store File Locally", "");
                    siteEquipmentAttrs.put("FTP Service", aifFtpService);
                    siteEquipmentAttrs.put("siteName", site);
                    siteEquipmentAttrs.put("logicalName", "Logical_Name");
                    siteEquipmentAttrs.put("lmtPorts", "1");
                    siteEquipmentAttrs.put("fqBandHighEdgeBranchA", "1");
                    siteEquipmentAttrs.put("fqBandLowEdgeBranchA", "1");
                    siteEquipmentAttrs.put("fqBandHighEdgeBranchB", "1");
                    siteEquipmentAttrs.put("fqBandLowEdgeBranchB", "1");
                    siteEquipmentAttrs.put("latitude", "1");
                    siteEquipmentAttrs.put("longitud", "1");
                    siteEquipmentAttrs.put("altitude", "1");
                    siteEquipmentAttrs.put("tolerance", "1");
                    break;

                /*
                 * <SubstitutionAttribute name="Local Directory" value=""/>
                 * <SubstitutionAttribute name="Store File Locally" value=""/>
                 * <SubstitutionAttribute name="FTP Service"
                 * value="SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-WRAN-nedssv4,FtpService=aifwran"/>
                 * <SubstitutionAttribute name="siteName" value="kerry"/>
                 * <SubstitutionAttribute name="logicalName" value="1234"/>
                 * <SubstitutionAttribute name="lmtPorts" value="1"/>
                 * <SubstitutionAttribute name="fqBandHighEdgeBranchA" value="1"/>
                 * <SubstitutionAttribute name="fqBandLowEdgeBranchA" value="1"/>
                 * <SubstitutionAttribute name="fqBandHighEdgeBranchB" value="1"/>
                 * <SubstitutionAttribute name="fqBandLowEdgeBranchB" value="1"/>
                 * <SubstitutionAttribute name="latitude" value="1"/>
                 * <SubstitutionAttribute name="longitud" value="1"/>
                 * <SubstitutionAttribute name="altitude" value="1"/>
                 * <SubstitutionAttribute name="tolerance" value="1"/>
                 */
                default:
                    log.error("ERROR: BsimMicroWCDMADataProvider.getSiteEquipmentAttributesByTemplateName() invalid templateName received " + templateName);
                    break;
            }
        }

        return siteEquipmentAttrs;
    }

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

        log.info("Invoking BsimMicroWCDMADataProvider.getSiteInstallationAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> siteInstallationAttrs = new LinkedHashMap<String, String>();

        siteInstallationAttrs.put(CSVColumns.NODE_NAME, nodeName);
        siteInstallationAttrs.put(CSVColumns.USER_NAME, "nmsadm");

        if (templateName == null) {
            log.error("ERROR:  BsimMicroWCDMADataProvider.getSiteInstallationAttributesByTemplateName() templateName is Null");
        } else {
            switch (templateName.toLowerCase()) {
                case "siteinstallexampleipsec_mrbs":
                    siteInstallationAttrs.put("NODE_VERSION", "W15.1");
                    siteInstallationAttrs.put("Save as", String.format("/home/nmsadm/%1$sSiteInstall.xml", nodeName));
                    siteInstallationAttrs.put("FTP Service", aifFtpService);
                    siteInstallationAttrs.put("vLan Id", "1");
                    siteInstallationAttrs.put("rbsIntegrationCode", "null");
                    siteInstallationAttrs.put("Outer Ip Address", "1.2.3.4");
                    siteInstallationAttrs.put("Subnet Mask", "1.2.3.4");
                    siteInstallationAttrs.put("Default Router0 IP Address", "1.2.3.4");
                    siteInstallationAttrs.put("Outer Dns Server Address", "1.2.3.4");
                    siteInstallationAttrs.put("Security Gateway", "1.2.3.4");
                    siteInstallationAttrs.put("Inner Ip Address", "1.2.3.4");
                    siteInstallationAttrs.put("Inner Dns Server Address", "1.2.3.4");
                    siteInstallationAttrs.put("AIF_UserName", nodeName + "_mRBS_WRAN");
                    siteInstallationAttrs.put("AIF_Password", "null");

                    break;
                default:
                    log.error("ERROR: BsimMicroWCDMADataProvider.getSiteInstallationAttributesByTemplateName() invalid templateName received " + templateName);
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

        log.info("Invoking BsimMicroWCDMADataProvider.getCabinetEquipmentAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> cabinetEquipmentAttrs = new LinkedHashMap<String, String>();

        cabinetEquipmentAttrs.put(CSVColumns.NODE_NAME, nodeName);
        cabinetEquipmentAttrs.put(CSVColumns.USER_NAME, "nmsadm");

        if (templateName == null) {
            log.error("ERROR:  BsimMicroWCDMADataProvider.getCabinetEquipmentAttributesByTemplateName() templateName is Null");
        } else {
            switch (templateName.toLowerCase()) {
                case "cabinetequipment_mrbs":
                    cabinetEquipmentAttrs.put("FTP Service", aifFtpService);
                    cabinetEquipmentAttrs.put("ipAddress", "1.2.3.4");
                    cabinetEquipmentAttrs.put("defaultRouter", "1.2.3.4");
                    break;
                default:
                    log.error("ERROR: BsimMicroWCDMADataProvider.getCabinetEquipmentAttributesByTemplateName() invalid templateName received " + templateName);
                    break;
            }
        }

        return cabinetEquipmentAttrs;

    }

    private String getNextAvailableCidValue(final int increment) {

        String nextAvailableCid = "1";
        final String lastUtranCell = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + CHECK_LAST_UTRANCELL).trim();
        final String getLastUtranCellscId = SEG_MASTER_SERVICE + " la " + lastUtranCell + " cId";
        final String lastUtranCellscId = ossMasterCLICommandHelper.simpleExec(getLastUtranCellscId);
        final String[] cId = lastUtranCellscId.split(":");
        final int cIdAsInt = Integer.parseInt(cId[1].trim());
	log.info("Last used cId value is: " + cIdAsInt);
        final int nextAvailableCidValue = cIdAsInt + increment;
	log.info("cId value chosen is: " + nextAvailableCidValue);
        nextAvailableCid = Integer.toString(nextAvailableCidValue);
        return nextAvailableCid;

    }

    @Override
    protected LinkedHashMap<String, String> createAifDataOptionAttributes() {

        log.info("Invoking BsimMicroWCDMADataProvider.createAifDataOptionAttributes()");

        final LinkedHashMap<String, String> aifDataOptionAttrs = new LinkedHashMap<String, String>();
        aifDataOptionAttrs.put(CSVColumns.AUTO_INTEGRATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.UNLOCK_CELLS, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.UPLOAD_CV_AFTER_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.INSTALL_LICENSE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.ACTIVATE_LICENSE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.INTEGRATE_UNLOCK, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_BASIC_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_EQUIPMENT_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.SITE_INSTALLATION_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        aifDataOptionAttrs.put(CSVColumns.CABINET_EQUIPMENT_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        // OSS-93001 CMPv2 TAF: Modify Micro/Macro TAF TCs to include Security
        aifDataOptionAttrs.put(CSVColumns.SECURITY, EMPTY_STRING_PLACEHOLDER);
        // OSS-101781 Untrusted WCDMA - TAF: Implement TAF of Add Node of Micro WCDMA node when added with Untrusted options
        aifDataOptionAttrs.put(CSVColumns.NETWORK_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);

        return aifDataOptionAttrs;
    }

    @Override
    public boolean isValidNodeType(final String s) {

        if (s.equals(NodeType.MICRO_WCDMA.toString())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected NodeType getNodeType() {

        return NodeType.MICRO_WCDMA;
    }
}


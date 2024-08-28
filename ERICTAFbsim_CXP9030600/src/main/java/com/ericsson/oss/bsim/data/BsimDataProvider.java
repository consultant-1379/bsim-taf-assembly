package com.ericsson.oss.bsim.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.utils.csv.CsvReader;
import com.ericsson.oss.bsim.batch.data.model.NetworkType;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.DG2Domain;
import com.ericsson.oss.bsim.data.model.NetworkConfiguration;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.data.model.SpitfireDomain;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.operators.api.BsimAddNodeApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimRetrieveServerDataOperator;
import com.ericsson.oss.bsim.robustness.validation.MimVersionValidator;

public abstract class BsimDataProvider {

    private static Logger log = Logger.getLogger(BsimDataProvider.class);

    protected static final String EMPTY_STRING_PLACEHOLDER = "";

    protected static final String DG2_UPGRADE_PATH = "CommonPersistent/Software/TAF_Upgrade";

    private CsvReader csvReader;

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static String SEG_MASTER_SERVICE = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS";

    private static String RETRIEVE_RNC_COMMAND = " lrm | grep RNC";

    private static String RETRIEVE_ENBID_COMMAND = " lt ENodeBFunction -an eNBId | grep -i eNBId";

    private static boolean isCommunityString = false;

    private static boolean isUpgrade = false;

    private NodeType nodeType;

    public void initialization(final String testDataFileName) {
        try {
            csvReader = DataHandler.readCsv(testDataFileName, ";");
        } catch (final Exception e) {
            e.printStackTrace();

        }
    }

    public List<Object[]> generateTestDataForAddNode() {

        final List<Object[]> testDataList = new ArrayList<Object[]>();

        final int rowCount = csvReader.getRowCount();

        // process csv file and generate test data
        // row index starts from 1 as row "0" stores column titles
        for (int rowNo = 1; rowNo < rowCount; rowNo++) {
            final Object[] testData = new Object[7];
            final String csvNodeType = checkCsvForNodeType(rowNo);
            log.info("CSV NODETYPE CHECKED HERE ==>" + csvNodeType);
            if (csvNodeType != null && !csvNodeType.equalsIgnoreCase("")) {
                if (isValidNodeType(csvNodeType)) {
                    nodeType = NodeType.valueOf(csvNodeType);
                } else {
                    log.warn("The specified Node Type in CSV =>" + csvNodeType + "<= is INVALID!");
                }

            } else {
                nodeType = getNodeType();
            }
            final BsimNodeData nodeData = new BsimNodeData(nodeType);

            // all the data will store in the nodeData object
            prepareNodeTestData(nodeData, rowNo);

            testData[0] = retrieveCellValueFromCSV(CSVColumns.TC_ID, rowNo);
            testData[1] = retrieveCellValueFromCSV(CSVColumns.TC_TITLE, rowNo);
            testData[2] = retrieveCellValueFromCSV(CSVColumns.TC_DESC, rowNo);
            testData[3] = nodeData;
            testData[4] = true; // currently only for positive test cases
            testData[5] = retrieveCellValueFromCSV(CSVColumns.NUMBER_OF_NODES_TO_ADD, rowNo);
            testData[6] = retrieveCellValueFromCSV(CSVColumns.TIME_LIMIT_TO_ADD, rowNo);

            testDataList.add(testData);
        }

        return testDataList;
    }

    private void prepareNodeTestData(final BsimNodeData nodeData, final int rowNo) {
        final BsimRetrieveServerDataOperator retrieveServerDataOperator = new BsimRetrieveServerDataOperator();
        String dg2Domain = "";
        final boolean isEndToEnd = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.IS_END_TO_END, rowNo));
        final boolean isIPv6 = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.IS_IPv6, rowNo));
        final String nodeTemplateName = retrieveCellValueFromCSV(CSVColumns.OSS_NODE_TEMPLATE, rowNo);
        final String nodeName = generateNodeName(nodeData.getNodeType(), rowNo);
        final boolean antennaSysConfig = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.ANTENNA_SYS_CONFIG, rowNo));
        final boolean rbrQosFactor = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.RBR_QOS_FACTOR, rowNo));
        final boolean bulkPostInstall = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.BULK_POST_INSTALL, rowNo));
        final boolean updateDHCP = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.UPDATE_DHCP, rowNo));
        final boolean generateDHCPExport = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.GENERATE_DHCP_EXPORT, rowNo));
        final boolean enablePCIAssignment = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.ENABLE_PCI_ASS, rowNo));
        final boolean generateDNSExport = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.GENERATE_DNS_EXPORT, rowNo));
        final boolean associateToSTN = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.ASSOCIATE_TO_STN, rowNo));
        final boolean isGeoRequired = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.ENABLE_GEO_CHECK, rowNo));

        String subNetwork = "";
        String rbsGroup = "";
        String rncIpAddress = "";
        String rncFdn = "";
        String rncSubNetworkFdn = "";
        String scProfile = "";
        String scProfileName = "";
        String rnc = "";
        // Adding BSC for GRAN:
        String bsc = "";
        String Ntype = "";

        if (nodeData.getNodeType() == NodeType.WCDMA || nodeData.getNodeType() == NodeType.MICRO_WCDMA) {

            rncSubNetworkFdn = retrieveRncSubNetworkFdn();
            subNetwork = retrieveRnc(rncSubNetworkFdn);
            log.info("RNC Chosen is ==> " + subNetwork);

            rbsGroup = retrieveRbsGroupFdn(rncSubNetworkFdn);
            rncIpAddress = retrieveRncIpAddress(rncSubNetworkFdn, subNetwork);
            log.info("RNC IP Address ==> " + rncIpAddress);
            rncFdn = rncSubNetworkFdn + ",MeContext=" + subNetwork;

            // new options added
            nodeData.setantennaSystemConfiguration(antennaSysConfig);
            nodeData.setrbrQosFactor(rbrQosFactor);
            nodeData.setbulkPostInstall(bulkPostInstall);
            nodeData.setupdateDHCP(updateDHCP);
            nodeData.setgenerateDHCPExport(generateDHCPExport);
            nodeData.setenablePCIAssignment(enablePCIAssignment);
            nodeData.setgenerateDNSExport(generateDNSExport);
            nodeData.setassociateToSTN(associateToSTN);
            nodeData.setGeoRequired(isGeoRequired);

            boolean enableScProfile = false;
            scProfile = retrieveCellValueFromCSV(CSVColumns.IS_USE_SC_PROFILE, rowNo);
            if (scProfile.equalsIgnoreCase("true")) {
                enableScProfile = true;
                final BsimAddNodeApiOperator bsimAddNodeObject = new BsimAddNodeApiOperator();
                scProfileName = bsimAddNodeObject.getFirstScProfileFromServer();
                nodeData.setScProfile(scProfileName);
                nodeData.setUseSCProfile(enableScProfile);
            } else {
                scProfile = retrieveCellValueFromCSV(CSVColumns.SC_PROFILE, rowNo);
                nodeData.setScProfile(scProfile);
                nodeData.setUseSCProfile(enableScProfile);

            }
        } else if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {

            dg2Domain = retrieveCellValueFromCSV(CSVColumns.DG2_Domain, rowNo);
            final DG2Domain dg2domain = DG2Domain.dg2DomainTypeFromString(dg2Domain);
            nodeData.setDg2Domain(dg2domain);

            if (dg2Domain.equals(DG2Domain.WRAN.toString())) {

                rncSubNetworkFdn = retrieveRncSubNetworkFdn();
                subNetwork = retrieveRnc(rncSubNetworkFdn);
                nodeData.CriticalData.setRncName(subNetwork);

                rbsGroup = retrieveRbsGroupFdn(rncSubNetworkFdn);
                rncIpAddress = retrieveRncIpAddress(rncSubNetworkFdn, subNetwork);
                nodeData.CriticalData.setRbsGroup(rbsGroup);
                log.info("RNC IP Address ==> " + rncIpAddress);
                rncFdn = rncSubNetworkFdn + ",MeContext=" + subNetwork;

            } else if (dg2Domain.equals(DG2Domain.LRAN.toString())) {
                // final String networkType = NetworkType.RADIO.asAttribute();
                // subNetwork = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
                subNetwork = retrieveCellValueFromCSV(CSVColumns.SUBNETWORK_GROUP, rowNo);

            } else if (dg2Domain.equals(DG2Domain.W_L_RAN.toString())) {
                // final String networkType = NetworkType.RADIO.asAttribute();
                // subNetwork = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
                subNetwork = retrieveCellValueFromCSV(CSVColumns.SUBNETWORK_GROUP, rowNo);
                rncSubNetworkFdn = retrieveRncSubNetworkFdn();
                rnc = retrieveRnc(rncSubNetworkFdn);
                nodeData.CriticalData.setRncName(rnc);
                rbsGroup = retrieveRbsGroupFdn(rncSubNetworkFdn);
                nodeData.CriticalData.setRbsGroup(rbsGroup);

            } else if (dg2domain.equals(DG2Domain.TRANSPORT.toString())) {
                subNetwork = retrieveCellValueFromCSV(CSVColumns.SUBNETWORK_GROUP, rowNo);
                // final String networkType = NetworkType.RADIO.asAttribute();
                // subNetwork = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
            }

            // Adding for G-RAN //if (dg2domain.equals(DG2Domain.W-G-RAN.toString()))- cutting xrahmak
            else if (dg2domain.equals(DG2Domain.GRAN.toString().trim()) || dg2domain.equals(DG2Domain.L_G_RAN.toString())) {

                subNetwork = retrieveCellValueFromCSV(CSVColumns.SUBNETWORK_GROUP, rowNo);
                // final String networkType = NetworkType.RADIO.asAttribute();
                // subNetwork = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
                log.info("Subnetwork selected is ==>" + subNetwork);
                bsc = retrieveCellValueFromCSV(CSVColumns.Bsc_Name, rowNo);
                nodeData.setBscName(bsc);
                log.info("BSCName selected is ==>" + nodeData.getBscName());
                Ntype = retrieveCellValueFromCSV(CSVColumns.NODE_TYPE, rowNo);
                log.info("node type selected from csv is==> " + Ntype);
                nodeData.CriticalData.setBscName(bsc);
                final String associatedBsc = getBSCNodeFDN(bsc, subNetwork);
                log.info("Associated BSC is ==>>" + associatedBsc);
                nodeData.CriticalData.setassociatedBsc(associatedBsc);

            }// Adding for GRAN //if (dg2domain.equals(DG2Domain.GRAN.toString()))- cutting xriskas
            else {

                subNetwork = retrieveCellValueFromCSV(CSVColumns.SUBNETWORK_GROUP, rowNo);
                // final String networkType = NetworkType.RADIO.asAttribute();
                // subNetwork = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
                rncSubNetworkFdn = retrieveRncSubNetworkFdn();
                rnc = retrieveRnc(rncSubNetworkFdn);
                nodeData.CriticalData.setRncName(rnc);
                rbsGroup = retrieveRbsGroupFdn(rncSubNetworkFdn);
                nodeData.CriticalData.setRbsGroup(rbsGroup);
                bsc = retrieveCellValueFromCSV(CSVColumns.Bsc_Name, rowNo);
                nodeData.setBscName(bsc);
                log.info("BSCName selected is ==>" + nodeData.getBscName());
                Ntype = retrieveCellValueFromCSV(CSVColumns.NODE_TYPE, rowNo);
                log.info("node type selected from csv is==> " + Ntype);
                nodeData.CriticalData.setBscName(bsc);
                final String associatedBsc = getBSCNodeFDN(bsc, subNetwork);
                nodeData.CriticalData.setassociatedBsc(associatedBsc);
                nodeData.setAssociatedBSc(associatedBsc);
            }
            final String nodeVersion = retrieveCellValueFromCSV(CSVColumns.NODE_VERSION, rowNo);
            nodeData.setNodeVersion(nodeVersion);

        } else if (nodeData.getNodeType() == NodeType.Router_6672) {
            final String spitfireDomain = retrieveCellValueFromCSV(CSVColumns.Spitfire_Domain, rowNo);
            final SpitfireDomain spitfiredomain = SpitfireDomain.spitfireDomainTypeFromString(spitfireDomain);
            nodeData.setSpitfireDomain(spitfiredomain);
            subNetwork = retrieveCellValueFromCSV(CSVColumns.SUBNETWORK_GROUP, rowNo);
            // final String networkType = NetworkType.LTE.asAttribute();
            // subNetwork = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
            final String nodeVersion = retrieveCellValueFromCSV(CSVColumns.NODE_VERSION, rowNo);
            isCommunityString = Boolean.parseBoolean(retrieveCellValueFromCSV(CSVColumns.isCommunityString, rowNo));
            isUpgrade = Boolean.parseBoolean(retrieveCellValueFromCSV(CSVColumns.isUseUpgradePackage, rowNo));
            if (isUpgrade) {
                final String upgradeName = retrieveCellValueFromCSV(CSVColumns.Upgrade_Package_id, rowNo);
                final String upgradeLoc = retrieveCellValueFromCSV(CSVColumns.Upgarde_Package_loc, rowNo);
                nodeData.setupgradePackageName(upgradeName);
                nodeData.setupgradePackageLocation(upgradeLoc);
            }
            nodeData.setNodeVersion(nodeVersion);
            nodeData.setNodeTemplate(nodeTemplateName);

        }

        else {
            // subNetwork = retrieveCellValueFromCSV(CSVColumns.SUBNETWORK_GROUP, rowNo);
            // oss-147671

            final String networkType = NetworkType.LTE.asAttribute();
            subNetwork = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
            log.info("Subnetwork choosen is " + subNetwork);
        }
        if (nodeData.getNodeType() == NodeType.LTE || nodeData.getNodeType() == NodeType.MICRO_LTE) {
            setBsimNodeDataGeoValue(nodeData, rowNo);
        }
        final String planName;
        if (nodeData.getNodeType() == NodeType.BasebandT) {
            planName = "";
        } else {
            planName = generatePlanName(nodeName);
        }

        // mim version is retrieved from server automatically

        final String ossNodeAndMimMapping = retrieveCellValueFromCSV(CSVColumns.OSS_NODE_AND_MIM_MAPPING, rowNo);
        String ossOssMimVersion = "";
        if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {

            ossOssMimVersion = MimVersionValidator.getOssMimVersionByNodeVersion(nodeData.getNodeType(), ossNodeAndMimMapping, isEndToEnd);

            log.info("OSS Mim chosen ==> " + ossOssMimVersion);
        }

        final String ipAddress;
        if (!isEndToEnd) {
            ipAddress = genereateDynamicIPAddress(isIPv6);
        } else {
            ipAddress = retrieveCellValueFromCSV(CSVColumns.IP_ADDRESS, rowNo);
        }
        final String siteName;
        if (nodeTemplateName.contains("Site")) {
            siteName = generateSite(nodeTemplateName, nodeName);
        } else {
            siteName = retrieveCellValueFromCSV(CSVColumns.SITE, rowNo);
        }

        if (nodeData.getNodeType() == NodeType.WCDMA || nodeData.getNodeType() == NodeType.MICRO_WCDMA) {
            nodeData.CriticalData.setRncName(subNetwork);
            nodeData.CriticalData.setRbsGroup(rbsGroup);
            nodeData.CriticalData.setRncIpAddress(rncIpAddress);
            nodeData.CriticalData.setRncFdn(rncFdn);
            nodeData.CriticalData.setOssMimVersion(ossOssMimVersion);

        }

        // -----------------Start to prepare node data model (without
        // AI)----------------
        // Attributes for AddNodeData object
        final LinkedHashMap<String, String> addNodeDataBasicAttrs = createAddNodeDataBasicAttrs(nodeTemplateName);
        setRncAndRBSGroupIfNodeTypeIsWCDMA(nodeData, addNodeDataBasicAttrs);
        updateHashMapWithCSVData(addNodeDataBasicAttrs, rowNo);
        if (nodeData.getNodeType() != NodeType.Router_6672) {
            updateTemplateAttributes(addNodeDataBasicAttrs, nodeName, ipAddress, planName, siteName, ossOssMimVersion);
        } else {
            updateTemplateAttributesSpitfire(addNodeDataBasicAttrs, nodeName, ipAddress, siteName);
        }
        /*
         * if (nodeData.getNodeType() == NodeType.WCDMA){
         * updateScAttribute(addNodeDataBasicAttrs,scProfileName); }
         */
        // node template
        final LinkedHashMap<String, String> nodeTemplateattrs = createNodeTemplateAttributes(nodeTemplateName);
        setRncAndRBSGroupIfNodeTypeIsWCDMA(nodeData, nodeTemplateattrs);
        updateHashMapWithCSVData(nodeTemplateattrs, rowNo);
        /*
         * if (nodeData.getNodeType() == NodeType.LTE || nodeData.getNodeType() == NodeType.MICRO_LTE || nodeData.getNodeType() ==
         * NodeType.Router_6672
         * || nodeData.getNodeType() == NodeType.BasebandT) {
         * nodeTemplateattrs.put(CSVColumns.SUBNETWORK_GROUP, subNetwork);
         * } else if (nodeData.getNodeType() == NodeType.DG2
         * && (dg2Domain.equals(DG2Domain.LRAN.toString()) || dg2Domain.equals(DG2Domain.GRAN.toString())
         * || dg2Domain.equals(DG2Domain.L_G_RAN.toString()) || dg2Domain.equals(DG2Domain.W_L_RAN.toString())
         * || dg2Domain.equals(DG2Domain.W_L_G_RAN.toString()) || dg2Domain.equals(DG2Domain.W_G_RAN.toString()))) {
         * nodeTemplateattrs.put(CSVColumns.SUBNETWORK_GROUP, subNetwork);
         * }
         */
        if (nodeData.getNodeType() == NodeType.LTE || nodeData.getNodeType() == NodeType.MICRO_LTE) {
            nodeTemplateattrs.put(CSVColumns.SUBNETWORK_GROUP, subNetwork);
        }
        if (nodeData.getNodeType() != NodeType.Router_6672) {
            if (nodeData.getNodeType() == NodeType.DG2) {
                nodeTemplateattrs.put("Associated BSC", nodeData.getAssociatedBSc());
            }
            updateTemplateAttributes(nodeTemplateattrs, nodeName, ipAddress, planName, siteName, ossOssMimVersion);
        } else {

            updateTemplateAttributesSpitfire(addNodeDataBasicAttrs, nodeName, ipAddress, siteName);
        }

        // transport
        boolean importTransportConfiguration = false;
        LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();
        if (nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {
            if (parseBooleanVariable(addNodeDataBasicAttrs.get(CSVColumns.IMPORT_TRANSPORT_CONFIGURATION).toLowerCase())) {
                transportTemplateAttrs = getTransportTemplateAttributesByTemplateName(retrieveCellValueFromCSV(CSVColumns.TRANSPORT_TEMPLATE_NAME, rowNo),
                        subNetwork, nodeName, planName);
                importTransportConfiguration = true;
		log.info("Transport Configuration selected.");
            }
        }
        // radio
        boolean importRadioConfiguration = false;
        String radioTemplateName = null;
        LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();
        if (nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {
            if (parseBooleanVariable(addNodeDataBasicAttrs.get(CSVColumns.IMPORT_RADIO_CONFIGURATION).toLowerCase())) {
                radioTemplateName = retrieveCellValueFromCSV(CSVColumns.RADIO_TEMPLATE_NAME, rowNo);
                radioTemplateAttrs = getRadioTemplateAttributesByTemplateName(radioTemplateName, subNetwork, nodeName, planName);
                importRadioConfiguration = true;
		log.info("Radio Configuration selected.");
            }
        }
        // Set test data to BSIMNodeData model
        nodeData.setEndToEnd(isEndToEnd);
        nodeData.setIPv6(isIPv6);
        nodeData.setPlanName(planName);
        nodeData.setNodeTemplate(nodeTemplateName);
        nodeData.setNodeName(nodeName);
        nodeData.setNodeFdn(generateNodeFdn(subNetwork, nodeName));
        // nodeData.setScProfile(scProfile);
        nodeData.setAddNodeDataAttrs(addNodeDataBasicAttrs);
        nodeData.setNodeTemplateAttrs(nodeTemplateattrs);
        nodeData.setImportTransportConfiguration(importTransportConfiguration);
        nodeData.setTransportTemplateAttrs(transportTemplateAttrs);
        nodeData.setImportRadioConfiguration(importRadioConfiguration);
        nodeData.setRadioTemplateAttrs(radioTemplateAttrs);

        nodeData.CriticalData.setSite(siteName);
        nodeData.CriticalData.setIpAddress(ipAddress);
        if (isEndToEnd) {
            nodeData.CriticalData.setRadioTemplateName(radioTemplateName);
        }
        if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {
            // OSS-93001 CMPv2 TAF: Modify Micro/Macro TAF TCs to include Security: updated for Macro
            log.info("ossNodeAndMimMapping.compareTo(O16B-U)" + ossNodeAndMimMapping.compareTo("O16B-U") + "----ossNodeAndMimMapping----"
                    + ossNodeAndMimMapping);
	//Commenting out hardcoding of Netsim mim version and making it dynamic 
           // if (nodeData.getNodeType().equals(NodeType.MICRO_WCDMA) && nodeData.isEndToEnd() == true) {
             //   nodeData.CriticalData.setOssNodeAndMimMapping(ossNodeAndMimMapping);
               // nodeData.CriticalData.setOssMimVersion(ossOssMimVersion);
                //nodeData.CriticalData.setNetsimMimVersion("WCDMA RBS U4470-lim");

                // -----------------Finish preparation for node data
                // model----------------
           // } else if (nodeData.getNodeType().equals(NodeType.WCDMA) && ossNodeAndMimMapping.compareTo("O16B-U") >= 0) {
             //   nodeData.CriticalData.setOssNodeAndMimMapping(ossNodeAndMimMapping);
               // nodeData.CriticalData.setOssMimVersion(ossOssMimVersion);
                //nodeData.CriticalData.setNetsimMimVersion("WCDMA RBS U4470-lim");
            //} else {
                final String netsimOssMimVersion = MimVersionValidator.getNetsimMimByOssMim(ossOssMimVersion);
                nodeData.CriticalData.setOssNodeAndMimMapping(ossNodeAndMimMapping);
                nodeData.CriticalData.setOssMimVersion(ossOssMimVersion);
                nodeData.CriticalData.setNetsimMimVersion(netsimOssMimVersion);
            //}
        } else if (nodeData.getNodeType() == NodeType.DG2) {

            final String nodeVersion = retrieveCellValueFromCSV(CSVColumns.NODE_VERSION, rowNo);
            nodeData.CriticalData.setNetsimMimVersion(MimVersionValidator.getDG2NetsimNEType(nodeVersion));

        } else if (nodeData.getNodeType() == NodeType.BasebandT) {

            final String nodeVersion = retrieveCellValueFromCSV(CSVColumns.NODE_VERSION, rowNo);
            nodeData.CriticalData.setNetsimMimVersion(MimVersionValidator.getBasebandTNetsimNEType(nodeVersion));

        } else if (nodeData.getNodeType() == NodeType.Router_6672) {
            final String nodeVersion = retrieveCellValueFromCSV(CSVColumns.NODE_VERSION, rowNo);
            nodeData.CriticalData.setNetsimMimVersion(MimVersionValidator.getSpitfireNetsimNEType(nodeVersion));
            nodeData.getNodeTemplateAttrs().put(CSVColumns.NODE_NAME, nodeName);
        }

        // Auto Integration Data model preparation
        final String aifFtpService = retrieveCellValueFromCSV(CSVColumns.FTP_AUTO_INTEGRATION, rowNo);
        prepareAIFTestData(nodeData, rowNo, aifFtpService, siteName, nodeName);
    }

    /**
     * @param nodeData
     * @param addNodeDataBasicAttrs
     */
    private void setRncAndRBSGroupIfNodeTypeIsWCDMA(final BsimNodeData nodeData, final LinkedHashMap<String, String> addNodeDataBasicAttrs) {
        if (nodeData.getNodeType() == NodeType.WCDMA || nodeData.getNodeType() == NodeType.MICRO_WCDMA || nodeData.getNodeType() == NodeType.DG2) {
            addNodeDataBasicAttrs.put(CSVColumns.RNC_NAME, nodeData.CriticalData.getRncName());
            addNodeDataBasicAttrs.put(CSVColumns.GROUP_NAME, nodeData.CriticalData.getRbsGroup());
        }
    }

    /**
     * Set whether Geo check needs to be done in netsim for auto-integration
     * Set Pas parameter value for geoFeature enabled
     * 
     * @param nodeData
     * @param rowNo
     */
    private void setBsimNodeDataGeoValue(final BsimNodeData nodeData, final int rowNo) {

        final Boolean isGeoRequired = Boolean.parseBoolean(retrieveCellValueFromCSV(CSVColumns.ENABLE_GEO_CHECK, rowNo));
        nodeData.setGeoRequired(isGeoRequired);

    }

    /**
     * Prepare AIF data for adding a node.
     * 
     * @param nodeData
     *        the object contains all the test data for a single test case
     *        of adding a node
     * @param rowNo
     *        the number of the row to be retrieved from
     * @param aifFtpService
     *        ftp service for auto integration
     * @param ipAddress
     * @param site
     * @param nodeName
     */
    private void prepareAIFTestData(final BsimNodeData nodeData, final int rowNo, final String aifFtpService, final String site, final String nodeName) {

        final boolean autoIntegrate = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.AUTO_INTEGRATE, rowNo));
        final boolean unlockCells = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.UNLOCK_CELLS, rowNo));

        if (autoIntegrate) {
            nodeData.getAifData().setAutoIntegrate(true);

            if (nodeData.getNodeType() == NodeType.LTE || nodeData.getNodeType() == NodeType.MICRO_MACRO_LTE) {
                nodeData.getAifData().setWithoutLaptop(parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.WITHOUT_LAPTOP, rowNo)));

            }
            if (nodeData.getNodeType() == NodeType.MICRO_WCDMA) {
                nodeData.getAifData().setWithoutLaptop(parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.WITHOUT_LAPTOP, rowNo)));
                nodeData.getAifData().setManualBind(parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.MANUAL_BIND, rowNo)));
                final boolean manualBind = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.MANUAL_BIND, rowNo));
                nodeData.getAifData().setManualBind(manualBind);
            }

            else if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {
                final String siteBasicFile = retrieveCellValueFromCSV(CSVColumns.SITE_BASIC_FILE, rowNo);
                final String siteEquipmentFile = retrieveCellValueFromCSV(CSVColumns.SITE_EQUIPMENT_FILE, rowNo);
                final String ossNodeProtocolFile = retrieveCellValueFromCSV(CSVColumns.OSS_NODE_PROTOCOL_TEMPLATE, rowNo);

                final boolean noHWBind = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.NO_HW_BIND, rowNo));
                final String siteInstallationTemplate = retrieveCellValueFromCSV(CSVColumns.SITE_INSTALLATION_TEMPLATE, rowNo);
                final boolean manualBind = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.MANUAL_BIND, rowNo));
                final boolean isUsingOss = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.is_Using_OssNodeProtocolFile, rowNo));

                nodeData.getAifData().setSiteBasicFileName(siteBasicFile);
                nodeData.getAifData().setSiteEquipmentFileName(siteEquipmentFile);
                nodeData.getAifData().setOssNodeProtocolFileName(ossNodeProtocolFile);
                nodeData.getAifData().setIsNoHardwareBind(noHWBind);
                nodeData.getAifData().setSiteInstallTemplateName(siteInstallationTemplate);
                nodeData.getAifData().setManualBind(manualBind);
                nodeData.getAifData().setUsingOssNodeProtocolFile(isUsingOss);
		nodeData.getAifData().setOssNodeProtocolTemplateAttrs(BsimDG2DataProvider.getOssNodeProtocolTemplateAttributes());

            } else if (nodeData.getNodeType() == NodeType.Router_6672) {
                final String siteBasicFile = retrieveCellValueFromCSV(CSVColumns.SITE_BASIC_FILE, rowNo);
                nodeData.getAifData().setSiteBasicFileName(siteBasicFile);
                final String siteEquipmentFile = retrieveCellValueFromCSV(CSVColumns.SITE_EQUIPMENT_FILE, rowNo);
                nodeData.getAifData().setSiteEquipmentFileName(siteEquipmentFile);

                final boolean manualBind = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.MANUAL_BIND, rowNo));
                final String siteInstallationTemplate = retrieveCellValueFromCSV(CSVColumns.SITE_INSTALLATION_TEMPLATE, rowNo);
                nodeData.getAifData().setManualBind(manualBind);
                final boolean isSaveTo = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.isSaveTo, rowNo));
                nodeData.setisSaveTo(isSaveTo);
                nodeData.getAifData().setSiteInstallTemplateName(siteInstallationTemplate);

            } else {
                nodeData.getAifData().setWithoutLaptop(false);
            }

            nodeData.getAifData().setUnlockCells(unlockCells);

            // Aif data option attributes
            final LinkedHashMap<String, String> aifDataOptionAttrs = createAifDataOptionAttributes();
	
	if((nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) && nodeData.getAifData().isUsingOssNodeProtocolFile()){

 		aifDataOptionAttrs.put("subjectName", "subject");
        	aifDataOptionAttrs.put("subjectAltName", "altNameDummy");
        	aifDataOptionAttrs.put("ldapIpAddress", "12.12.13.24");
        	aifDataOptionAttrs.put("fallbackLdapIpAddress", "123.2.3.4");
        	aifDataOptionAttrs.put("bindDn", "1");
        	aifDataOptionAttrs.put("password", "1");
        	aifDataOptionAttrs.put("baseDn", "1");
       		aifDataOptionAttrs.put("address", "120.23.3.4");	
	}
            updateHashMapWithCSVData(aifDataOptionAttrs, rowNo);
            nodeData.getAifData().setAifDataOptionAttrs(aifDataOptionAttrs);

            if (nodeData.getNodeType() != NodeType.DG2 && nodeData.getNodeType() != NodeType.Router_6672 && nodeData.getNodeType() != NodeType.BasebandT) {
                // Site Basic Template
                nodeData.getAifData().setSiteBasicTemplateAttrs(
                        getSiteBasicAttributesByTemplateName(retrieveCellValueFromCSV(CSVColumns.SITE_BASIC_TEMPLATE, rowNo), aifFtpService, nodeName));

                // Site Equipment Template
                nodeData.getAifData().setSiteEquipmentTemplateAttrs(
                        getSiteEquipmentAttributesByTemplateName(retrieveCellValueFromCSV(CSVColumns.SITE_EQUIPMENT_TEMPLATE, rowNo), aifFtpService, nodeName,
                                site));
            }

            // Site Installation Template
            if (nodeData.getNodeType() == NodeType.LTE || nodeData.getNodeType() == NodeType.MICRO_LTE) {

                final boolean security = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.SECURITY, rowNo));
                if (security) {
                    nodeData.getAifData().setSecurity(true);
                }
                nodeData.getAifData().setSiteInstallationTemplateAttrs(
                        getSiteInstallationAttributesByTemplateName(retrieveCellValueFromCSV(CSVColumns.SITE_INSTALLATION_TEMPLATE, rowNo), aifFtpService,
                                nodeName));
            }

            if (nodeData.getNodeType() == NodeType.MICRO_WCDMA || nodeData.getNodeType() == NodeType.WCDMA
                    && nodeData.CriticalData.getOssNodeAndMimMapping().compareTo("O16A-U") >= 0) {
                // OSS-93001 CMPv2 TAF: Modify Micro/Macro TAF TCs to include Security
                final boolean security = parseBooleanVariable(retrieveCellValueFromCSV(CSVColumns.SECURITY, rowNo));
                log.info("security------------------" + security);

                if (security) {
                    nodeData.getAifData().setSecurity(true);
                    log.info("nodeData.getAifData()------------------" + nodeData.getAifData().getSecurity());
                    // OSS-101781:Untrusted WCDMA - TAF: Implement TAF of Add Node of Micro WCDMA node when added with Untrusted options
                    if (nodeData.getNodeType() == NodeType.MICRO_WCDMA && nodeData.CriticalData.getOssNodeAndMimMapping().compareTo("O16A-U") >= 0) {
                        nodeData.getAifData().setNetworkConfiguration(
                                NetworkConfiguration.valueOf(retrieveCellValueFromCSV(CSVColumns.NETWORK_CONFIGURATION, rowNo)));
                    }
                }
                log.info("nodeData.getAifData()------------------" + nodeData.getAifData().getSecurity());

                nodeData.getAifData().setSiteInstallationTemplateAttrs(
                        getSiteInstallationAttributesByTemplateName(retrieveCellValueFromCSV(CSVColumns.SITE_INSTALLATION_TEMPLATE, rowNo), aifFtpService,
                                nodeName));
                nodeData.getAifData().setCabinetEquipmentTemplateAttrs(
                        getCabinetEquipmentAttributesByTemplateName(retrieveCellValueFromCSV(CSVColumns.CABINET_EQUIPMENT_TEMPLATE, rowNo), aifFtpService,
                                nodeName));
            }

        } else {
            // no AI feature required
            nodeData.getAifData().setAutoIntegrate(false);

        }
    }

    private String retrieveCellValueFromCSV(final String columnName, final int rowNo) {

        final int columnNo = csvReader.getColumnNoByHeader(columnName);
        if (columnNo != -1) {
            final String value = csvReader.getCell(columnNo, rowNo);
            // log.info("CSV value is " + value + "on row no." + rowNo + "on column no" + columnNo + "column name" + columnName);
            return value;
        } else {
            log.warn("********************Cannot retrieve value from column: " + columnName + "********************");
            return EMPTY_STRING_PLACEHOLDER;
        }
    }

    private void updateHashMapWithCSVData(final LinkedHashMap<String, String> attributes, final int rowNo) {

        // process the row of record
        final int columnCount = csvReader.getColumnCount();
        log.info("columnCount " + columnCount);
        String columnName;
        for (int i = 1; i < columnCount; i++) {
            columnName = csvReader.getCell(i, 0);
            if (attributes.containsKey(columnName)) {
                attributes.put(columnName, csvReader.getCell(i, rowNo));
            }
        }
    }

    private void updateTemplateAttributes(
            final LinkedHashMap<String, String> attributes,
            final String nodeName,
            final String ipAddress,
            final String planName,
            final String siteName,
            final String mimVersion) {

        // update with dynamic values
        attributes.put(CSVColumns.NODE_NAME, nodeName);
        attributes.put(CSVColumns.PLAN_NAME, planName);
        attributes.put(CSVColumns.IP_ADDRESS, ipAddress);
        attributes.put(CSVColumns.SITE, siteName);
        attributes.put(CSVColumns.MIM_VERSION, mimVersion);
    }

    private void updateTemplateAttributesSpitfire(
            final LinkedHashMap<String, String> attributes,
            final String nodeName,
            final String ipAddress,
            final String siteName) {

        // update with dynamic values
        attributes.put(CSVColumns.NODE_NAME, nodeName);
        attributes.put(CSVColumns.IP_ADDRESS, ipAddress);
        attributes.put(CSVColumns.SITE, siteName);
        if (isCommunityString) {
            attributes.put("CommunityString", "ComStrng");
        }
    }

    private boolean parseBooleanVariable(final String val) {

        if ("true".equalsIgnoreCase(val)) {
            // only return true if it is a String of true
            return true;
        } else {
            return false;
        }
    }

    // -----------------Start generate dynamic values----------------
    private String generateNodeName(final NodeType nodeType, final int rowNo) {

        try {
            // Sleep for random ms (within 1 - 5 ms) to make sure that the node
            // name is unique
            final Random r = new Random(System.currentTimeMillis());
            Thread.sleep(r.nextInt(5) + 1);
        } catch (final InterruptedException e) {
        }
        if (nodeType != NodeType.DG2 && nodeType != NodeType.Router_6672) {
            return nodeType.toString() + "Node_TAF_" + System.currentTimeMillis();
        } else {

            return nodeType.toString() + "Node_TAF_" + System.currentTimeMillis() + "999";
        }
    }

    private String generateNodeFdn(final String subNetwork, final String nodeName) {

        return String.format("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=%1$s,MeContext=%2$s", subNetwork, nodeName);
    }

    private String generatePlanName(final String nodeName) {

        return nodeName + "_PCA";
    }

    private String generateSite(final String nodeTemplateName, final String nodeName) {

        return nodeName + "_Site";
    }

    private String genereateDynamicIPAddress(final boolean isIPv6) {

        final Random r = new Random(System.currentTimeMillis());
        final String ip = r.nextInt(253) + 1 + "." + r.nextInt(255) + "." + r.nextInt(255) + "." + r.nextInt(255);

        return ip;
    }

    private String retrieveRncSubNetworkFdn() {

        final String rncCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + RETRIEVE_RNC_COMMAND).trim();

        final String[] rncList = rncCommandResult.split("\n");

        ArrayUtils.reverse(rncList);

        String rncSubNetworkFdn = null;

        for (final String element : rncList) {

            final String[] count = element.split("=");

            if (count.length == 3 && count[1].contains("SubNetwork")) {

                if (retrieveRbsGroupFdn(element) != null) {

                    rncSubNetworkFdn = element;

                    break;
                }

            }

        }

        return rncSubNetworkFdn.trim();

    }

    private String retrieveRbsGroupFdn(final String[] subRnc) {

        String rbsGroupFdn = null;

        for (final String element : subRnc) {

            if (element.contains("RbsGroup=")) {

                rbsGroupFdn = element;

            }

        }

        if (rbsGroupFdn != null) {
            rbsGroupFdn = rbsGroupFdn.replace("ONRM_ROOT_MO_R", "ONRM_ROOT_MO");
            rbsGroupFdn = rbsGroupFdn.replace("RbsGroup=", "Group=");
        }
        return rbsGroupFdn;

    }

    private String retrieveRbsGroupFdn(final String rncFdn) {

        final String retrieveSubRncCommand = " lm " + rncFdn + " -l 1";

        final String subRncCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + retrieveSubRncCommand).trim();

        final String[] rbsGroupList = subRncCommandResult.split("\n");

        final String rbsGroupFdn = retrieveRbsGroupFdn(rbsGroupList);

        log.info("RbsGroupFdn: " + rbsGroupFdn);

        return rbsGroupFdn;

    }

    private String retrieveRnc(final String rncSubNetworkFdn) {

        final String[] tempArray = rncSubNetworkFdn.split("=");

        log.info(tempArray[2]);

        return tempArray[2];

    }

    /**
     * Retrieves the RNC's IP Address based on the RNC's FDN and the RNC's name
     * 
     * @param rncFdn
     * @param rncName
     * @return
     */
    private String retrieveRncIpAddress(final String rncFdn, final String rncName) {

        final String retrieveSubRncCommand = " lm " + rncFdn + " -l 1";

        final String subRncCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + retrieveSubRncCommand).trim();

        final String[] rbsGroupList = subRncCommandResult.split("\n");

        String retrieveIpCommand = null;

        for (final String element : rbsGroupList) {

            if (element.contains(rncFdn + ",MeContext=" + rncName)) {

                retrieveIpCommand = " la " + element + " ipAddress";
                break;

            }

        }

        final String ipCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + retrieveIpCommand).trim();

        return ipCommandResult.split("\"")[1];

    }

    private String checkCsvForNodeType(final int rowNo) {

        final int columnNo = csvReader.getColumnNoByHeader(CSVColumns.NODE_TYPE);
        if (columnNo != -1) {
            final String value = csvReader.getCell(columnNo, rowNo);
            return value;
        } else {
            return EMPTY_STRING_PLACEHOLDER;
        }
    }

    // -----------------Start abstract methods----------------
    protected abstract NodeType getNodeType();

    protected abstract boolean isValidNodeType(String s);

    protected abstract LinkedHashMap<String, String> createAddNodeDataBasicAttrs(String templateName);

    protected abstract LinkedHashMap<String, String> createNodeTemplateAttributes(String templateName);

    protected abstract LinkedHashMap<String, String> getTransportTemplateAttributesByTemplateName(
            String templateName,
            String subNetwork,
            String nodeName,
            String planName);

    protected abstract LinkedHashMap<String, String> getRadioTemplateAttributesByTemplateName(
            String templateName,
            String subNetwork,
            String nodeName,
            String planName);

    protected abstract LinkedHashMap<String, String> createAifDataOptionAttributes();

    protected abstract LinkedHashMap<String, String> getSiteBasicAttributesByTemplateName(String templateName, String aifFtpService, String nodeName);

    protected abstract LinkedHashMap<String, String> getSiteEquipmentAttributesByTemplateName(
            String templateName,
            String aifFtpService,
            String nodeName,
            String site);

    protected abstract LinkedHashMap<String, String> getSiteInstallationAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            String nodeName);

    protected abstract LinkedHashMap<String, String> getCabinetEquipmentAttributesByTemplateName(
            final String templateName,
            final String aifFtpService,
            String nodeName);

    protected static class CSVColumns {

        // test case info
        static final String TC_ID = "TC ID";

        static final String DG2_Domain = "DG2 Domain";

        static final String NODE_VERSION = "Node Version";

        static final String ASSOCIATED_RNC = "Associated RNC";

        static final String TC_TITLE = "TC Title";

        static final String TC_DESC = "TC Desc";

        static final String IS_END_TO_END = "IsEndToEnd";

        static final String SITE_BASIC_FILE = "Site Basic File";

        static final String SITE_EQUIPMENT_FILE = "Site Equipment File";

        static final String OSS_NODE_PROTOCOL_FILE = "OssNodeProtocol File";

 	static final String OSS_NODE_PROTOCOL_TEMPLATE = "OssNodeProtocolTemplate";

        static final String UPGRADE_PACKAGE = "Upgrade Package File Path";

        static final String IS_IPv6 = "IsIPv6";

        static final String OSS_NODE_AND_MIM_MAPPING = "OSS Node and MIM Mapping";

        static final String NUMBER_OF_NODES_TO_ADD = "Number of Nodes";

        static final String isSaveTo = "issaveto";

        static final String isCommunityString = "isCommunityString";

        static final String isUseUpgradePackage = "isUseUpgradePackage";

        static final String Upgrade_Package_id = "Upgrade Package Id";

        static final String Upgarde_Package_loc = "Upgrade Package Location";

        static final String TIME_LIMIT_TO_ADD = "Time Limit (Minutes)";

        static final String NODE_TYPE = "Node Type"; // Ja Node Type Added

        // Common to one or more (LTE, WCDMA - so far)
        static final String OSS_NODE_TEMPLATE = "OSS Node Template";

        static final String NODE_NAME = "Node Name";

        static final String SUBNETWORK_GROUP = "SubNetwork Group";

        static final String SC_PROFILE = "SC Profile";

        static final String IS_USE_SC_PROFILE = "isUseScProfile";

        static final String IP_ADDRESS = "IP Address";

        static final String FTP_BACKUP_STORE = "ftpBackUpStore";

        static final String FTP_SW_STORE = "ftpSwStore";

        static final String FTP_LICENSE_KEY = "ftpLicenseKey";

        static final String FTP_AUTO_INTEGRATION = "ftpAutoIntegration";

        static final String FTP_UPLINK = "ftpUplink";

        static final String MIM_VERSION = "MIM Version";

        static final String SITE = "Site";

        static final String WORLD_TIME_ZONE_ID = "worldTimeZoneId";

        static final String LOCATION = "Location";

        static final String ASSOCIATED_STN = "Associated STN";

        static final String IMPORT_TRANSPORT_CONFIGURATION = "Import Transport Configuration";

        static final String USE_TRANSPORT_CM_TEMPLATE = "Use Transport CM Template";

        static final String TRANSPORT_TEMPLATE_NAME = "Transport Template Name";

        static final String IMPORT_RADIO_CONFIGURATION = "Import Radio Configuration";

        static final String USE_RADIO_CM_TEMPLATE = "Use Radio CM Template";

        static final String RADIO_TEMPLATE_NAME = "Radio Template Name";

        // LTE specific
        static final String ENABLE_PCI_ASSIGNMENT = "Enable PCI Assignment";

        static final String PCI_PROFILE = "PCI Profile";

        static final String WITHOUT_LAPTOP = "Without Laptop";

        // WCDMA specific
        static final String RNC_NAME = "RNC Name";

        static final String GROUP_NAME = "Group Name";

        static final String FTP_CONFIG_STORE = "ftpConfigStore";

        // Other Attributes not in Templates
        static final String PLAN_NAME = "Plan Name";

        static final String USER_NAME = "User Name";

        // ------------------------- AI specific below -------------------------
        static final String AUTO_INTEGRATE = "Auto Integrate";

        static final String UNLOCK_CELLS = "Unlock Cells";

        // only for Lte
        static final String UPLOAD_CV_AFTER_PLAN_ACTIVATION = "Upload CV after plan activation";

        static final String UPLOAD_CV_AFTER_AUTO_INTEGRATION = "Upload CV after auto integration";

        static final String INSTALL_LICENSE = "Install License";

        static final String ACTIVATE_LICENSE = "Activate License";

        static final String NETWORK_CONFIGURATION = "Network Configuration";

        static final String SECURITY = "Security";

        static final String OPTIMUM_SECURITY_LEVEL = "Optimum Security Level";

        static final String MINIMUM_SECURITY_LEVEL = "Minimum Security Level";

        static final String ENROLLMENT_MODE = "Enrollment Mode";

        // only for Wcdma
        static final String INTEGRATE_UNLOCK = "Integrate Unlock";

        static final String SITE_BASIC_TEMPLATE = "Site Basic Template";

        static final String SITE_EQUIPMENT_TEMPLATE = "Site Equipment Template";

        static final String SITE_INSTALLATION_TEMPLATE = "Site Installation Template";

        static final String is_Using_OssNodeProtocolFile = "isUsingOssNodeProtocolFile";

        static final String CABINET_EQUIPMENT_TEMPLATE = "Cabinet Equipment Template";

        static final String ENABLE_GEO_CHECK = "Enable Geo Check";

        static final String ANTENNA_SYS_CONFIG = "Antenna System Configuration";

        static final String RBR_QOS_FACTOR = "RBR QoS factor";

        static final String BULK_POST_INSTALL = "Bulk Post Install";

        static final String UPDATE_DHCP = "Update DHCP";

        static final String GENERATE_DHCP_EXPORT = "Generate DHCP Export";

        static final String ENABLE_PCI_ASS = "Enable PCI Assignment";

        static final String GENERATE_DNS_EXPORT = "Generate DNS Export";

        static final String ASSOCIATE_TO_STN = "Associate to STN";

        static final String NO_HW_BIND = "No Hardware Bind";

        static final String MANUAL_BIND = "Manual Bind";

        static final String Spitfire_Domain = "Spitfire Domain";

        // GRAN Specific
        static final String Bsc_Name = "BSC Name";

        static final String Associated_bsc = "Associated BSC";
    }

    public String generateENodeBId() {
        log.info("Generating ENodeB ID...");
        String output = "";
        final String result = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + RETRIEVE_ENBID_COMMAND).trim();
        final List<String> enbid = new ArrayList<String>();
        if (result.contains("eNBId (long -1..1048575)")) {
            final String[] rows = result.split("\n");
            for (final String row : rows) {
                enbid.add(row.split("\\:")[1].replace("\r", " ").trim());
            }
        }
        final Random generator = new Random();
        int flag = -1;
        while (flag == -1) {
            final int random = generator.nextInt(1048);
            if (!enbid.contains(random + " ".trim())) {
                flag = 0;
                output = "" + random;
                log.info("ENodeB ID is : " + random);
            }
        }
        return output;
    }

    protected String getBSCNodeFDN(final String bscName, final String bscSubNetworkByBscName) {
        /*
         * for example we need to create bts fdn like below
         * Subnetwork=ONRM_ROOT_MO,Subnetwork=%Subnetworkname%,ManagedElement=%BSC Name%,BssFunction=BSS_ManagedFunction
         */
        // final String rootMo = addNodeData.getFdn().split(",")[0];
        final String rootMo = "SubNetwork=ONRM_ROOT_MO_R";
        return rootMo + ",SubNetwork=" + bscSubNetworkByBscName + ",ManagedElement=" + bscName + ",BssFunction=BSS_ManagedFunction";
    }

}

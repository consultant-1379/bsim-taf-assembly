package com.ericsson.oss.bsim.data;

import static com.ericsson.oss.bsim.operators.api.AddDeleteNodeClusterOperator.clustername;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.guice.OperatorRegistry;
import com.ericsson.cifwk.taf.utils.csv.CsvReader;
import com.ericsson.oss.bsim.batch.data.model.FtpTypes;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.NetworkType;
import com.ericsson.oss.bsim.operators.api.BsimRetrieveServerDataOperator;
import com.ericsson.oss.bsim.operators.api.IClusterOperator;

public class MockLRANPicoBatchBuilder implements TestCase {

    private static Logger log = Logger.getLogger(MockLRANPicoBatchBuilder.class);

    protected static final String EMPTY_STRING_PLACEHOLDER = "";

    public static final String DATA_FILE = "";

    MockLRANPicoBatch mockLRANPicoBatch;

    List<String> nodeFdns;

    private final CsvReader csvReader;

    @Inject
    private OperatorRegistry<IClusterOperator> cexProvider;

    // private final ApiClient client = BsimApiGetter.getOsgiClient();
    private final BsimRetrieveServerDataOperator retrieveServerDataOperator = new BsimRetrieveServerDataOperator();

    public MockLRANPicoBatchBuilder(final MockLRANPicoBatch batch, final CsvReader csvFile) {

        mockLRANPicoBatch = batch;
        csvReader = csvFile;

    }

    public MockLRANPicoBatch prepareLRANPicoBatchTestData(final String batchId, final String customizedBatchName) {

        int rowNo = -1;

        final List<String> name = csvReader.getColumn("BatchConfig ID");
        for (final String s : name) {
            if (s.equals(batchId)) {
                rowNo = name.indexOf(batchId) + 1;
            }
        }
        return prepareLRANPicoBatchTestData(rowNo, customizedBatchName);
    }

    public MockLRANPicoBatch prepareLRANPicoBatchTestData(final int rowNo, final String customizedBatchName) {

        String pciProfileName = null;
        final String nodeTemplateName = retrieveCellValueFromCSV(BatchCSVColumns.OSS_NODE_TEMPLATE, rowNo);

        final String batchName = customizedBatchName != null && !"".equals(customizedBatchName) ? customizedBatchName : retrieveCellValueFromCSV(
                BatchCSVColumns.BATCH_NAME, rowNo);

        // plan ***
        final String planName = retrieveCellValueFromCSV(BatchCSVColumns.PLAN_NAME, rowNo);

        final int batchSize = Integer.parseInt(retrieveCellValueFromCSV(BatchCSVColumns.BATCH_SIZE, rowNo));
        final boolean usePciProfile = parseBooleanVariable(retrieveCellValueFromCSV(BatchCSVColumns.USE_PCI_PROFILE, rowNo));
        if (usePciProfile) {
            pciProfileName = retrieveCellValueFromCSV(BatchCSVColumns.PCI_PROFILE_NAME, rowNo);
        }

        // Retrieve valid CS Data if Required
        final String rncName = checkAndRetreiveCSRncIfRequired(rowNo);
        final String subnetworkGroup = checkAndRetreiveCSSubnetworkGroupIfRequired(rowNo);
        final String ftpStore = checkAndRetreiveCSFtpSwStoreIfRequired(rowNo);
        final String ftpAutoIntegrate = checkAndRetreiveCSFtpAutoIntegrationIfRequired(rowNo);
        final String site = checkAndRetreiveCSSiteIfRequired(rowNo);

        // Node Name & AutoRule. Add Map of fdn's for nodes which will be used
        // for deleting nodes
        String autoRuleText = retrieveCellValueFromCSV(BatchCSVColumns.AUTO_RULE_TEXT, rowNo);
        final String autoRuleNumberRange = retrieveCellValueFromCSV(BatchCSVColumns.AUTO_RULE_NUMBER_RANGE, rowNo);
        final String autoRuleNumberRangeCustom = retrieveCellValueFromCSV(BatchCSVColumns.AUTO_RULE_NUMBER_RANGE_CUSTOM, rowNo);
        String nodeName;
        nodeFdns = new ArrayList<String>();

        if (!retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo).equals("") && autoRuleText.equals("") && autoRuleNumberRange.equals("")
                && autoRuleNumberRangeCustom.equals("")) {
            nodeName = retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo);
            if (nodeName.contains("BATCHNAME")) {
                nodeName = nodeName.replaceAll("BATCHNAME", batchName);
            }

            final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + subnetworkGroup + ",MeContext=" + nodeName;
            nodeFdns.add(fdn);
        } else if (retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo).equals("") && autoRuleText.equals("")
                && (!autoRuleNumberRange.equals("") || !autoRuleNumberRangeCustom.equals(""))) {
            nodeName = autoRuleFixedGenerator(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom);
            if (nodeName.contains("BATCHNAME")) {
                nodeName = nodeName.replaceAll("BATCHNAME", batchName);
                autoRuleText = autoRuleText.replaceAll("BATCHNAME", batchName);
            }
            setNodeFdnValues(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom, subnetworkGroup);

        } else if (retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo).equals("") && !autoRuleText.equals("")
                && (!autoRuleNumberRange.equals("") || !autoRuleNumberRangeCustom.equals(""))) {
            nodeName = autoRuleFixedGenerator(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom);
            if (nodeName.contains("BATCHNAME")) {
                nodeName = nodeName.replaceAll("BATCHNAME", batchName);
                autoRuleText = autoRuleText.replaceAll("BATCHNAME", batchName);
            }
            setNodeFdnValues(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom, subnetworkGroup);

        } else {
            nodeName = null;
        }

        final String nodeVersion = retrieveCellValueFromCSV(BatchCSVColumns.NODE_VERSION, rowNo);

        final LinkedHashMap<String, String> addLRANBatchDataAttrs = createAddLRANBatchDataBasicAttrs(nodeTemplateName);
        updateHashMapWithCSVData(addLRANBatchDataAttrs, rowNo);

        if (addLRANBatchDataAttrs.get("Add to Cluster").equalsIgnoreCase("TRUE")) {
            final String clusterFdn = "SubNetwork=ONRM_ROOT_MO_R,Cluster=" + clustername;
            addLRANBatchDataAttrs.put("Cluster FDN", clusterFdn);
            log.info("cluster :: " + addLRANBatchDataAttrs.get("Cluster FDN"));
        }
        // Templates
        // Node Template
        final LinkedHashMap<String, String> nodeTemplateAttrs = createNodeTemplateAttributes(nodeTemplateName);
        updateHashMapWithCSVData(nodeTemplateAttrs, rowNo);
        updateNodeTemplateAttributes(nodeTemplateAttrs, subnetworkGroup, nodeVersion, nodeName, ftpStore, ftpAutoIntegrate, site);

        // Transport ***
        final LinkedHashMap<String, String> transportTemplateAttrs = prepareTransportTemplateTestData(rowNo, planName, subnetworkGroup, nodeName,
                addLRANBatchDataAttrs);
        // Radio ***
        final LinkedHashMap<String, String> radioTemplateAttrs = prepareRadioTemplateTestData(rowNo, planName, subnetworkGroup, nodeName, addLRANBatchDataAttrs);

        // ICF
        final LinkedHashMap<String, String> icfTemplateAttrs = getIcfTemplateAttributesByTemplateName(
                retrieveCellValueFromCSV(BatchCSVColumns.INITIAL_CONFIGURATION_FILE_TEMPLATE_NAME, rowNo), rncName, nodeName, batchName);

        // Set test data to BsimLRANPicoBatch model
        mockLRANPicoBatch.setName(batchName);
        mockLRANPicoBatch.setSize(batchSize);
        mockLRANPicoBatch.setUsePciProfile(usePciProfile);
        if (usePciProfile) {
            mockLRANPicoBatch.setPciProfileName(pciProfileName);
        }
        mockLRANPicoBatch.setAddNodeDataAttrs(addLRANBatchDataAttrs);
        mockLRANPicoBatch.setNodeTemplateAttrs(nodeTemplateAttrs);
        mockLRANPicoBatch.setTransportTemplateAttrs(transportTemplateAttrs);
        mockLRANPicoBatch.setRadioTemplateAttrs(radioTemplateAttrs);
        mockLRANPicoBatch.setIcfTemplateAttrs(icfTemplateAttrs);
        mockLRANPicoBatch.setNodeFdnValues(nodeFdns);
        mockLRANPicoBatch.setPlanName(planName);
        log.info("BsimLRANBatchDataProvider.prepareLRANPicoBatchTestData(): mockLRANPicoBatch ==" + mockLRANPicoBatch.getName());
        setNoHardwareBindAttributes(mockLRANPicoBatch, rowNo);

        return mockLRANPicoBatch;

    }

    private boolean parseBooleanVariable(final String val) {

        if ("true".equalsIgnoreCase(val)) {
            // only return true if it is a String of true
            return true;
        } else {
            return false;
        }
    }

    private LinkedHashMap<String, String> prepareRadioTemplateTestData(
            final int rowNo,
            final String planName,
            final String subNetworkName,
            final String nodeName,
            final LinkedHashMap<String, String> addLRANBatchDataAttrs) {
        LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();
        if (parseBooleanVariable(addLRANBatchDataAttrs.get(BatchCSVColumns.IMPORT_RADIO_CONFIGURATION).toLowerCase())) {
            radioTemplateAttrs = getRadioTemplateAttributesByTemplateName(retrieveCellValueFromCSV(BatchCSVColumns.RADIO_TEMPLATE_NAME, rowNo), subNetworkName,
                    nodeName, planName);
        }
        return radioTemplateAttrs;
    }

    private LinkedHashMap<String, String> prepareTransportTemplateTestData(
            final int rowNo,
            final String planName,
            final String subNetworkName,
            final String nodeName,
            final LinkedHashMap<String, String> addLRANBatchDataAttrs) {
        LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();
        if (parseBooleanVariable(addLRANBatchDataAttrs.get(BatchCSVColumns.IMPORT_TRANSPORT_CONFIGURATION).toLowerCase())) {
            transportTemplateAttrs = getTransportTemplateAttributesByTemplateName(retrieveCellValueFromCSV(BatchCSVColumns.TRANSPORT_TEMPLATE_NAME, rowNo),
                    subNetworkName, nodeName, planName);
        }
        return transportTemplateAttrs;
    }

    protected LinkedHashMap<String, String> createAddLRANBatchDataBasicAttrs(final String templateName) {

        final LinkedHashMap<String, String> addLRANBatchDataObjectAttrs = new LinkedHashMap<String, String>();

        log.info("BsimLRANBatchDataProvider.createAddLRANBatchDataBasicAttrs() templateName == " + templateName);

        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.OSS_NODE_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.ADD_TO_CLUSTER, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.CLUSTER_FDN, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.UNLOCK_CELLS, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.UPGRADE_PACKAGE_ID, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.UPGRADE_LOCATION, EMPTY_STRING_PLACEHOLDER);
        // **
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.INITIAL_CONFIGURATION_FILE_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.IMPORT_TRANSPORT_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.USE_TRANSPORT_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.TRANSPORT_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.IMPORT_RADIO_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.USE_RADIO_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.RADIO_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);
        // PCI
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.USE_PCI_PROFILE, EMPTY_STRING_PLACEHOLDER);
        addLRANBatchDataObjectAttrs.put(BatchCSVColumns.PCI_PROFILE_NAME, EMPTY_STRING_PLACEHOLDER);

        return addLRANBatchDataObjectAttrs;
    }

    private LinkedHashMap<String, String> createNodeTemplateAttributes(final String nodeTemplateName) {

        final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();

        log.info("BsimLRANBatchDataProvider.createNodeTemplateAttributes(): templateName ==" + nodeTemplateName);

        if (nodeTemplateName.equals("AddLtePicoExample")) {

            nodeTemplateAttrs.put(BatchCSVColumns.SUBNETWORK_GROUP, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.NODE_VERSION, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.FTP_SW_STORE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.FTP_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put("Serial Number", EMPTY_STRING_PLACEHOLDER);

        }
        return nodeTemplateAttrs;
    }

    private void updateNodeTemplateAttributes(
            final LinkedHashMap<String, String> attributes,
            final String subnetworkGroup,
            final String nodeVersion,
            final String nodeName,
            final String ftpStore,
            final String ftpAutoIntegrate,
            final String site) {

        // update with dynamic values
        attributes.put(BatchCSVColumns.SUBNETWORK_GROUP, subnetworkGroup);
        attributes.put(BatchCSVColumns.NODE_NAME, nodeName);
        attributes.put(BatchCSVColumns.NODE_VERSION, nodeVersion);
        attributes.put(BatchCSVColumns.FTP_SW_STORE, ftpStore);
        attributes.put(BatchCSVColumns.FTP_AUTO_INTEGRATION, ftpAutoIntegrate);
        attributes.put(BatchCSVColumns.SITE, site);
        attributes.put("Serial Number", "");

    }

    // ****
    protected LinkedHashMap<String, String> getTransportTemplateAttributesByTemplateName(
            final String templateName,
            final String subNetworkName,
            final String nodeName,
            final String planName) {

        log.info("MockLRANPicoBatchBuilder.getTransportTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();

        // Values Propagated in Bsim Client
        // transportTemplateAttrs.put("RNC Name", rncName);
        transportTemplateAttrs.put("Node Name", nodeName);
        transportTemplateAttrs.put("Plan", planName);

        if (templateName.equals("LranPicoTNBulkCMTemplateFile")) {
            transportTemplateAttrs.put("userLabel", "testUserLabel");
        }

        return transportTemplateAttrs;
    }

    // ****
    protected LinkedHashMap<String, String> getRadioTemplateAttributesByTemplateName(
            final String templateName,
            final String subNetworkName,
            final String nodeName,
            final String planName) {

        log.info("MockLRANPicoBatchBuilder.getRadioTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();

        // Values Propagated in Bsim Client
        // radioTemplateAttrs.put("RNC Name", rncName);
        radioTemplateAttrs.put("Node Name", nodeName);
        radioTemplateAttrs.put("Plan", planName);

        if (templateName.equals("LranPicoRNBulkCMTemplateFile")) {
            radioTemplateAttrs.put("SubNetwork Group", subNetworkName);
            radioTemplateAttrs.put("tac", "1");
            radioTemplateAttrs.put("Cell ID 1", "1");

        }

        return radioTemplateAttrs;
    }

    protected LinkedHashMap<String, String> getIcfTemplateAttributesByTemplateName(
            final String templateName,
            final String dnPrefix,
            final String nodeName,
            final String batchName) {

        log.info("BsimLRANBatchDataProvider.getIcfTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> autoIntLranInitialCngAttrs = new LinkedHashMap<String, String>();

        if (templateName.equals("LRANInitialConfigurationFileExample_14A") || templateName.equals("LRANInitialConfigurationFileExample_IPSEC_14A")
                || templateName.equals("LRAN_ICF_Example_Manual") || templateName.equals("LRAN_ICF_Example_Manual_IPSEC")) {

            final Properties properties = new Properties();
            try {
                properties.load(MockLRANPicoBatchBuilder.class.getResourceAsStream("/Icfpropertiesfiles/" + templateName + ".properties"));
            } catch (final Exception e) {
                log.error("Unable to get the properties file for template:" + templateName);
            }
            for (final String key : properties.stringPropertyNames()) {
                if (key.equals("dnPrefix")) {
                    autoIntLranInitialCngAttrs.put(key, dnPrefix);
                } else if (key.equals("nodeName")) {
                    autoIntLranInitialCngAttrs.put("Node Name", nodeName);
                } else if (key.equals("AutoProvisionProperties")) {
                    autoIntLranInitialCngAttrs.put(key, batchName);
                } else if (key.equals("Save_as")) {
                    autoIntLranInitialCngAttrs.put("Save as", "");
                } else if (key.equals("Cell_1_")) {
                    autoIntLranInitialCngAttrs.put("Cell 1 : localCellId", "1");
                } else if (key.equals("User_Name")) {
                    autoIntLranInitialCngAttrs.put("User Name", "");
                } else if (key.equals("IUB_ID")) {
                    autoIntLranInitialCngAttrs.put("IUB ID", "1");
                } else {
                    final String value = properties.getProperty(key);
                    autoIntLranInitialCngAttrs.put(key, value);
                }
            }
        }
        log.info(templateName + ": Temlate Attributes are " + autoIntLranInitialCngAttrs);

        return autoIntLranInitialCngAttrs;
    }

    /**
     * @param templateName
     * @return
     *         Used for NoHardwareBind Test Case.
     *         Retrieves the SiteInstall attributes based on template name.
     */
    protected Map<String, String> getSiteInstallTemplateAttributesByTemplateName(final String templateName) {

        log.info("MockLRANPicoBatchBuilder.getSiteInstallTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> siteInstallTemplateAttrs = new LinkedHashMap<String, String>();

        if (templateName.equals("SiteInstallPicoExample_Untrusted")) {
            siteInstallTemplateAttrs.put("Integration Outer IP Address", "1.1.1.1");
        }
        siteInstallTemplateAttrs.put("Integration OAM IP Address", "1.1.1.1");

        return siteInstallTemplateAttrs;
    }

    private String checkAndRetreiveCSRncIfRequired(final int rowNo) {

        final String rncName;
        if (retrieveCellValueFromCSV(BatchCSVColumns.RNC_NAME, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_RNC)) {
            rncName = retrieveServerDataOperator.getFirstAvailableRnc();

        } else {
            rncName = retrieveCellValueFromCSV(BatchCSVColumns.RNC_NAME, rowNo);
        }
        log.info("RNC Chosen ==> " + rncName);
        return rncName;
    }

    private String checkAndRetreiveCSSubnetworkGroupIfRequired(final int rowNo) {

        final String subnetworkGroup;
        if (retrieveCellValueFromCSV(BatchCSVColumns.SUBNETWORK_GROUP, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_SUBNETWORK_GROUP)) {
            final String networkType = NetworkType.LTE.asAttribute();
            subnetworkGroup = retrieveServerDataOperator.getFirstSubnetworkByNetworkType(networkType);
        } else {
            subnetworkGroup = retrieveCellValueFromCSV(BatchCSVColumns.SUBNETWORK_GROUP, rowNo);
        }
        log.info("SubNetwork Chosen ==> " + subnetworkGroup);
        return subnetworkGroup;
    }

    private String checkAndRetreiveCSFtpSwStoreIfRequired(final int rowNo) {

        String ftpSwStore;
        if (retrieveCellValueFromCSV(BatchCSVColumns.FTP_SW_STORE, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_FTP_SW_STORE)) {
            final String ftpType = FtpTypes.FTP_TYPE_SW_STORE;
            final String networkType = NetworkType.CORE.asAttribute();
            ftpSwStore = retrieveServerDataOperator.getFirstFtpServiceByFtpAndNetworkType(ftpType, networkType);
        } else {
            ftpSwStore = retrieveCellValueFromCSV(BatchCSVColumns.FTP_SW_STORE, rowNo);
        }
        log.info("FtpSwStore Chosen ==> " + ftpSwStore);
        return ftpSwStore;
    }

    private String checkAndRetreiveCSFtpAutoIntegrationIfRequired(final int rowNo) {

        String ftpAutoIntegration;
        if (retrieveCellValueFromCSV(BatchCSVColumns.FTP_AUTO_INTEGRATION, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_FTP_AUTO_INTEGRATION)) {
            final String ftpType = FtpTypes.FTP_TYPE_AUTO_INTEGRATION;
            final String networkType = NetworkType.CORE.asAttribute();
            ftpAutoIntegration = retrieveServerDataOperator.getFirstFtpServiceByFtpAndNetworkType(ftpType, networkType);
        } else {
            ftpAutoIntegration = retrieveCellValueFromCSV(BatchCSVColumns.FTP_AUTO_INTEGRATION, rowNo);
        }
        log.info("ftpAutoIntegration Chosen ==> " + ftpAutoIntegration);
        return ftpAutoIntegration;
    }

    private String checkAndRetreiveCSSiteIfRequired(final int rowNo) {

        final String site;
        if (retrieveCellValueFromCSV(BatchCSVColumns.SITE, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_SITE)) {
            site = retrieveServerDataOperator.getAvailableSiteName();
        } else {
            site = retrieveCellValueFromCSV(BatchCSVColumns.SITE, rowNo);
        }
        log.info("Site Chosen ==> " + site);
        return site;
    }

    private String autoRuleFixedGenerator(final int batchSize, final String text, final String numberRange, final String numberRangeCustom) {

        int numberRangeSize = 0;
        int numberRangeCustomSize = 0;
        if (!numberRange.equals("") && numberRange.contains(":")) {
            numberRangeSize = Integer.parseInt(numberRange.substring(numberRange.lastIndexOf(':') + 1));
        }
        if (!numberRangeCustom.equals("") && numberRangeCustom.contains("-")) {
            numberRangeCustomSize = Integer.parseInt(numberRangeCustom.substring(numberRangeCustom.lastIndexOf('-') + 1));
        }
        // NumberRange only node name assignment
        if (numberRangeSize == batchSize && text.equals("") && numberRangeCustom.equals("")) {
            return "AutoRuleNumber::1:" + numberRangeSize + ";1|assignmentRuleFixed::true|";
        }
        // NumberRangeCustom only node name assignment
        if (numberRangeCustomSize == batchSize && text.equals("") && numberRange.equals("")) {
            return "AutoRuleCustomRange::1-" + numberRangeCustomSize + "|assignmentRuleFixed::true|";
        }
        // NumberRange and text node name assignment
        if (numberRangeSize == batchSize && !text.equals("") && numberRangeCustom.equals("")) {
            return "AutoRuleText::" + text + "|AutoRuleNumber::1:" + numberRangeSize + ";1|assignmentRuleFixed::true|";
        }
        // NumberRangeCustom and text node name assignment
        if (numberRangeCustomSize == batchSize && !text.equals("") && numberRange.equals("")) {
            return "AutoRuleText::" + text + "|AutoRuleCustomRange::1-" + numberRangeCustomSize + "|assignmentRuleFixed::true|";
        }
        // NumberRangeCustom, NumberRange node name assignment
        if (numberRangeCustomSize == batchSize && numberRangeSize == batchSize && text.equals("")) {
            return "AutoRuleNumber::1:" + numberRangeSize + ";1|AutoRuleCustomRange::1-" + numberRangeCustomSize + "|assignmentRuleFixed::true|";
        }
        // NumberRangeCustom, NumberRange and text node name assignment
        if (numberRangeCustomSize == batchSize && numberRangeSize == batchSize && !text.equals("")) {
            return "AutoRuleText::" + text + "|AutoRuleCustomRange::1-" + numberRangeCustomSize + "|AutoRuleNumber::1:" + numberRangeSize
                    + ";1|assignmentRuleFixed::true|";
        } else {
            return null;
        }

    }

    private void setNodeFdnValues(final int batchSize, final String text, final String numberRange, final String numberRangeCustom, final String subnetwork) {

        int numberRangeSize = 0;
        int numberRangeCustomSize = 0;
        if (!numberRange.equals("") && numberRange.contains(":")) {
            numberRangeSize = Integer.parseInt(numberRange.substring(numberRange.lastIndexOf(':') + 1));
        }
        if (!numberRangeCustom.equals("") && numberRangeCustom.contains("-")) {
            numberRangeCustomSize = Integer.parseInt(numberRangeCustom.substring(numberRangeCustom.lastIndexOf('-') + 1));
        }
        // NumberRange only node name assignment
        if (numberRangeSize == batchSize && text.equals("") && numberRangeCustom.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + subnetwork + ",MeContext=" + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom only node name assignment
        if (numberRangeCustomSize == batchSize && text.equals("") && numberRange.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + subnetwork + ",MeContext=" + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRange and text node name assignment
        if (numberRangeSize == batchSize && !text.equals("") && numberRangeCustom.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + subnetwork + ",MeContext=" + text + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom and text node name assignment
        if (numberRangeCustomSize == batchSize && !text.equals("") && numberRange.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + subnetwork + ",MeContext=" + text + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom, NumberRange node name assignment
        if (numberRangeCustomSize == batchSize && numberRangeSize == batchSize && text.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + subnetwork + ",MeContext=" + i + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom, NumberRange and text node name assignment
        if (numberRangeCustomSize == batchSize && numberRangeSize == batchSize && !text.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + subnetwork + ",MeContext=" + text + i + i;
                nodeFdns.add(fdn);
            }
        }

    }

    private String retrieveCellValueFromCSV(final String columnName, final int rowNo) {

        final int columnNo = csvReader.getColumnNoByHeader(columnName);
        if (columnNo != -1) {
            final String value = csvReader.getCell(columnNo, rowNo);

            // for test purpose
            log.debug(value);
            return value;
        } else {
            log.warn("********************Cannot retrieve value from column: " + columnName + "********************");
            return EMPTY_STRING_PLACEHOLDER;
        }
    }

    private void updateHashMapWithCSVData(final LinkedHashMap<String, String> attributes, final int rowNo) {

        // process the row of record
        final int columnCount = csvReader.getColumnCount();
        String columnName;
        for (int i = 1; i < columnCount; i++) {
            columnName = csvReader.getCell(i, 0);
            if (attributes.containsKey(columnName)) {
                attributes.put(columnName, csvReader.getCell(i, rowNo));
            }
        }
    }

    /**
     * @param mockLRANPicoBatch
     * @param rowNo
     *        Retrieves attributes for NoHardware Bind if necessary from CSV.
     */
    private void setNoHardwareBindAttributes(final MockLRANPicoBatch mockLRANPicoBatch, final int rowNo) {

        if (retrieveCellValueFromCSV(BatchCSVColumns.NO_HARDWARE_BIND, rowNo).equals("true")) {

            mockLRANPicoBatch.setIsNoHardwareBind(true);

            mockLRANPicoBatch.setSiteInstallTemplateName(retrieveCellValueFromCSV(BatchCSVColumns.SITE_INSTALL_TEMPLATE_NAME, rowNo));

            final Map<String, String> siteInstallTemplateAttrs = getSiteInstallTemplateAttributesByTemplateName(retrieveCellValueFromCSV(
                    BatchCSVColumns.SITE_INSTALL_TEMPLATE_NAME, rowNo));
            mockLRANPicoBatch.setSiteInstallTemplateAttrs(siteInstallTemplateAttrs);
        } else {

            mockLRANPicoBatch.setIsNoHardwareBind(false);
        }
    }

    protected static class BatchCSVColumns {

        // test case info
        static final String TC_ID = "TC ID";

        static final String TC_TITLE = "TC Title";

        static final String TC_DESC = "TC Desc";

        static final String BIND = "Bind";

        static final String NODES_TO_BIND = "Nodes to Bind";

        static final String DELETE_BOUND_NODES = "Delete Bound Nodes";

        static final String BATCH_RESULT = "Batch Result";

        static final String BIND_RESULT = "Bind Result";

        static final String DELETE_RESULT = "Delete Result";

        // General
        static final String BATCH_NAME = "Batch Name";

        static final String BATCH_SIZE = "Batch Size";

        // PCI
        static final String USE_PCI_PROFILE = "Use PCI Profile";

        static final String PCI_PROFILE_NAME = "PCI Profile Name";

        // Auto Rule
        static final String AUTO_RULE_TEXT = "Auto Rule Text";

        static final String AUTO_RULE_NUMBER_RANGE = "Auto Rule Number Range";

        static final String AUTO_RULE_NUMBER_RANGE_CUSTOM = "Auto Rule Number Range Custom";

        // Other
        static final String USER_NAME = "User Name";

        // Node Template
        static final String OSS_NODE_TEMPLATE = "OSS Node Template";

        static final String PLAN_NAME = "Plan Name";

        static final String RNC_NAME = "RNC Name";

        static final String SUBNETWORK_GROUP = "SubNetwork Group";

        static final String NODE_VERSION = "Node Version";

        static final String NODE_NAME = "Node Name";

        static final String ADD_TO_CLUSTER = "Add to Cluster";

        static final String CLUSTER_FDN = "Cluster FDN";

        static final String FTP_SW_STORE = "ftpSwStore";

        static final String FTP_AUTO_INTEGRATION = "ftpAutoIntegration";

        static final String SITE = "Site";

        // Transport Template
        static final String IMPORT_TRANSPORT_CONFIGURATION = "Import Transport Configuration";

        static final String USE_TRANSPORT_CM_TEMPLATE = "Use Transport CM Template";

        static final String TRANSPORT_TEMPLATE_NAME = "Transport Template Name";

        // Radio Template
        static final String IMPORT_RADIO_CONFIGURATION = "Import Radio Configuration";

        static final String USE_RADIO_CM_TEMPLATE = "Use Radio CM Template";

        static final String RADIO_TEMPLATE_NAME = "Radio Template Name";

        // No Hardware Bind
        static final String NO_HARDWARE_BIND = "No Hardware Bind";

        static final String SITE_INSTALL_TEMPLATE_NAME = "Site Install Template Name";

        // AI specific below
        static final String UNLOCK_CELLS = "Unlock Cells";

        static final String UPGRADE_PACKAGE_ID = "Upgrade Package Id";

        static final String UPGRADE_LOCATION = "Upgrade Package Location";

        static final String INITIAL_CONFIGURATION_FILE_TEMPLATE_NAME = "Initial Configuration File Template Name";

        // CS Retrieval Values

        static final String CS_RETRIEVED_SITE = "CS_RETRIEVED_SITE";

        static final String CS_RETRIEVED_RNC = "CS_RETRIEVED_RNC";

        static final String CS_RETRIEVED_SUBNETWORK_GROUP = "CS_RETRIEVED_SUBNETWORK_GROUP";

        static final String CS_RETRIEVED_GROUP_NAME = "CS_RETRIEVED_GROUP_NAME";

        static final String CS_RETRIEVED_FTP_SW_STORE = "CS_RETRIEVED_FTP_SW_STORE";

        static final String CS_RETRIEVED_FTP_AUTO_INTEGRATION = "CS_RETRIEVED_FTP_AUTO_INTEGRATION";

    }

}


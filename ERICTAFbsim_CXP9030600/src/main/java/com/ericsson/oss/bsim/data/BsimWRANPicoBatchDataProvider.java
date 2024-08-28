package com.ericsson.oss.bsim.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.utils.csv.CsvReader;
import com.ericsson.oss.bsim.batch.data.model.FtpTypes;
import com.ericsson.oss.bsim.batch.data.model.MockWRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.NetworkType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.operators.api.BsimRetrieveServerDataOperator;

public class BsimWRANPicoBatchDataProvider {

    private static Logger log = Logger.getLogger(BsimWRANPicoBatchDataProvider.class);

    protected static final String EMPTY_STRING_PLACEHOLDER = "";

    public static final String DATA_FILE = "BSIM_WRANPicoBatchTestData.csv";

    private final BsimRemoteCommandExecutor commandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static String SEG_MASTER_SERVICE = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS";

    private static String LIST_MO = " lm ";

    private static String LIST_ATTRIBUTE = " la ";

    private static final String getGroupNameSSHCommand = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s ONRM_CS lt Group | grep %1$s";

    private static String RETRIEVE_RNC_COMMAND = " lrm | grep RNC";

    List<String> nodeFdns;

    CsvReader csvReader;

    // private final ApiClient client = BsimApiGetter.getOsgiClient();
    private final BsimRetrieveServerDataOperator retrieveServerDataOperator = new BsimRetrieveServerDataOperator();

    public List<Object[]> getTestDataList() {

        initialization(DATA_FILE);
        return generateTestDataForWRANPicoBatch();
    }

    public void initialization(final String testDataFileName) {
        try {
            csvReader = DataHandler.readCsv(testDataFileName, ",");
        } catch (final Exception ex) {

            ex.printStackTrace();
        }
    }

    public List<Object[]> generateTestDataForWRANPicoBatch() {

        final List<Object[]> testDataList = new ArrayList<Object[]>();

        final int rowCount = csvReader.getRowCount();

        // process csv file and generate test data
        // row index starts from 1 as row "0" stores column titles
        for (int rowNo = 1; rowNo < rowCount; rowNo++) {
            final Object[] testData = new Object[11];
            final MockWRANPicoBatch mockWRANPicoBatch = new MockWRANPicoBatch();

            // all the data will store in the BsimWRANPicoBatch object
            prepareWRANPicoBatchTestData(mockWRANPicoBatch, rowNo);

            testData[0] = retrieveCellValueFromCSV(BatchCSVColumns.TC_ID, rowNo);
            testData[1] = retrieveCellValueFromCSV(BatchCSVColumns.TC_TITLE, rowNo);
            testData[2] = retrieveCellValueFromCSV(BatchCSVColumns.TC_DESC, rowNo);
            testData[3] = mockWRANPicoBatch;
            testData[4] = retrieveCellValueFromCSV(BatchCSVColumns.BIND, rowNo);
            testData[5] = retrieveCellValueFromCSV(BatchCSVColumns.NODES_TO_BIND, rowNo);
            testData[6] = retrieveCellValueFromCSV(BatchCSVColumns.DELETE_BOUND_NODES, rowNo);
            testData[7] = retrieveCellValueFromCSV(BatchCSVColumns.BATCH_RESULT, rowNo);
            testData[8] = retrieveCellValueFromCSV(BatchCSVColumns.BIND_RESULT, rowNo);
            testData[9] = retrieveCellValueFromCSV(BatchCSVColumns.DELETE_RESULT, rowNo);
            testData[10] = retrieveCellValueFromCSV(BatchCSVColumns.IS_END_TO_END, rowNo);
            testDataList.add(testData);
        }

        return testDataList;

    }

    private void prepareWRANPicoBatchTestData(final MockWRANPicoBatch mockWRANPicoBatch, final int rowNo) {

        String scProfileName = null;
        final String nodeTemplateName = retrieveCellValueFromCSV(BatchCSVColumns.OSS_NODE_TEMPLATE, rowNo);
        // Prepare required data
        // General
        final boolean isAutoPlan = parseBooleanVariable(retrieveCellValueFromCSV(BatchCSVColumns.AUTO_PLAN, rowNo));
        final String batchName = retrieveCellValueFromCSV(BatchCSVColumns.BATCH_NAME, rowNo);
        final int batchSize = Integer.parseInt(retrieveCellValueFromCSV(BatchCSVColumns.BATCH_SIZE, rowNo));
        final String planName = retrieveCellValueFromCSV(BatchCSVColumns.PLAN_NAME, rowNo);
        final boolean useScAssignment = parseBooleanVariable(retrieveCellValueFromCSV(BatchCSVColumns.USE_SC, rowNo));
        if (useScAssignment) {
            scProfileName = retrieveCellValueFromCSV(BatchCSVColumns.SC_PROFILE_NAME, rowNo);
        }
        final boolean isEndToEnd = Boolean.parseBoolean(retrieveCellValueFromCSV(BatchCSVColumns.IS_END_TO_END, rowNo));
        // Retrieve valid CS Data if Required
        final String rncName = retrieveRncSubNetworkName();
        final String groupName = retrieveRbsGroupFdn(rncName);
        // // retrieveRbsGroupFdn(rncName);
        final String nodeVersion = retrieveCellValueFromCSV(BatchCSVColumns.NODE_VERSION, rowNo);
        final String ftpStore = checkAndRetreiveCSFtpSwStoreIfRequired(rowNo);
        final String ftpAutoIntegrate = checkAndRetreiveCSFtpAutoIntegrationIfRequired(rowNo);
        final String site = checkAndRetreiveCSSiteIfRequired(rowNo);
        final String ipAddress = retrieveCellValueFromCSV(BatchCSVColumns.IP_ADDRESS, rowNo);

        // Node Name & AutoRule. Add Map of fdn's for nodes which will be used
        // for deleting nodes
        final String autoRuleText = retrieveCellValueFromCSV(BatchCSVColumns.AUTO_RULE_TEXT, rowNo);
        final String autoRuleNumberRange = retrieveCellValueFromCSV(BatchCSVColumns.AUTO_RULE_NUMBER_RANGE, rowNo);
        final String autoRuleNumberRangeCustom = retrieveCellValueFromCSV(BatchCSVColumns.AUTO_RULE_NUMBER_RANGE_CUSTOM, rowNo);
        String nodeName;

        nodeFdns = new ArrayList<String>();

        nodeName = getNodeName(mockWRANPicoBatch, rowNo, batchSize, rncName, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom);

        // WRANAddBatchData
        final LinkedHashMap<String, String> addWRANBatchDataAttrs = createAddWRANBatchDataBasicAttrs(nodeTemplateName);
        updateHashMapWithCSVData(addWRANBatchDataAttrs, rowNo);
        final String rncFdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + rncName;
        final String rncIpAddress = retrieveIpAddress(rncFdn);
        final String associatedRnc = retrieveAssociatedRncSubNetworkName(rncFdn);

        // Templates
        final LinkedHashMap<String, String> nodeTemplateAttrs = prepareNodeTemplateTestData(rowNo, nodeTemplateName, rncName, groupName, ftpStore,
                ftpAutoIntegrate, site, nodeName, nodeVersion, associatedRnc);

        final LinkedHashMap<String, String> transportTemplateAttrs = prepareTransportTemplateTestData(rowNo, planName, rncName, nodeName, addWRANBatchDataAttrs);

        final LinkedHashMap<String, String> radioTemplateAttrs = prepareRadioTemplateTestData(rowNo, planName, rncName, nodeName, addWRANBatchDataAttrs);

        final LinkedHashMap<String, String> icfTemplateAttrs = getIcfTemplateAttributesByTemplateName(
                retrieveCellValueFromCSV(BatchCSVColumns.INITIAL_CONFIGURATION_FILE_TEMPLATE_NAME, rowNo), rncName, nodeName);

        // Set test data to BsimWRANPicoBatch model
        mockWRANPicoBatch.setName(batchName);
        mockWRANPicoBatch.setIsAutoPlan(isAutoPlan);
        mockWRANPicoBatch.setSize(batchSize);
        mockWRANPicoBatch.setUseScAssignment(useScAssignment);
        if (useScAssignment) {
            mockWRANPicoBatch.setScProfileName(scProfileName);
        }
        mockWRANPicoBatch.setIsEndToEnd(isEndToEnd);
        mockWRANPicoBatch.setIpAddress(ipAddress);
        mockWRANPicoBatch.setPlanName(planName);
        mockWRANPicoBatch.setRncName(rncName);
        mockWRANPicoBatch.setRncFdn(rncFdn);
        mockWRANPicoBatch.setRncIpAddress(rncIpAddress);
        mockWRANPicoBatch.setSite(site);
        mockWRANPicoBatch.setAddNodeDataAttrs(addWRANBatchDataAttrs);
        mockWRANPicoBatch.setNodeTemplateAttrs(nodeTemplateAttrs);
        mockWRANPicoBatch.setTransportTemplateAttrs(transportTemplateAttrs);
        mockWRANPicoBatch.setRadioTemplateAttrs(radioTemplateAttrs);
        mockWRANPicoBatch.setIcfTemplateAttrs(icfTemplateAttrs);
        mockWRANPicoBatch.setNodeFdnValues(nodeFdns);
    }

    /**
     * Retrieves the RbsGroup FDN
     * 
     * @param rncName
     * @return
     */
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

    /**
     * Retrieves the RbsGroup FDN corresponding to the RNC.
     * Example rbsGroupFdn ==> "SubNetwork=ONRM_ROOT_MO,SubNetwork=RNC02,Group=RNC02RBSGROUP"
     * 
     * @param rncName
     * @return rbsGroupFdn
     */
    private String retrieveRbsGroupFdn(final String rncName) {
        final String rncFdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName;
        final String retrieveSubRncCommand = " lm " + rncFdn + " -l 1";
        final String subRncCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + retrieveSubRncCommand).trim();
        final String[] rbsGroupList = subRncCommandResult.split("\n");
        final String rbsGroupFdn = retrieveRbsGroupFdn(rbsGroupList);
        log.info("RbsGroupFdn: " + rbsGroupFdn);
        return rbsGroupFdn;
    }

    /**
     * Checks if an RNC has a RBS Group and returns the rbsGroupFdn
     * 
     * @param rncFdn
     * @return rbsGroupFdn
     */
    private String checkIfRncHasAnRbsGroup(final String rncFdn) {
        // final String rncFdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName;
        final String retrieveSubRncCommand = " lm " + rncFdn + " -l 1";
        final String subRncCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + retrieveSubRncCommand).trim();
        final String[] rbsGroupList = subRncCommandResult.split("\n");
        final String rbsGroupFdn = retrieveRbsGroupFdn(rbsGroupList);
        log.info("RbsGroupFdn: " + rbsGroupFdn);
        return rbsGroupFdn;
    }

    /**
     * Retrieves a suitable RNC Name, if it has an associated RbsGroup. For example "RNC02"
     * 
     * @return rncName
     */
    private String retrieveRncSubNetworkName() {
        final String rncCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + RETRIEVE_RNC_COMMAND).trim();
        final String[] rncList = rncCommandResult.split("\n");
        ArrayUtils.reverse(rncList);
        String rncSubNetworkFdn = null;
        for (final String element : rncList) {
            final String[] count = element.split("=");
            if (count.length == 3 && count[1].contains("SubNetwork")) {
                if (checkIfRncHasAnRbsGroup(element) != null) {
                    rncSubNetworkFdn = element;
                    break;
                }
            }
        }

        final String[] temp = rncSubNetworkFdn.split("=");
        final String rncName = temp[2].trim();
        log.info("Rnc chosen is: " + rncName);
        return rncName;
    }

    private LinkedHashMap<String, String> prepareRadioTemplateTestData(
            final int rowNo,
            final String planName,
            final String rncName,
            final String nodeName,
            final LinkedHashMap<String, String> addWRANBatchDataAttrs) {
        LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();
        if (parseBooleanVariable(addWRANBatchDataAttrs.get(BatchCSVColumns.IMPORT_RADIO_CONFIGURATION).toLowerCase())) {
            radioTemplateAttrs = getRadioTemplateAttributesByTemplateName(retrieveCellValueFromCSV(BatchCSVColumns.RADIO_TEMPLATE_NAME, rowNo), rncName,
                    nodeName, planName);
        }
        return radioTemplateAttrs;
    }

    private LinkedHashMap<String, String> prepareTransportTemplateTestData(
            final int rowNo,
            final String planName,
            final String rncName,
            final String nodeName,
            final LinkedHashMap<String, String> addWRANBatchDataAttrs) {
        LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();
        if (parseBooleanVariable(addWRANBatchDataAttrs.get(BatchCSVColumns.IMPORT_TRANSPORT_CONFIGURATION).toLowerCase())) {
            transportTemplateAttrs = getTransportTemplateAttributesByTemplateName(retrieveCellValueFromCSV(BatchCSVColumns.TRANSPORT_TEMPLATE_NAME, rowNo),
                    rncName, nodeName, planName);
        }
        return transportTemplateAttrs;
    }

    private LinkedHashMap<String, String> prepareNodeTemplateTestData(
            final int rowNo,
            final String nodeTemplateName,
            final String rncName,
            final String groupName,
            final String ftpStore,
            final String ftpAutoIntegrate,
            final String site,
            final String nodeName,
            final String nodeVersion,
            final String associatedRnc) {
        final LinkedHashMap<String, String> nodeTemplateAttrs = createNodeTemplateAttributes(nodeTemplateName);
        updateHashMapWithCSVData(nodeTemplateAttrs, rowNo);
        updateNodeTemplateAttributes(nodeTemplateAttrs, rncName, groupName, nodeName, ftpStore, ftpAutoIntegrate, site, nodeVersion, associatedRnc);
        return nodeTemplateAttrs;
    }

    private String getNodeName(
            final MockWRANPicoBatch mockWRANPicoBatch,
            final int rowNo,
            final int batchSize,
            final String rncName,
            final String autoRuleText,
            final String autoRuleNumberRange,
            final String autoRuleNumberRangeCustom) {
        String nodeName;
        if (!retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo).equals("") && autoRuleText.equals("") && autoRuleNumberRange.equals("")
                && autoRuleNumberRangeCustom.equals("")) {
            nodeName = retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo);
            final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + nodeName;
            nodeFdns.add(fdn);
            mockWRANPicoBatch.setNodeName(nodeName);
        } else if (retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo).equals("") && autoRuleText.equals("")
                && (!autoRuleNumberRange.equals("") || !autoRuleNumberRangeCustom.equals(""))) {
            nodeName = autoRuleFixedGenerator(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom);
            setNodeFdnValues(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom, rncName);
        } else if (retrieveCellValueFromCSV(BatchCSVColumns.NODE_NAME, rowNo).equals("") && !autoRuleText.equals("")
                && (!autoRuleNumberRange.equals("") || !autoRuleNumberRangeCustom.equals(""))) {
            nodeName = autoRuleFixedGenerator(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom);
            setNodeFdnValues(batchSize, autoRuleText, autoRuleNumberRange, autoRuleNumberRangeCustom, rncName);
        } else {
            nodeName = null;
        }
        return nodeName;
    }

    protected LinkedHashMap<String, String> createAddWRANBatchDataBasicAttrs(final String templateName) {

        final LinkedHashMap<String, String> addWRANBatchDataObjectAttrs = new LinkedHashMap<String, String>();

        log.info("BsimWRANBatchDataProvider.createAddWRANBatchDataBasicAttrs() templateName == " + templateName);

        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.AUTO_PLAN, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.PLAN_NAME, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.OSS_NODE_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.IMPORT_TRANSPORT_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.USE_TRANSPORT_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.TRANSPORT_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.IMPORT_RADIO_CONFIGURATION, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.USE_RADIO_CM_TEMPLATE, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.RADIO_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.UNLOCK_CELLS, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.UPGRADE_PACKAGE_ID, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.UPGRADE_LOCATION, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.INITIAL_CONFIGURATION_FILE_TEMPLATE_NAME, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.USE_SC, EMPTY_STRING_PLACEHOLDER);
        addWRANBatchDataObjectAttrs.put(BatchCSVColumns.SC_PROFILE_NAME, EMPTY_STRING_PLACEHOLDER);

        return addWRANBatchDataObjectAttrs;
    }

    private LinkedHashMap<String, String> createNodeTemplateAttributes(final String nodeTemplateName) {

        final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();

        log.info("BsimWRANBatchDataProvider.createNodeTemplateAttributes(): templateName ==" + nodeTemplateName);

        if (nodeTemplateName.equals("AddWcdmaPicoExample") || nodeTemplateAttrs.equals("AddWcdmaPicoExample_16B")) {

            nodeTemplateAttrs.put(BatchCSVColumns.RNC_NAME, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.GROUP_NAME, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.NODE_NAME, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.FTP_SW_STORE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.FTP_AUTO_INTEGRATION, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put(BatchCSVColumns.SITE, EMPTY_STRING_PLACEHOLDER);
            nodeTemplateAttrs.put("autoIntegration", EMPTY_STRING_PLACEHOLDER);

        }

        return nodeTemplateAttrs;
    }

    private void updateNodeTemplateAttributes(
            final LinkedHashMap<String, String> attributes,
            final String rncName,
            final String groupName,
            final String nodeName,
            final String ftpStore,
            final String ftpAutoIntegrate,
            final String site,
            final String nodeVersion,
            final String associatedRnc) {

        // update with dynamic values
        attributes.put(BatchCSVColumns.RNC_NAME, rncName);
        attributes.put(BatchCSVColumns.GROUP_NAME, groupName);
        attributes.put(BatchCSVColumns.NODE_NAME, nodeName);
        attributes.put(BatchCSVColumns.NODE_VERSION, nodeVersion);
        attributes.put(BatchCSVColumns.FTP_SW_STORE, ftpStore);
        attributes.put(BatchCSVColumns.FTP_AUTO_INTEGRATION, ftpAutoIntegrate);
        attributes.put(BatchCSVColumns.SITE, site);
        if (nodeVersion.compareTo("16B") >= 0) {
            attributes.put("Associated RNC", associatedRnc);
        }
        attributes.put("autoIntegration", "1");

    }

    protected LinkedHashMap<String, String> getTransportTemplateAttributesByTemplateName(
            final String templateName,
            final String rncName,
            final String nodeName,
            final String planName) {

        log.info("BsimWRANBatchDataProvider.getTransportTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();

        // Values Propagated in Bsim Client
        transportTemplateAttrs.put("RNC Name", rncName);
        transportTemplateAttrs.put("Node Name", nodeName);
        transportTemplateAttrs.put("Plan", planName);
        transportTemplateAttrs.put("IUB ID", "1");
        transportTemplateAttrs.put("rbsId", "1");

        if (templateName.equals("WcdmaPicoRNCTNBulkCMExample")) {
            transportTemplateAttrs.put("IpAccessHostPool", "1");
        }

        return transportTemplateAttrs;
    }

    protected LinkedHashMap<String, String> getRadioTemplateAttributesByTemplateName(
            final String templateName,
            final String rncName,
            final String nodeName,
            final String planName) {

        log.info("BsimWRANBatchDataProvider.getRadioTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();

        // Values Propagated in Bsim Client
        radioTemplateAttrs.put("RNC Name", rncName);
        radioTemplateAttrs.put("Node Name", nodeName);
        radioTemplateAttrs.put("Plan", planName);

        if (templateName.equals("1CellWcdmaPicoRNBulkCMExample") || templateName.equals("WcdmaPicoRNBulkCMTemplateFile")) {
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
        }
	if (templateName.equals("1CellWcdmaPicoRNBulkCMExample")) {
            radioTemplateAttrs.put("Rbsid", "1");
            radioTemplateAttrs.put("Cell 1: rac", "1");
        }
        if (templateName.equals("WcdmaPicoRNBulkCMTemplateFile")) {
            radioTemplateAttrs.put("Cell 2: localCellId", "1");
            radioTemplateAttrs.put("Cell 2: uarfcnUl", "1");
            radioTemplateAttrs.put("Cell 2: uarfcnDl", "1");
            radioTemplateAttrs.put("Cell 2: primaryScramblingCode", "1");
            radioTemplateAttrs.put("Cell 2: cId", "1");
            radioTemplateAttrs.put("Cell 2: User Label", "1");
            radioTemplateAttrs.put("Cell 2: lac", "1");
            radioTemplateAttrs.put("Cell 2: sac", "1");
            radioTemplateAttrs.put("Cell 2: tCell", "1");
            radioTemplateAttrs.put("Cell 2: sib1PlmnScopeValueTag", "1");

            radioTemplateAttrs.put("Cell 3: localCellId", "1");
            radioTemplateAttrs.put("Cell 3: uarfcnUl", "1");
            radioTemplateAttrs.put("Cell 3: uarfcnDl", "1");
            radioTemplateAttrs.put("Cell 3: primaryScramblingCode", "1");
            radioTemplateAttrs.put("Cell 3: cId", "1");
            radioTemplateAttrs.put("Cell 3: User Label", "1");
            radioTemplateAttrs.put("Cell 3: lac", "1");
            radioTemplateAttrs.put("Cell 3: sac", "1");
            radioTemplateAttrs.put("Cell 3: tCell", "1");
            radioTemplateAttrs.put("Cell 3: sib1PlmnScopeValueTag", "1");
        }

        return radioTemplateAttrs;
    }

    protected LinkedHashMap<String, String> getIcfTemplateAttributesByTemplateName(final String templateName, final String dnPrefix, final String nodeName) {

        log.info("BsimWRANBatchDataProvider.getIcfTemplateAttributesByTemplateName() templateName == " + templateName);
        final LinkedHashMap<String, String> autoIntWranInitialCngAttrs = new LinkedHashMap<String, String>();

        if (templateName.equals("WRANInitialConfigurationFileExample_14A") || templateName.equals("WRANInitialConfigurationFileExample_IPSEC_14A")
                || templateName.equals("WRAN_ICF_Example_Manual") || templateName.equals("WRAN_ICF_Example_Manual_IPSEC")) {

            final Properties properties = new Properties();
            try {
                properties.load(BsimWRANPicoBatchDataProvider.class.getResourceAsStream("/Icfpropertiesfiles/" + templateName + ".properties"));
            } catch (final Exception e) {
                log.error("Unable to get the properties file for template:" + templateName);
            }

            for (final String key : properties.stringPropertyNames()) {
                if (key.equals("dnPrefix")) {
                    autoIntWranInitialCngAttrs.put(key, dnPrefix);
                } else if (key.equals("nodeName")) {
                    autoIntWranInitialCngAttrs.put("Node Name", nodeName);
                } else if (key.equals("Save_as")) {
                    autoIntWranInitialCngAttrs.put("Save as", "");
                } else if (key.equals("Cell_1_")) {
                    autoIntWranInitialCngAttrs.put("Cell 1 : localCellId", "1");
                } else if (key.equals("User_Name")) {
                    autoIntWranInitialCngAttrs.put("User Name", "");
                } else if (key.equals("IUB_ID")) {
                    autoIntWranInitialCngAttrs.put("IUB ID", "1");
                } else {
                    final String value = properties.getProperty(key);
                    autoIntWranInitialCngAttrs.put(key, value);
                }

            }
        }
        log.info(templateName + ": Temlate Attributes are " + autoIntWranInitialCngAttrs);

        return autoIntWranInitialCngAttrs;
    }

    @SuppressWarnings("unused")
    private String checkAndRetreiveCSRncIfRequired(final int rowNo) {

        final String rncName;
        if (retrieveCellValueFromCSV(BatchCSVColumns.RNC_NAME, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_RNC)) {
            rncName = retrieveServerDataOperator.getFirstAvailableRnc();

        } else {
            rncName = retrieveCellValueFromCSV(BatchCSVColumns.RNC_NAME, rowNo);
        }
        return rncName;
    }

    @SuppressWarnings("unused")
    private String checkAndRetreiveCSGroupNameIfRequired(final int rowNo) {

        final String rncName = retrieveServerDataOperator.getFirstAvailableRnc();
        if (retrieveCellValueFromCSV(BatchCSVColumns.GROUP_NAME, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_GROUP_NAME)) {
            final String commandOutput = commandExecutor.simpleExec(String.format(getGroupNameSSHCommand, rncName + ","));
            final String[] regexSplit = commandOutput.split("\\r?\\n");
            final String groupFDN = regexSplit[0];
            log.info("Group found, using " + groupFDN);
            return groupFDN;
        } else {
            final String groupName = retrieveCellValueFromCSV(BatchCSVColumns.GROUP_NAME, rowNo);
            return "SubNetwork=ONRM_ROOT_MO,SubNetwork=" + rncName + ",Group=" + groupName;
        }
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
        return ftpAutoIntegration;
    }

    private String checkAndRetreiveCSSiteIfRequired(final int rowNo) {

        final String site;
        if (retrieveCellValueFromCSV(BatchCSVColumns.SITE, rowNo).equalsIgnoreCase(BatchCSVColumns.CS_RETRIEVED_SITE)) {
            site = retrieveServerDataOperator.getAvailableSiteName();
        } else {
            site = retrieveCellValueFromCSV(BatchCSVColumns.SITE, rowNo);
        }
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

    private void setNodeFdnValues(final int batchSize, final String text, final String numberRange, final String numberRangeCustom, final String rncName) {

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
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom only node name assignment
        if (numberRangeCustomSize == batchSize && text.equals("") && numberRange.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRange and text node name assignment
        if (numberRangeSize == batchSize && !text.equals("") && numberRangeCustom.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + text + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom and text node name assignment
        if (numberRangeCustomSize == batchSize && !text.equals("") && numberRange.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + text + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom, NumberRange node name assignment
        if (numberRangeCustomSize == batchSize && numberRangeSize == batchSize && text.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + i + i;
                nodeFdns.add(fdn);
            }
        }
        // NumberRangeCustom, NumberRange and text node name assignment
        if (numberRangeCustomSize == batchSize && numberRangeSize == batchSize && !text.equals("")) {
            for (int i = 1; i <= batchSize; i++) {
                final String fdn = "SubNetwork=ONRM_ROOT_MO_R,SubNetwork=" + rncName + ",MeContext=" + text + i + i;
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

    private boolean parseBooleanVariable(final String val) {

        if ("true".equalsIgnoreCase(val)) {
            // only return true if it is a String of true
            return true;
        } else {
            return false;
        }
    }

    protected static class BatchCSVColumns {

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

        static final String USE_SC = "Enable SC Assignment";

        static final String SC_PROFILE_NAME = "SC Profile Name";

        // Auto Rule
        static final String AUTO_RULE_TEXT = "Auto Rule Text";

        static final String AUTO_RULE_NUMBER_RANGE = "Auto Rule Number Range";

        static final String AUTO_RULE_NUMBER_RANGE_CUSTOM = "Auto Rule Number Range Custom";

        // Plan
        static final String AUTO_PLAN = "Auto Plan";

        static final String PLAN_NAME = "Plan Name";

        // Other
        static final String USER_NAME = "User Name";

        // Node Template
        static final String OSS_NODE_TEMPLATE = "OSS Node Template";

        static final String RNC_NAME = "RNC Name";

        static final String GROUP_NAME = "Group Name";

        static final String NODE_NAME = "Node Name";

        static final String NODE_VERSION = "Node Version";

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

        // AI specific below
        static final String IS_END_TO_END = "isEndToEnd";

        static final String IP_ADDRESS = "IP Address";

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

    private String retrieveIpAddress(final String rncFdn) {

        final String retrieveSubRncCommand = LIST_MO + rncFdn + " -l 1";

        final String subRncCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + retrieveSubRncCommand).trim();

        final String[] rbsGroupList = subRncCommandResult.split("\n");

        String retrieveIpCommand = null;

        for (final String element : rbsGroupList) {

            if (element.contains("MeContext")) {

                retrieveIpCommand = LIST_ATTRIBUTE + element + " ipAddress";
                break;

            }

        }

        final String ipCommandResult = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + retrieveIpCommand).trim();

        return ipCommandResult.split("\"")[1];

    }

    private String retrieveAssociatedRncSubNetworkName(final String rncFdn) {
        return rncFdn + ",ManagedElement=1,RncFunction=1";
    }

}

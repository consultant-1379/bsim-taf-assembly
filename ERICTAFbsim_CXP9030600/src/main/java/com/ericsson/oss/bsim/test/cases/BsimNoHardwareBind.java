package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.Output;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.utils.csv.CsvReader;
import com.ericsson.oss.bsim.batch.data.model.LRANBatchConfig;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.NoHardwareBindContent;
import com.ericsson.oss.bsim.cli.test.configuration.BsimCliTestConfigurationType;
import com.ericsson.oss.bsim.data.CsvItemRetriever;
import com.ericsson.oss.bsim.data.MockLRANPicoBatchBuilder;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimBatchOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

public class BsimNoHardwareBind extends TorTestCaseHelper implements TestCase {

    private static Logger log = Logger.getLogger(BsimNoHardwareBind.class);

    BsimBatchOperator bsimBatchOperator;

    Set<String> filesToDelete = new LinkedHashSet<String>();

    List<LRANBatchConfig> batchConfigurationList = new ArrayList<LRANBatchConfig>();

    BsimCliTestConfigurationType testConfigType;

    CsvReader batchCsvReader;

    CsvReader noHardwareBindCsvReader;

    @BeforeClass
    public void prepareTheRun() {

        log = Logger.getLogger(BsimNoHardwareBind.class);
        setTestcase("BSIM_PREPARE_FOR_CLI", "Preparation for CLI Test Run");

        setTestStep("Do pre-check for PICO_LTE");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.PICO_LTE, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());

    }

    @DataDriven(name = "Bsim_No_Hardware_Bind")
    @Context(context = { Context.API })
    @Test
    public void testCommands(
            @Input("tcID") final String tcId,
            @Input("tcTitle") final String tcTitle,
            @Input("TC Desc") final String tcDesc,
            @Input("batchCSVFile") final String batchCSVFile,
            @Input("batchConfigs") final String batchConfigs,
            @Input("validationString") final String validationString,
            @Output("result") final String result) {

        setTestcase(tcId, tcTitle);
        setTestInfo(tcDesc);

        // initialize variables
        bsimBatchOperator = new BsimBatchOperator();
        try {
            batchCsvReader = DataHandler.readCsv(batchCSVFile, ",");
            noHardwareBindCsvReader = DataHandler.readCsv("No_Hardware_bind_Configs.csv", ",");
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        try {
            setTestStep("Parse test data in csv file");
            parseBatchConfigTestData(batchConfigs);

            setTestStep("Add batches");
            for (final LRANBatchConfig batchConfig : batchConfigurationList) {
                if (batchConfigs != null && !batchConfigs.equalsIgnoreCase("")) {
                    addLRANBatch(batchConfig.getMockLRANPicoBatch(), "Not Started");
                }
            }

            // TODO finish implementing methods in operators
            // Execute NoHardware
            // for (final LRANBatchConfig batchConfig : batchConfigurationList) {
            // final List<String> noHardwardBindFdns;
            // for (int i = 1; i <= batchConfig.getBatchSize(); i++) {
            // result = bsimBatchOperator.noHardwareBindLRANBatch(batchConfig.getMockLRANPicoBatch().getName(),
            // batchConfig.getNoHardwareBind()
            // .getSaveToLocation(), batchConfig.getNoHardwareBind().getSiteInstallTemplateName(), batchConfig.getNoHardwareBind()
            // .getSiteInstallTemplateAttrs());
            // }
            // }

        } finally {

            cleanUp();
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        for (final LRANBatchConfig batchConfig : batchConfigurationList) {
            deleteBatch(bsimBatchOperator, batchConfig.getMockLRANPicoBatch(), "true");
        }
        log.info("There are " + batchConfigurationList.size() + " deleted from server.");

        final List<String> nodesToDelete = new ArrayList<String>();
        for (final LRANBatchConfig batchConfig : batchConfigurationList) {
            for (final String s : batchConfig.getMockLRANPicoBatch().getNodeFdnValues()) {
                if (bsimBatchOperator.moExist(s).equals("true")) {
                    nodesToDelete.add(s);
                }
            }
        }
        if (!nodesToDelete.isEmpty()) {
            deleteBindLRANBatchNodes(bsimBatchOperator, nodesToDelete, "Successful");
        }

        batchConfigurationList.clear();
    }

    public void parseBatchConfigTestData(final String batchConfigs) {

        final HashMap<String, Integer> batchMap = processBatchConfigs(batchConfigs);
        for (final Entry<String, Integer> entry : batchMap.entrySet()) {

            for (int i = 1; i <= entry.getValue(); i++) {
                final LRANBatchConfig temp = new LRANBatchConfig();
                final String batchConfigId = entry.getKey().toString();
                final MockLRANPicoBatch tempBatch = getMockLRANBatchObject(batchConfigId, batchConfigId + "_BATCH" + i);
                temp.setMockLRANPicoBatch(tempBatch);
                final CsvItemRetriever csvItemRetriever = new CsvItemRetriever();
                final int rowNo = csvItemRetriever.retrieveAttribureRowNumber(batchCsvReader, "BatchConfig ID", entry.getKey().toString());
                final int batchSize = Integer.parseInt(csvItemRetriever.retrieveCellValueFromCSV(batchCsvReader, "Batch Size", rowNo));
                temp.setBatchSize(batchSize);
                final String isNoHardwareBind = csvItemRetriever.retrieveCellValueFromCSV(batchCsvReader, "noHardwareBind", rowNo);
                if (isNoHardwareBind.equalsIgnoreCase("true")) {
                    temp.setNoHardwareBind(true);
                    final HashMap<NoHardwareBindContent, Integer> noHardwareBindContents = new HashMap<NoHardwareBindContent, Integer>();
                    temp.setNoHardwareBindconfigs(noHardwareBindContents);
                    final String noHardwareBindConfigs = csvItemRetriever.retrieveCellValueFromCSV(batchCsvReader, "noHardwareBindConfig", rowNo);
                    processNoHardwareBindContents(noHardwareBindConfigs, temp);

                } else {
                    temp.setNoHardwareBind(false);
                }

                batchConfigurationList.add(temp);
            }
        }
    }

    private void processNoHardwareBindContents(final String configs, final LRANBatchConfig batchConfig) {

        final Pattern noHardwareBindConfigNamesAndNumberOfConfigs = Pattern.compile("<(.*?)=(.*?)>");
        String numOfNoHardwareBindContent = null;

        final Matcher matcher = noHardwareBindConfigNamesAndNumberOfConfigs.matcher(configs);

        while (matcher.find()) {
            final String noHardwareBindContentString = matcher.group(1);
            numOfNoHardwareBindContent = matcher.group(2);
            parseNoHardwareBindConfigTestData(noHardwareBindContentString, Integer.parseInt(numOfNoHardwareBindContent), batchConfig);
        }
    }

    private void parseNoHardwareBindConfigTestData(final String noHardwareBindConfig, final int numOfNoHardwareBindConfig, final LRANBatchConfig batchConfig) {

        final CsvItemRetriever csvItemRetriever = new CsvItemRetriever();
        final int rowNo = csvItemRetriever.retrieveAttribureRowNumber(noHardwareBindCsvReader, "NoHardwareBindConfig ID", noHardwareBindConfig);
        final String templateName = csvItemRetriever.retrieveCellValueFromCSV(noHardwareBindCsvReader, "Template Name" + "", rowNo);
        final NoHardwareBindContent noHardwareBindInfo = new NoHardwareBindContent();
        noHardwareBindInfo.setSiteInstallTemplateName(templateName);
        final String saveToLocation = csvItemRetriever.retrieveCellValueFromCSV(noHardwareBindCsvReader, "SaveToLocation" + "", rowNo);
        noHardwareBindInfo.setSaveToLocation(saveToLocation);
        final LinkedHashMap<String, String> siteInstallTemplateAttrs = new LinkedHashMap<String, String>();
        final String integrationOamIpAddress = csvItemRetriever.retrieveCellValueFromCSV(noHardwareBindCsvReader, "Integration OAM IP Address" + "", rowNo);
        if (integrationOamIpAddress != null && !"".equals(integrationOamIpAddress)) {
            siteInstallTemplateAttrs.put("Integration OAM IP Address", integrationOamIpAddress);
        }
        final String integrationOuterIpAddress = csvItemRetriever.retrieveCellValueFromCSV(noHardwareBindCsvReader, "Integration Outer IP Address" + "", rowNo);
        if (integrationOuterIpAddress != null && !"".equals(integrationOuterIpAddress)) {

            siteInstallTemplateAttrs.put("Integration Outer IP Address", integrationOuterIpAddress);
        }
        noHardwareBindInfo.setSiteInstallTemplateAttrs(siteInstallTemplateAttrs);
        batchConfig.getNoHardwareBindconfigs().put(noHardwareBindInfo, numOfNoHardwareBindConfig);

    }

    private HashMap<String, Integer> processBatchConfigs(final String configs) {

        final HashMap<String, Integer> batchMap = new HashMap<String, Integer>();
        final Pattern pattern = Pattern.compile("<(.*?)=(.*?)>");
        String batchConfig = null;
        String numOfBatches = null;

        final Matcher matcher = pattern.matcher(configs);

        while (matcher.find()) {
            batchConfig = matcher.group(1);
            numOfBatches = matcher.group(2);
            batchMap.put(batchConfig, Integer.parseInt(numOfBatches));
        }

        return batchMap;
    }

    private MockLRANPicoBatch getMockLRANBatchObject(final String batchID, final String customizedBatchName) {

        MockLRANPicoBatch mockLRANPicoBatch = new MockLRANPicoBatch();

        final MockLRANPicoBatchBuilder mockLRANPicoBatchBuilder = new MockLRANPicoBatchBuilder(mockLRANPicoBatch, batchCsvReader);
        mockLRANPicoBatch = mockLRANPicoBatchBuilder.prepareLRANPicoBatchTestData(batchID, customizedBatchName);
        return mockLRANPicoBatch;
    }

    private void addLRANBatch(final MockLRANPicoBatch mockLRANPicoBatch, final String expectedResult) {

        log.info("Add LRAN Batch to CS via calling BSIM Service...");
        final String actualResult = bsimBatchOperator.addLRANBatch(mockLRANPicoBatch);
        Assert.assertEquals(actualResult, expectedResult);
    }

    private String deleteBatch(final BsimBatchOperator bsimBatchOperator, final MockLRANPicoBatch mockLRANPicoBatch, final String expectedResult) {

        log.info("Deleting LRAN via calling BSIM Service...");
        final String actualResult = bsimBatchOperator.deleteLRANBatch(mockLRANPicoBatch);
        Assert.assertEquals(actualResult, expectedResult);
        return actualResult;
    }

    private String deleteBindLRANBatchNodes(final BsimBatchOperator bsimBatchOperator, final List<String> nodesToDelete, final String expectedResult) {

        log.info("Deleteing LRAN batch nodes via calling BSIM Service...");
        final String actualResult = bsimBatchOperator.deletebindLRANBatchNodes(nodesToDelete);
        Assert.assertEquals(actualResult, expectedResult);
        return actualResult;
    }

}

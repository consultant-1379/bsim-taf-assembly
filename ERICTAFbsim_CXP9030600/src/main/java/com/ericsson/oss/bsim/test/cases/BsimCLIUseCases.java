package com.ericsson.oss.bsim.test.cases;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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
import com.ericsson.oss.bsim.operators.BsimCliOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.utils.BatchSerialNumberGenerator;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;
import com.ericsson.oss.bsim.utils.file.LocalTempFileConstants;

public class BsimCLIUseCases extends TorTestCaseHelper implements TestCase {

    private static Logger log = Logger.getLogger(BsimCLIUseCases.class);

    BsimBatchOperator bsimBatchOperator;

    BsimCliOperator bsimCliOperator;

    Set<String> filesToDelete = new LinkedHashSet<String>();

    List<String> combindedOutputFiles = new ArrayList<String>();

    List<LRANBatchConfig> batchConfigurationList = new ArrayList<LRANBatchConfig>();

    BsimCliTestConfigurationType testConfigType;

    CsvReader csvReader;

    CsvReader noHardwareBindCsvReader;

    @BeforeClass
    public void prepareTheRun() {

        log = Logger.getLogger(BsimCLIUseCases.class);
        setTestcase("BSIM_PREPARE_FOR_CLI", "Preparation for CLI Test Run");

        setTestStep("Do pre-check for PICO_LTE");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.PICO_LTE, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());

    }

    @DataDriven(name = "Bsim_Cli_Create_Commands")
    @Context(context = { Context.API })
    @Test
    public void testCommands(
            @Input("tcID") final String tcId,
            @Input("tcTitle") final String tcTitle,
            @Input("TC Desc") final String tcDesc,
            @Input("batchCSVFile") final String batchCSVFile,
            @Input("batchConfigs") final String batchConfigs,
            @Input("command type") final String commandType,
            @Input("command") String command,
            @Input("xmlFile") final String inputXmlFile,
            @Input("outputXmlFile") final String outputXmlFile,
            @Input("validationString") final String validationString,
            @Output("result") final String result) {

        setTestcase(tcId, tcTitle);
        setTestInfo(tcDesc);

        // initialize variables
        bsimBatchOperator = new BsimBatchOperator();
        bsimCliOperator = new BsimCliOperator();
        try {
            csvReader = DataHandler.readCsv(batchCSVFile, ",");
            noHardwareBindCsvReader = DataHandler.readCsv("No_Hardware_bind_Configs.csv", ",");
        } catch (final Exception ex) {
            ex.printStackTrace();
        }

        testConfigType = BsimCliTestConfigurationType.valueOf(commandType);

        try {
            setTestStep("Parse test data in csv file");
            parseBatchConfigTestData(batchConfigs);

            if (testConfigType.isAddBatch()) {
                setTestStep("Add batches");
                for (final LRANBatchConfig batchConfig : batchConfigurationList) {
                    if (batchConfigs != null && !batchConfigs.equalsIgnoreCase("")) {
                        addLRANBatch(batchConfig.getMockLRANPicoBatch(), "Not Started");
                    }
                    setTestStep("Bind nodes");
                    if (batchConfig.getNodesToBind() > 0 && !testConfigType.name().contains("CREATE")) {
                        bindLRANBatch(bsimBatchOperator, batchConfig.getMockLRANPicoBatch(), batchConfig.getNodesToBind(), "Successful",
                                batchConfig.getHardwareIdPrefix());
                    }
                }
            }

            setTestStep("Generate command input file ");
            if (testConfigType.isTransferInputFile()) {
                final String tempFileName = generateAndTransferInputFile(inputXmlFile, batchConfigurationList);
                final int index = inputXmlFile.lastIndexOf("/");
                final String fileName = inputXmlFile.substring(index + 1);
                command = command.replaceAll(fileName, tempFileName);
            }

            setTestStep("Execute BSIM CLI command");
            final String response = executeCliCommand(command);

            setTestStep("Verification");
            doVerification(response, outputXmlFile, validationString);

        } finally {
            setTestStep("Clean CLI Clean up");
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

        if (!filesToDelete.isEmpty()) {
            for (final String s : filesToDelete) {
                bsimCliOperator.deleteRemoteFile(s);
            }
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
                final int rowNo = csvItemRetriever.retrieveAttribureRowNumber(csvReader, "BatchConfig ID", entry.getKey().toString());
                final String nodesToBind = csvItemRetriever.retrieveCellValueFromCSV(csvReader, "Nodes to Bind", rowNo);
                temp.setNodesToBind(Integer.parseInt(nodesToBind));
                final String configID = csvItemRetriever.retrieveCellValueFromCSV(csvReader, "Config Identifier", rowNo);
                // temp.setHardwareID(configID + "B00" + i);
                temp.setHardwareIdPrefix(configID);
                // get batch size
                final int batchSize = Integer.parseInt(csvItemRetriever.retrieveCellValueFromCSV(csvReader, "Batch Size", rowNo));
                temp.setBatchSize(batchSize);
                final String isNoHardwareBind = csvItemRetriever.retrieveCellValueFromCSV(csvReader, "noHardwareBind", rowNo);
                if (isNoHardwareBind.equalsIgnoreCase("true")) {
                    temp.setNoHardwareBind(true);
                    final HashMap<NoHardwareBindContent, Integer> noHardwareBindContents = new HashMap<NoHardwareBindContent, Integer>();
                    temp.setNoHardwareBindconfigs(noHardwareBindContents);
                    final String noHardwareBindConfigs = csvItemRetriever.retrieveCellValueFromCSV(csvReader, "noHardwareBindConfig", rowNo);
                    processNoHardwareBindContents(noHardwareBindConfigs, temp);

                } else {
                    temp.setNoHardwareBind(false);
                }

                batchConfigurationList.add(temp);
            }
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
        final Pattern batchConfigNamesAndNumberOfConfigs = Pattern.compile("<(.*?)=(.*?)>");
        String batchConfig = null;
        String numOfBatches = null;

        final Matcher matcher = batchConfigNamesAndNumberOfConfigs.matcher(configs);

        while (matcher.find()) {
            batchConfig = matcher.group(1);
            numOfBatches = matcher.group(2);
            batchMap.put(batchConfig, Integer.parseInt(numOfBatches));
        }

        return batchMap;
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

    private MockLRANPicoBatch getMockLRANBatchObject(final String batchID, final String customizedBatchName) {

        MockLRANPicoBatch mockLRANPicoBatch = new MockLRANPicoBatch();

        final MockLRANPicoBatchBuilder mockLRANPicoBatchBuilder = new MockLRANPicoBatchBuilder(mockLRANPicoBatch, csvReader);
        mockLRANPicoBatch = mockLRANPicoBatchBuilder.prepareLRANPicoBatchTestData(batchID, customizedBatchName);
        return mockLRANPicoBatch;
    }

    private void addLRANBatch(final MockLRANPicoBatch mockLRANPicoBatch, final String expectedResult) {

        log.info("Add LRAN Batch to CS via calling BSIM Service...");
        final String actualResult = bsimBatchOperator.addLRANBatch(mockLRANPicoBatch);
        Assert.assertEquals(actualResult, expectedResult);
    }

    private List<String> bindLRANBatch(
            final BsimBatchOperator bsimBatchOperator,
            final MockLRANPicoBatch mockLRANPicoBatch,
            final int nodesToBind,
            final String expectedResult,
            final String hardwareID) {

        log.info("Binding LRAN batch nodes via calling BSIM Service...");

        final ArrayList<String> serials = new BatchSerialNumberGenerator().generateSerialNumbersList(hardwareID, 1, nodesToBind);
        final List<String> actualResult = bsimBatchOperator.bindLRANBatch(mockLRANPicoBatch, nodesToBind, serials);
        String success = "";
        log.info("bindLRANBatch:  actualResult size: " + actualResult.size() + ", nodesToBind size: " + nodesToBind);
        if (actualResult.size() == nodesToBind) {
            success = "Successful";
        }
        Assert.assertEquals(success, expectedResult);
        return actualResult;
    }

    private String generateAndTransferInputFile(final String inputXmlFile, final List<LRANBatchConfig> batchConfigurationList) {

        final int index = inputXmlFile.lastIndexOf("/");
        final String remoteDir = inputXmlFile.substring(0, index + 1);
        final String fileName = inputXmlFile.substring(index + 1);

        // generate Create command input file
        final File createCommandInputFileLocalPath = writeContentToInputXmlFile(fileName, batchConfigurationList);

        // Transferring xml file to server step
        if (createCommandInputFileLocalPath != null) {
            bsimCliOperator.copyFileToRemoteHost(createCommandInputFileLocalPath.getAbsolutePath(), remoteDir);
            filesToDelete.add(remoteDir + createCommandInputFileLocalPath.getName());
            createCommandInputFileLocalPath.deleteOnExit();
        } else {
            Assert.fail("Generate local CreateInputFile failed!");
        }

        return createCommandInputFileLocalPath.getName();
    }

    private File writeContentToInputXmlFile(final String xmlFile, final List<LRANBatchConfig> batchConfigurationList) {

        File createInputFile = null;
        try {

            final DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            final DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            final Document doc = docBuilder.newDocument();
            final Element rootElement = doc.createElement("bbf:BSIMBindFile");
            doc.appendChild(rootElement);

            rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
            rootElement.setAttribute("xmlns:bbf", "http://www.ericsson.se/BsimBindFileXMLSchema");
            rootElement.setAttribute("xsi:schemaLocation", "http://www.ericsson.se/BsimBindFileXMLSchema BSIMBindFileSchema.xsd");

            // bindList elements
            final Element bindList = doc.createElement("BSIMBindList");
            bindList.setAttribute("filecreationtime", "0");
            rootElement.appendChild(bindList);

            if (testConfigType.isBindType().equalsIgnoreCase("BATCH")) {
                int noHardwareBindCount = 0;
                int i = 0;
                for (final LRANBatchConfig batchConfig : batchConfigurationList) {
                    i++;
                    if (batchConfig.isNoHardwareBind()) {
                        noHardwareBindCount = generateBatchNoHardwareBindInput(doc, bindList, i, batchConfig);
                    }
                    if (noHardwareBindCount < batchConfig.getBatchSize()) {
                        generateBatchBindInput(doc, bindList, i, noHardwareBindCount, batchConfig);
                    }
                }
            }

            if (testConfigType.isBindType().equalsIgnoreCase("NODE")) {
                int i = 0;
                for (final LRANBatchConfig batchConfig : batchConfigurationList) {
                    i++;
                    // Bind elements
                    generateNodeBindInput(doc, bindList, i, batchConfig);
                }
            }
            // write the content into xml file
            final TransformerFactory transformerFactory = TransformerFactory.newInstance();
            final Transformer transformer = transformerFactory.newTransformer();
            final DOMSource source = new DOMSource(doc);
            createInputFile = new File(LocalTempFileConstants.getLocalTempDirName() + File.separator + xmlFile);
            createInputFile.deleteOnExit();
            final StreamResult result = new StreamResult(createInputFile);

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            // Output to console for testing
            // StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);

            log.info("Local CLI File saved!" + createInputFile.getAbsolutePath());
        } catch (final ParserConfigurationException pce) {
            log.error(pce.getMessage());
            log.error(pce.getStackTrace());
        } catch (final TransformerException tfe) {
            log.error(tfe.getMessage());
            log.error(tfe.getStackTrace());
        }
        return createInputFile;
    }

    private void generateBatchBindInput(
            final Document doc,
            final Element bindList,
            final int i,
            final int noHardwareBindCount,
            final LRANBatchConfig batchConfig) {
        final Element bind = doc.createElement("Bind");
        bindList.appendChild(bind);

        // set attribute to Bind element
        bind.setAttribute("number", Integer.toString(i - 1));
        bind.setAttribute("bindtype", "BATCH");
        final String batchUri = batchConfig.getMockLRANPicoBatch().getName();

        log.info("BatchUri value: " + batchUri);

        bind.setAttribute("uri", batchUri);
        String hardwareId;
        // Handle ID for update command
        if (testConfigType.name().contains("UPDATE")) {
            final String sub = batchConfig.getHardwareIdPrefix().substring(1);
            hardwareId = "U" + sub;
        } else {
            hardwareId = batchConfig.getHardwareIdPrefix();
        }
        final String serialNumbers = new BatchSerialNumberGenerator().generateSerialNumbersString(hardwareId, i, batchConfig.getBatchSize()
                - noHardwareBindCount);

        log.info("serial numbers generated: " + serialNumbers);

        bind.setAttribute("hardwareids", serialNumbers);
    }

    private int generateBatchNoHardwareBindInput(final Document doc, final Element bindList, final int i, final LRANBatchConfig batchConfig) {
        final Element NoHardwareBind = doc.createElement("NoHardwareBind");
        bindList.appendChild(NoHardwareBind);
        NoHardwareBind.setAttribute("number", Integer.toString(i - 1));
        NoHardwareBind.setAttribute("bindtype", "BATCH");
        final String batchUri = batchConfig.getMockLRANPicoBatch().getName();
        log.info("BatchUri value: " + batchUri);
        NoHardwareBind.setAttribute("uri", batchUri);
        int noHardwareBindCount = 0;
        for (final Entry<NoHardwareBindContent, Integer> noHardwareBindconfig : batchConfig.getNoHardwareBindconfigs().entrySet()) {
            noHardwareBindCount = writeNoHardwareBindContents(doc, NoHardwareBind, noHardwareBindconfig);
        }
        return noHardwareBindCount;
    }

    private int writeNoHardwareBindContents(final Document doc, final Element NoHardwareBind, final Entry<NoHardwareBindContent, Integer> noHardwareBindconfig) {
        int noHardwareBindCount = 0;
        for (int j = 0; j < noHardwareBindconfig.getValue(); j++) {
            final Element NoHardwareBindContent = doc.createElement("NoHardwareBindContent");
            NoHardwareBind.appendChild(NoHardwareBindContent);
            NoHardwareBindContent.setAttribute("saveAsGeneratedConfigFile", noHardwareBindconfig.getKey().getSaveToLocation());
            filesToDelete.add(noHardwareBindconfig.getKey().getSaveToLocation());
            combindedOutputFiles.add(noHardwareBindconfig.getKey().getSaveToLocation());
            NoHardwareBindContent.setAttribute("siteInstallTemplateName", noHardwareBindconfig.getKey().getSiteInstallTemplateName());

            for (final Map.Entry<String, String> entry : noHardwareBindconfig.getKey().getSiteInstallTemplateAttrs().entrySet()) {
                final Element SubstitutionAttribute = doc.createElement("SubstitutionAttribute");
                NoHardwareBindContent.appendChild(SubstitutionAttribute);
                SubstitutionAttribute.setAttribute("name", entry.getKey());
                SubstitutionAttribute.setAttribute("value", entry.getValue());
            }
            noHardwareBindCount++;
        }
        return noHardwareBindCount;
    }

    private void generateNodeBindInput(final Document doc, final Element bindList, final int i, final LRANBatchConfig batchConfig) {
        final Element bind = doc.createElement("Bind");
        bindList.appendChild(bind);

        // set attribute to Bind element
        bind.setAttribute("number", Integer.toString(i - 1));
        bind.setAttribute("bindtype", "NODE");
        final String batchUri = batchConfig.getMockLRANPicoBatch().getNodeFdnValues().get(i - 1);

        log.info("BatchUri value: " + batchUri);

        bind.setAttribute("uri", batchUri);
        String hardwareId;
        // Handle ID for update command
        if (testConfigType.name().contains("UPDATE")) {
            final String sub = batchConfig.getHardwareIdPrefix().substring(1);
            hardwareId = "U" + sub;
        } else {
            hardwareId = batchConfig.getHardwareIdPrefix();
        }
        final ArrayList<String> serialNumbers = new BatchSerialNumberGenerator().generateSerialNumbersList(hardwareId, i, batchConfig.getBatchSize());

        log.info("serial number used: " + serialNumbers.get(i - 1));

        bind.setAttribute("hardwareids", serialNumbers.get(i - 1));
    }

    private String executeCliCommand(final String command) {

        log.info("Executing CLI command...");
        log.info("command to be executed" + command);

        final String response = bsimCliOperator.executeCommand(command);
        log.info("CLI command response: " + response);

        return response;
    }

    private void doVerification(final String commandOutput, final String outputXmlFile, final String validationString) {

        // verify command output
        boolean singleResult = true;
        boolean overallResult = true;

        if (testConfigType.isValidateByString()) {
            singleResult = processValidationStrings(validationString, commandOutput);
            overallResult &= singleResult;
            log.info("Validated string result: " + singleResult);
        }

        if (testConfigType.isBind() && testConfigType.name().equalsIgnoreCase("CREATE_WITH_FILE_BATCH")) {
            singleResult &= verifyCreateCommandOutput(commandOutput);
            overallResult &= singleResult;
            log.info("Verified create command output: " + singleResult);
        }

        // verify output xml file
        if (testConfigType.isOutputFileGenerated()) {
            singleResult &= verifyOutputFile(outputXmlFile);
            overallResult &= singleResult;
            log.info("Verified output file was generated: " + singleResult);
        }

        if (combindedOutputFiles.size() > 0) {
            for (int i = 0; i < combindedOutputFiles.size(); i++) {
                singleResult &= verifyOutputFile(combindedOutputFiles.get(i));
                overallResult &= singleResult;
            }
            log.info("Verified combinded output files were generated: " + singleResult);
        }

        // verify nodes in database
        if (testConfigType.isBind() && !testConfigType.name().contains("DELETE")) {
            singleResult &= verifyBindsInDatabase();
            overallResult &= singleResult;
            log.info("Verified binds in database: " + singleResult);
        }

        if (!overallResult) {
            Assert.fail("Test case verification is failed! Please check the the logs above.");
        }
    }

    private boolean verifyCreateCommandOutput(final String commandOutput) {

        boolean result = true;

        // check the overall size of the binds output
        final int totalNodesToBind = getTotalNodesToBind();
        final List<LinkedHashMap<String, String>> allResult = parseCommandOutput(commandOutput);
        if (totalNodesToBind == allResult.size()) {
            log.info("Check the number of output content ==> SUCCESSFUL.");
        } else {
            log.error("The number of output content is not matched with total number of binds.");
            log.warn("Total Nodes To Bind ==> " + totalNodesToBind);
            log.warn("Number of binds printed out in Output ==> " + allResult.size());
            result = false;
        }

        // check the details of each bind output
        final List<LinkedHashMap<String, String>> badBindOutputList = new ArrayList<LinkedHashMap<String, String>>();
        final StringBuilder sb = new StringBuilder();
        for (final LinkedHashMap<String, String> map : allResult) {
            // find unsuccessful bind if any
            if (map.size() != 5 || !"BIND_SUCCESS".equalsIgnoreCase(map.get("bind create result"))) {
                badBindOutputList.add(map);
                for (final Entry<String, String> entry : map.entrySet()) {
                    sb.append(entry.getKey() + ": " + entry.getValue() + "<br/>\n");
                }
                sb.append("<br/>\n");
            }
        }
        if (badBindOutputList.size() == 0) {
            log.info("Check whether all the bind results are BIND_SUCCESS ==> SUCCESSFUL.");
        } else {
            // print out unsuccessful binds
            log.error("There are " + badBindOutputList.size() + " bind result are incorrect.");
            log.warn("The failed binds are as below:<br/>\n" + sb.toString());
            result = false;
        }

        return result;
    }

    private boolean verifyOutputFile(final String outputXmlFile) {

        boolean result = true;

        if (!outputXmlFile.equals("none")) {
            String respVal = "";
            respVal = bsimCliOperator.executeCommand("ls " + outputXmlFile);
            if (respVal.contains(outputXmlFile)) {
                log.info("Check output file existence ==> SUCCESSFUL.");
                filesToDelete.add(outputXmlFile);
            } else {
                log.error("Output xml file " + outputXmlFile + " does not exist.");
                result = false;
            }

            if (result == true) {
                respVal = bsimCliOperator.executeCommand("cat " + outputXmlFile);
                // TODO: check stuff
            }
        }

        return result;
    }

    private boolean verifyBindsInDatabase() {

        final List<String> failedBinds = new ArrayList<String>();
        for (final LRANBatchConfig batchConfig : batchConfigurationList) {
            for (final String nodeFdn : batchConfig.getMockLRANPicoBatch().getNodeFdnValues()) {
                // if node does not exit, it indicates bind failed
                if (!bsimBatchOperator.moExist(nodeFdn).equals("true")) {
                    failedBinds.add(nodeFdn);
                }
            }
        }
        if (failedBinds.size() == 0) {
            log.info("Check the binding nodes ==> SUCCESSFUL.");
            return true;
        } else {
            log.error("The failed binding nodes are:" + failedBinds.toString());
            return false;
            // Assert.fail("Not all nodes bound successfully!");
        }
    }

    private int getTotalNodesToBind() {

        int totalNodesToBind = 0;

        for (final LRANBatchConfig batchConfig : batchConfigurationList) {

            totalNodesToBind += batchConfig.getBatchSize();
        }

        return totalNodesToBind;
    }

    private List<LinkedHashMap<String, String>> parseCommandOutput(final String commandOutput) {

        Pattern pattern = Pattern.compile("CREATE\\s*=+\\s*([\\s\\S]+)Finished");
        Matcher matcher = pattern.matcher(commandOutput);
        String matchedAll = "";
        if (matcher.find()) {
            matchedAll = matcher.group(1);
        }

        pattern = Pattern.compile("([\\s\\S]+?)\n\n+");
        matcher = pattern.matcher(matchedAll);
        final List<String> matchedItems = new ArrayList<String>();
        while (matcher.find()) {
            final String matched = matcher.group(1);
            matchedItems.add(matched);
        }

        final List<LinkedHashMap<String, String>> resultList = new ArrayList<LinkedHashMap<String, String>>();
        String line = null;
        for (final String item : matchedItems) {
            final Scanner scanner = new Scanner(item);
            final LinkedHashMap<String, String> tmpMap = new LinkedHashMap<String, String>();

            while (scanner.hasNextLine()) {
                if (!"".equals(line = scanner.nextLine())) {
                    final String[] pair = line.split(":");
                    if (pair.length == 2) {
                        tmpMap.put(pair[0].trim(), pair[1].trim());
                    }
                }
            }
            resultList.add(tmpMap);

            scanner.close();
        }

        return resultList;
    }

    private boolean processValidationStrings(final String validationString, final String output) {

        boolean result = false;

        final List<String> valList = new ArrayList<String>();
        final Pattern pattern = Pattern.compile("<(.*?)>");
        String valString = null;

        final Matcher matcher = pattern.matcher(validationString);

        while (matcher.find()) {
            valString = matcher.group(1);
            valList.add(valString);
        }

        final String verifyResult = "";
        final Iterator<String> iterator = valList.iterator();
        while (iterator.hasNext() && !verifyResult.equalsIgnoreCase("Unsuccessful")) {
            if (output.contains(iterator.next().toString())) {
                result = true;
            } else {
                result = false;
            }
        }

        return result;

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

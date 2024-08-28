package com.ericsson.oss.bsim.datasource;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import se.ericsson.jcat.fw.logging.JcatLoggingApi;

import com.ericsson.cifwk.taf.annotations.DataSource;
import com.ericsson.cifwk.taf.assertions.TafAsserts;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.KeyValues.AI;
import com.ericsson.oss.bsim.data.model.KeyValues.CSV_HEADERS;
import com.ericsson.oss.bsim.data.model.KeyValues.GENERAL;
import com.ericsson.oss.bsim.data.model.KeyValues.RADIO;
import com.ericsson.oss.bsim.data.model.KeyValues.TRANS;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.data.model.TemplateType;
import com.ericsson.oss.bsim.getters.BsimDataGetter;

public class LTETestDataSource {

    private static final Logger log = Logger.getLogger(LTETestDataSource.class);

    private final Map<String, Map<String, String>> mapOfTemplateCombinations;

    private final Map<String, Map<String, String>> mapOfSupportedTemplates;

    public LTETestDataSource() {
        mapOfTemplateCombinations = loadTemplateCombinations();
        mapOfSupportedTemplates = loadSupportedTemplates();
    }

    // Result should implement java.lang.Iterable<Map<String,Object>>
    @DataSource
    public List<Map<String, Object>> dataSource() {
        JcatLoggingApi.setTestStepBegin("Generate AddNodeData Object");
        final List<Map<String, Object>> bsimNodeDataList = new ArrayList<Map<String, Object>>();

        final String csvFile = FileFinder.findFile("Taf_LTE_Tests_v2.csv", "testdata_files").get(0);
        try (CsvMapReader csvMapReader = new CsvMapReader(new FileReader(csvFile), CsvPreference.EXCEL_PREFERENCE)) {

            final String[] headers = csvMapReader.getHeader(true);
            Map<String, String> testData;
            Map<String, Object> rowData = null;
            while ((testData = csvMapReader.read(headers)) != null) {

                final BsimNodeData bsimNodeData = new BsimNodeData(NodeType.valueOf(testData.get(CSV_HEADERS.NODE_TYPE)));
                final List<Map<String, String>> listOfRequiredTemplates = getListOfRequiredTemplatesFromCombinationID(testData
                        .get(CSV_HEADERS.TEMPLATE_COMBINATION_ID));
                final ManagedDataSource dataSource = new ManagedDataSource(testData, listOfRequiredTemplates);

                for (final Map<String, String> template : listOfRequiredTemplates) {
                    final Map<String, String> dataMap = dataSource.getDataMapFromTemplate(template);
                    bsimNodeData.setTemplateAttrs((LinkedHashMap<String, String>) dataMap, TemplateType.valueOf(template.get(CSV_HEADERS.TEMPLATE_TYPE)));
                }

                processExtraFieldsRequiredonBsimNodeDataObject(bsimNodeData);

                /*
                 * This data is passed to the test.
                 */
                rowData = new HashMap<String, Object>();
                rowData.put("isEndtoEnd", true);
                rowData.put("numberOfNodes", 100);
                rowData.put("bsimNodeData", bsimNodeData);
                bsimNodeDataList.add(rowData);
            }
        } catch (final IOException e) {
            log.error(e);
        }

        return bsimNodeDataList;
    }

    private void processExtraFieldsRequiredonBsimNodeDataObject(final BsimNodeData bsimNodeData) {
        /*
         * This Data is required to be set on the bsimNodeData object.
         * Because of the new template solution I am able set these fields using information already on the bsim object.
         * This can be looked to be refactored in the future.
         */
        bsimNodeData.setNodeName(bsimNodeData.getAddNodeDataAttrs().get(GENERAL.NODE_NAME));
        bsimNodeData.setPlanName(bsimNodeData.getAddNodeDataAttrs().get(GENERAL.PLAN_NAME));
        bsimNodeData.setNodeTemplate(bsimNodeData.getAddNodeDataAttrs().get(GENERAL.OSS_NODE_TEMPLATE));
        bsimNodeData.setNodeFdn(BsimDataGetter.getNodeFdn(bsimNodeData.getAddNodeDataAttrs().get(GENERAL.SUBNETWORK_GROUP), bsimNodeData.getAddNodeDataAttrs()
                .get(GENERAL.NODE_NAME)));
        bsimNodeData.setImportRadioConfiguration(Boolean.valueOf(bsimNodeData.getAddNodeDataAttrs().get(RADIO.IMPORT_RADIO_CONFIGURATION)));
        bsimNodeData.setImportTransportConfiguration(Boolean.valueOf(bsimNodeData.getAddNodeDataAttrs().get(TRANS.IMPORT_TRANSPORT_CONFIGURATION)));
        if (bsimNodeData.getAifData().getAifDataOptionAttrs() == null) {
            bsimNodeData.getAifData().setAutoIntegrate(false);
        } else if (bsimNodeData.getAifData().getAifDataOptionAttrs().get(AI.AUTO_INTEGRATE) == null) {
            bsimNodeData.getAifData().setAutoIntegrate(false);
        } else {
            bsimNodeData.getAifData().setAutoIntegrate(Boolean.valueOf(bsimNodeData.getAifData().getAifDataOptionAttrs().get(AI.AUTO_INTEGRATE)));
        }
        bsimNodeData.CriticalData.setSite(bsimNodeData.getAddNodeDataAttrs().get(GENERAL.SITE));
        bsimNodeData.CriticalData.setIpAddress(bsimNodeData.getAddNodeDataAttrs().get(GENERAL.IP_ADDRESS));
        bsimNodeData.CriticalData.setOssMimVersion(bsimNodeData.getAddNodeDataAttrs().get(GENERAL.MIM_VERSION));
    }

    /*
     * Reads in the Taf_Supported_Templates.csv into a map of maps.
     * Each map can be retrieved by its Template ID
     */
    private Map<String, Map<String, String>> loadSupportedTemplates() {
        final Map<String, Map<String, String>> supportedTemplatesMap = new HashMap<String, Map<String, String>>();

        final List<String> csvFiles = FileFinder.findFile("Taf_Supported_Templates.csv", "testdata_files");
        TafAsserts
                .assertEquals(
                        "Taf_Supported_Templates.csv does not exist. Please create the csv data file with the data needed. Please see <documentation source to be included later>",
                        false, csvFiles.isEmpty());

        try (CsvMapReader csvMapReader = new CsvMapReader(new FileReader(csvFiles.get(0)), CsvPreference.EXCEL_PREFERENCE)) {
            final String[] headers = csvMapReader.getHeader(true);
            Map<String, String> row;
            while ((row = csvMapReader.read(headers)) != null) {
                supportedTemplatesMap.put(row.get("Template ID"), row);
            }
        } catch (final IOException e) {
            log.error("File Taf_Supported_Templates.csv does not have proper format or content");
            log.error(e);
        }
        return supportedTemplatesMap;
    }

    /*
     * Reads in the Taf_Template_Combinations.csv into a map of maps.
     * Each map can be retrieved by its Template Combination ID
     */
    private Map<String, Map<String, String>> loadTemplateCombinations() {
        final Map<String, Map<String, String>> templatesCombinationsMap = new HashMap<String, Map<String, String>>();

        final List<String> csvFiles = FileFinder.findFile("Taf_Template_Combinations.csv", "testdata_files");
        TafAsserts
                .assertEquals(
                        "Taf_Template_Combinations.csv does not exist. Please create the csv data file with the data needed. Please see <documentation source to be included later>",
                        false, csvFiles.isEmpty());

        try (CsvMapReader csvMapReader = new CsvMapReader(new FileReader(csvFiles.get(0)), CsvPreference.EXCEL_PREFERENCE)) {
            final String[] headers = csvMapReader.getHeader(true);
            Map<String, String> row;
            while ((row = csvMapReader.read(headers)) != null) {
                templatesCombinationsMap.put(row.get("Template Combination ID"), row);
            }
        } catch (final IOException e) {
            log.error("File Taf_Template_Combinations does not have proper format or content");
            log.error(e);
        }
        return templatesCombinationsMap;
    }

    /*
     * This will return a list of Maps from Taf_Supported_Templates.csv given the Template Combination ID.
     */
    private List<Map<String, String>> getListOfRequiredTemplatesFromCombinationID(final String templateCombinationID) {
        final List<Map<String, String>> listOfSupportedTemplatesMaps = new ArrayList<Map<String, String>>();

        final Map<String, String> chosenCombination = mapOfTemplateCombinations.get(templateCombinationID);
        TafAsserts.assertNotNull("Template Combination ID " + templateCombinationID + " is not found in Taf_Template_Combinations.csv", chosenCombination);

        // Template Combination ID is no longer needed. Needs to be removed as it cannot be processed in the next step.
        chosenCombination.remove("Template Combination ID");

        for (final String templateID : chosenCombination.values()) {
            if (templateID != null) {
                listOfSupportedTemplatesMaps.add(getSupportedTemplateMap(templateID));
            }
        }
        return listOfSupportedTemplatesMaps;
    }

    private Map<String, String> getSupportedTemplateMap(final String templateID) {
        final Map<String, String> map = mapOfSupportedTemplates.get(templateID);
        TafAsserts.assertNotNull("Template ID " + templateID + " is not found in Taf_Supported_Templates.csv", map);
        return map;
    }
}
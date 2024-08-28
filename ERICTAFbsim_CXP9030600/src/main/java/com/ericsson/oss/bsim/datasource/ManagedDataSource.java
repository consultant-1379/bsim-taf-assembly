package com.ericsson.oss.bsim.datasource;

import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertEquals;
import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertNotNull;

import java.io.FileReader;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;
import org.testng.SkipException;

import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.cache.BsimDataCache;
import com.ericsson.oss.bsim.data.model.KeyValues;
import com.ericsson.oss.bsim.data.model.KeyValues.CSV_HEADERS;
import com.ericsson.oss.bsim.data.model.TemplateType;

public class ManagedDataSource {

    private final BsimDataCache bsimDataCache;

    private static final Logger log = Logger.getLogger(ManagedDataSource.class);

    public ManagedDataSource(final Map<String, String> testData, final List<Map<String, String>> listOfRequiredTemplates) {
        this.bsimDataCache = new BsimDataCache(testData, listOfRequiredTemplates);
    }

    public Map<String, String> getDataMapFromTemplate(final Map<String, String> template) {
        final TemplateType templateType = TemplateType.valueOf(template.get(CSV_HEADERS.TEMPLATE_TYPE));
        final String templateID = template.get(CSV_HEADERS.TEMPLATE_ID);
        final String csvFile = template.get(CSV_HEADERS.CSVFILE);

        final LinkedHashMap<String, String> dataMap = new LinkedHashMap<String, String>();
        switch (templateType) {
            case BASIC:
                if (templateID.equals("LTE_Basic_MANAGED")) {
                    dataMap.put(KeyValues.GENERAL.OSS_NODE_TEMPLATE, bsimDataCache.getOssNodeTemplate()); // "AddNodeExample"
                    dataMap.put(KeyValues.GENERAL.NODE_NAME, bsimDataCache.getNodeName());// "NodeTAF12345"
                    dataMap.put(KeyValues.GENERAL.PLAN_NAME, bsimDataCache.getPlanName());// "PlanTaf123498");
                    dataMap.put(KeyValues.GENERAL.ENABLE_PCI_ASSIGNMENT, bsimDataCache.getEnablePCIAssignment());// true
                    dataMap.put(KeyValues.GENERAL.SUBNETWORK_GROUP, bsimDataCache.getSubnetworkGroup()); // "ERBS-SUBNW-1");
                    dataMap.put(KeyValues.GENERAL.MIM_VERSION, bsimDataCache.getMimVersion()); // "E.1.180.N.1.13");
                    dataMap.put(KeyValues.GENERAL.SITE, bsimDataCache.getSite()); // "Ath");
                    dataMap.put(KeyValues.GENERAL.IP_ADDRESS, bsimDataCache.getIpAddress()); // "192.168.103.101");
                    dataMap.put(KeyValues.TRANS.IMPORT_TRANSPORT_CONFIGURATION, bsimDataCache.getImportTransportConfiguration()); // "true");
                    dataMap.put(KeyValues.TRANS.USE_TRANSPORT_CM_TEMPLATE, bsimDataCache.getUseTransportTemplate()); // "true");
                    dataMap.put(KeyValues.TRANS.TRANSPORT_TEMPLATE_NAME, bsimDataCache.getTransportTemplateName());// "TNBulkCMExample");
                    dataMap.put(KeyValues.RADIO.IMPORT_RADIO_CONFIGURATION, bsimDataCache.getImportRadioConfiguration()); // true);
                    dataMap.put(KeyValues.RADIO.USE_RADIO_CM_TEMPLATE, bsimDataCache.getUseRadioTemplate()); // "true");
                    dataMap.put(KeyValues.RADIO.RADIO_TEMPLATE_NAME, bsimDataCache.getRadioTemplateName()); // "3Cell_FDD_RNBulkCMExample_L13");
                } else {
                    throw new SkipException("Unsupported Template ID " + templateID + "for Template Type " + templateType + ".");
                }
                break;
            case NODE:
                switch (templateID) {
                    case "LTE_AddNodeExample_MANAGED":
                        dataMap.put(KeyValues.GENERAL.SUBNETWORK_GROUP, bsimDataCache.getSubnetworkGroup()); // "ERBS-SUBNW-1");
                        dataMap.put(KeyValues.GENERAL.IP_ADDRESS, bsimDataCache.getIpAddress()); // "192.168.103.101");
                        dataMap.put(KeyValues.FTP.BACKUP_STORE, bsimDataCache.getFtpBackup()); // "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=l-back-nedssv4");
                        dataMap.put(KeyValues.FTP.LICENSE_KEY, bsimDataCache.getFtpLicense()); // "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=l-key-nedssv4");
                        dataMap.put(KeyValues.FTP.SW_STORE, bsimDataCache.getFtpSofware()); // "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=l-sws-nedssv4");
                        dataMap.put(KeyValues.FTP.AUTO_INTEGRATION, bsimDataCache.getFtpAI()); // "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=aiflran");
                        dataMap.put(KeyValues.GENERAL.MIM_VERSION, bsimDataCache.getMimVersion()); // "E.1.180.N.1.13");
                        dataMap.put(KeyValues.GENERAL.SITE, bsimDataCache.getSite()); // "Ath");
                        dataMap.put(KeyValues.GENERAL.USER_NAME, bsimDataCache.getUserName()); // "nmsadm");
                        dataMap.put(KeyValues.GENERAL.NODE_NAME, bsimDataCache.getNodeName());// "NodeTAF12345"
                        dataMap.put(KeyValues.GENERAL.PLAN_NAME, bsimDataCache.getPlanName());// "PlanTaf123498");
                        break;
                    default:
                        throw new SkipException("Unsupported Template ID " + templateID + "for Template Type " + templateType + ".");
                }
                break;
            case TRANSPORT:
            case RADIO:
                dataMap.put(KeyValues.GENERAL.SUBNETWORK_GROUP, bsimDataCache.getSubnetworkGroup());
                dataMap.put(KeyValues.GENERAL.NODE_NAME, bsimDataCache.getNodeName());
                dataMap.put(KeyValues.GENERAL.PLAN_NAME, bsimDataCache.getPlanName());
                dataMap.put(KeyValues.GENERAL.USER_NAME, bsimDataCache.getUserName());

                dataMap.putAll(getDataFromCSVFile(csvFile));
                break;
            case AUTOINTEGRATE:
                dataMap.put(KeyValues.AI.SITE_BASIC_TEMPLATE, bsimDataCache.getSiteBasicTemplateName());// "SiteBasicExample_L12A");
                dataMap.put(KeyValues.AI.SITE_EQUIPMENT_TEMPLATE, bsimDataCache.getSiteEquipmentTemplateName());// "SiteEquipment_3SectorDualTx_L13A");
                dataMap.put(KeyValues.AI.SITE_INSTALLATION_TEMPLATE, bsimDataCache.getSiteInstallationTemplateName());// "SiteInstallExample_L12A");

                dataMap.putAll(getDataFromCSVFile(csvFile));
                break;
            case SITE_BASIC:
            case SITE_INSTALL:
                dataMap.put(KeyValues.GENERAL.NODE_NAME, bsimDataCache.getNodeName());
                dataMap.put(KeyValues.GENERAL.USER_NAME, bsimDataCache.getUserName());
                dataMap.put(KeyValues.FTP.SERVICE, bsimDataCache.getFtpAI());

                dataMap.putAll(getDataFromCSVFile(csvFile));
                break;
            case SITE_EQUIPMENT:
                dataMap.put(KeyValues.GENERAL.NODE_NAME, bsimDataCache.getNodeName());
                dataMap.put(KeyValues.GENERAL.USER_NAME, bsimDataCache.getUserName());
                dataMap.put(KeyValues.FTP.SERVICE, bsimDataCache.getFtpAI());
                dataMap.put(KeyValues.GENERAL.SITE, bsimDataCache.getSite());

                dataMap.putAll(getDataFromCSVFile(csvFile));
                break;
            default:
                throw new SkipException("Invalid Template Type: " + templateType);
        }
        return dataMap;
    }

    private Map<String, String> getDataFromCSVFile(final String templateID) {
        Map<String, String> row = null;

        final List<String> csvFiles = FileFinder.findFile(templateID + ".csv", "csv_data_files");
        assertEquals(templateID
                + ".csv does not exist. Please create the csv data file with the data needed. Please see <documentation source to be included later>", false,
                csvFiles.isEmpty());

        try (CsvMapReader csvMapReader = new CsvMapReader(new FileReader(csvFiles.get(0)), CsvPreference.EXCEL_PREFERENCE)) {
            final String[] headers = csvMapReader.getHeader(true);
            row = csvMapReader.read(headers);
        } catch (final Exception any) {
            log.error(any);
        }

        assertNotNull(row);
        return row;
    }

}

package com.ericsson.oss.bsim.getters;

import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertNotNull;
import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.oss.bsim.data.model.KeyValues;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.data.model.TemplateType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.handlers.templates.TemplateHandler;
import com.ericsson.oss.bsim.handlers.templates.TemplateHandlerImpl;

/*
 * This Class is responsible for generating needed data and retrieving needed data from the Server
 *
 * Currently this is hard coded towards atvts627.
 * It will need to wait until the HostConfigurator is implemented before this
 * can be used to retrieve data from a server
 */
public class BsimDataGetter {

    private final static Host ossHost = BsimApiGetter.getHostMaster();

    private final static TemplateHandler templateHandler = new TemplateHandlerImpl(ossHost);

    private final static int ONEHUNDREDMILLION = 100000000;

    public static String getNodeName() {
        final long eightDigitId = System.currentTimeMillis() % ONEHUNDREDMILLION;
        return "NodeTAF" + eightDigitId;
    }

    public static String getPlanName() {
        final long eightDigitId = System.currentTimeMillis() % ONEHUNDREDMILLION;
        return "PlanTaf" + eightDigitId;
    }

    public static String getNodeFdn(final String subNetworkGroup, final String nodeName) {
        return String.format("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=%1$s,MeContext=%2$s", subNetworkGroup, nodeName);
    }

    /*
     * TODO
     * This is hard coded for now.
     * Could be updated to take user information from a Host user.
     */
    public static String getUserName() {
        return "nmsadm";
    }

    /*
     * TODO
     * This is hardcoded for now.
     * Can be updated to always be true or always be false as a default depending which is useful.
     * If this needs to be a test input then that can also be arranged.
     */
    public static String getEnablePCIAssignment() {
        return "false";
    }

    /*
     * If the AddNodeExample or AddNodeWithSTNExample node template is picked. Then this will require a site that exists.
     * If the AddNodeAndSiteExample or AddNodeandSitewithSTNexample node template is picked,
     * then this will be required to generate a site to use.
     * The parameter ossNodeTemplateName is available from the DataCache. It does not need to be supplied.
     */
    public static String getSite(final String ossNodeTemplateName) {
        String site = null;
        if (ossNodeTemplateName.contains("Site")) {
            final long id = System.currentTimeMillis() % 100000000;
            site = "SiteTAF" + id;
        } else {
            // TODO: site = SiteUtility.getSite();
            site = "Ath";
        }
        return site;
    }

    /*
     * Mim versions depend on two things. NodeType and NodeVersion
     */
    public static String getMimVersion(final NodeType nodeType, final String nodeVersion) {
        // TODO: MimUtility.getOssMimVersion(nodeType, nodeVersion);
        return "E.1.180.N.1.13";
    }

    public static String getNetsimMimVersion() {
        // TODO: MimUtility.getNetsimMimVersion(nodeType, nodeVersion);
        return "E1160";
    }

    /*
     * SubNetwork depends on NodeType
     */
    public static String getSubNetworkGroup(final NodeType nodeType) {
        // TODO: SubNetworkUtility.getSubNetwork(nodeType);
        return "ERBS-SUBNW-1";
    }

    /*
     * The Ip address will depend on the number of nodes and whether it is an ipv4 node or an ipv6 node.
     */
    public static String getIpAddress(final String numberOfNodes, final String isIPv6) {
        // TO DO: IpUtility.getFreeIpAddress(numberOfNodes, isIPv6);
        final Random r = new Random(System.currentTimeMillis());
        final String ip = "192.168." + r.nextInt(255) + "." + r.nextInt(255);

        return ip;
    }

    /*
     * Ftp services are known for a vapp. They are not known for a system test server and will need to be discovered.
     * They can be determined from the NodeType and whether it is an ipv4 node or an ipv6 node
     */
    public static String[] getFtpServices(final NodeType nodeType, final boolean isIPv6) {

        String ftpBackup = null;
        String ftpLicense = null;
        String ftpSoftware = null;
        String ftpAI = null;

        // will need to retrieve this from testdata. i.e. the row data isIPv6 and NodeType will determine these
        if (nodeType.equals(NodeType.LTE)) {
            if (isIPv6) {
                ftpBackup = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv6,FtpService=l-back-nedssv6";
                ftpLicense = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv6,FtpService=l-key-nedssv6";
                ftpSoftware = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv6,FtpService=l-sws-nedssv6";
                ftpAI = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv6,FtpService=aiflran";
            } else {
                ftpBackup = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=l-back-nedssv4";
                ftpLicense = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=l-key-nedssv4";
                ftpSoftware = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=l-sws-nedssv4";
                ftpAI = "SubNetwork=ONRM_ROOT_MO,FtpServer=SMRSSLAVE-LRAN-nedssv4,FtpService=aiflran";
            }
        }
        assertNotNull(ftpBackup);
        assertNotNull(ftpLicense);
        assertNotNull(ftpSoftware);
        assertNotNull(ftpAI);
        /*
         * This information will need to be validated against the server.
         */
        return new String[] { ftpBackup, ftpLicense, ftpSoftware, ftpAI };

    }

    /*
     * Information from here is obtained by using the list of templates associated with the Template Combination.
     * If a transport template is specifed -> getTranportData
     * If a radio template is specified -> getRadioData
     * If a node template is specified -> getNodeData
     * If a AI is to be run - getAItemplate names
     */
    public static String getOssNodeTemplate(final List<Map<String, String>> listOfSupportedTemplateMaps) {
        String ossNodeTemplateName = null;

        for (final Map<String, String> map : listOfSupportedTemplateMaps) {
            if (map.get("Template Type").equals(TemplateType.NODE.toString())) {
                ossNodeTemplateName = map.get("Template Name");
                break;
            }
        }
        assertNotNull("Could not find " + ossNodeTemplateName + " in csv file", ossNodeTemplateName);
        assertTrue(ossNodeTemplateName + " does not exist on the Oss Master.", templateHandler.templateExists(ossNodeTemplateName));

        return ossNodeTemplateName;
    }

    public static String[] getTransportData(final List<Map<String, String>> listOfSupportedTemplateMaps) {
        String transportTemplateName = null;
        String importTransportConfiguration = "false";
        String useTransportTemplate = "false";

        for (final Map<String, String> map : listOfSupportedTemplateMaps) {
            if (map.get("Template Type").equals(TemplateType.TRANSPORT.toString())) {
                transportTemplateName = map.get("Template Name");
                break;
            }
        }
        if (transportTemplateName != null) {
            importTransportConfiguration = "true";
            useTransportTemplate = "true";
        }

        assertNotNull("Could not find " + transportTemplateName + " in csv file", transportTemplateName);
        assertTrue(transportTemplateName + " does not exist on the Oss Master.",
                !templateHandler.templateExists(transportTemplateName) ? templateHandler.installTemplate(transportTemplateName) : true);

        return new String[] { transportTemplateName, importTransportConfiguration, useTransportTemplate };
    }

    public static String[] getRadioData(final List<Map<String, String>> listOfSupportedTemplateMaps) {
        String radioTemplateName = null;
        String useRadioTemplate = "false";
        String importRadioConfiguration = "false";
        for (final Map<String, String> map : listOfSupportedTemplateMaps) {
            if (map.get("Template Type").equals(TemplateType.RADIO.toString())) {
                radioTemplateName = map.get("Template Name");
                break;
            }
        }
        if (radioTemplateName != null) {
            importRadioConfiguration = "true";
            useRadioTemplate = "true";
        }

        assertNotNull("Could not find " + radioTemplateName + " in csv file", radioTemplateName);
        assertTrue(radioTemplateName + " does not exist on the Oss Master.",
                !templateHandler.templateExists(radioTemplateName) ? templateHandler.installTemplate(radioTemplateName) : true);

        return new String[] { radioTemplateName, importRadioConfiguration, useRadioTemplate };
    }

    public static String[] getAITemplateNames(final List<Map<String, String>> listOfSupportedTemplateMaps) {

        String siteBasicTemplateName = null;
        String siteInstallationTemplateName = null;
        String siteEquipmentTemplateName = null;
        for (final Map<String, String> map : listOfSupportedTemplateMaps) {
            switch (TemplateType.valueOf(map.get(KeyValues.CSV_HEADERS.TEMPLATE_TYPE))) {
                case SITE_BASIC:
                    siteBasicTemplateName = map.get(KeyValues.CSV_HEADERS.TEMPLATE_NAME);
                    break;
                case SITE_INSTALL:
                    siteInstallationTemplateName = map.get(KeyValues.CSV_HEADERS.TEMPLATE_NAME);
                    break;
                case SITE_EQUIPMENT:
                    siteEquipmentTemplateName = map.get(KeyValues.CSV_HEADERS.TEMPLATE_NAME);
                    break;
                default:
                    break;
            }
        }
        assertNotNull("Could not find " + siteBasicTemplateName + " in csv file", siteBasicTemplateName);
        assertNotNull("Could not find " + siteInstallationTemplateName + " in csv file", siteInstallationTemplateName);
        assertNotNull("Could not find " + siteEquipmentTemplateName + " in csv file", siteEquipmentTemplateName);

        assertTrue(siteBasicTemplateName + " does not exist on the Oss Master.",
                !templateHandler.templateExists(siteBasicTemplateName) ? templateHandler.installTemplate(siteBasicTemplateName) : true);
        assertTrue(siteInstallationTemplateName + " does not exist on the Oss Master.",
                !templateHandler.templateExists(siteInstallationTemplateName) ? templateHandler.installTemplate(siteInstallationTemplateName) : true);
        assertTrue(siteEquipmentTemplateName + " does not exist on the Oss Master.",
                !templateHandler.templateExists(siteEquipmentTemplateName) ? templateHandler.installTemplate(siteEquipmentTemplateName) : true);

        return new String[] { siteBasicTemplateName, siteInstallationTemplateName, siteEquipmentTemplateName };
    }

}

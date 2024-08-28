package com.ericsson.oss.bsim.robustness.precheck;

import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.utils.BsimTestCaseFileHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

public class PreCheckNonDeliveredTemplates implements IBsimPreChecker {

    private static Logger log;

    private final boolean allFilesTransferred = false;

    private String transferredTemplatesDirectory = "";

    private String consoleMessage;

    private static final String FAILED = "FAILED";

    private static BsimRemoteCommandExecutor preCheckSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    private static RemoteObjectHandler preCheckRemoteFileHandler = BsimApiGetter.getGatewayFileHandler();

    private static final Host host = HostGroup.getGateway();

    @Override
    public boolean doPreCheck(final NodeType nodeType) {

        return checkNonDeliveredTemplates(nodeType);
    }

    @Override
    @Test(groups = { "wcdma.precheck", "lte.precheck", "microlte.precheck", "common.precheck" }, alwaysRun = true)
    public void doPreCheck() {
        Assert.assertEquals(checkNonDeliveredTemplates(NodeType.WCDMA), true);
        Assert.assertEquals(checkNonDeliveredTemplates(NodeType.MICRO_WCDMA), true);
        Assert.assertEquals(checkNonDeliveredTemplates(NodeType.LTE), true);
        Assert.assertEquals(checkNonDeliveredTemplates(NodeType.MICRO_LTE), true);
    }

    /**
     * Check Non delivered templates and create them if missing
     * 
     * @param templates
     * @param nodeType
     * @return "OK" if all templates found
     */
    public boolean checkNonDeliveredTemplates(final NodeType nodeType) {

        log = Logger.getLogger(PreCheckNonDeliveredTemplates.class);
        log.info("<font color=purple><B>3> Start to check Non Delivered Templates...</B></font>");
        final String templates = updateTemplatesToBeCheckedBasedOnNodeType(nodeType);

        boolean testResult;
        final LinkedHashMap<String, String> templatesFoundMap = new LinkedHashMap<String, String>();
        final LinkedHashMap<String, String> unexpectedResultMap = new LinkedHashMap<String, String>();
        final String[] templateNames = templates.split(",");

        try {
            StringBuilder sb;
            for (final String templateName : templateNames) {

                log.info("Template Name being checked is " + templateName);
                final String myCmdString = "/opt/ericsson/nms_us_templates_cli/bin/ustemplates.sh find -n " + templateName;

                sb = new StringBuilder();
                preCheckSshRemoteCommandExecutor.simpleExec(myCmdString);
                log.info("Searching for template " + templateName);

                final Scanner scanner = new Scanner(preCheckSshRemoteCommandExecutor.simpleExec(myCmdString));
                scanThroughLineAndBreakIfXmlLineFound(sb, scanner);

                final String templateAttributes = sb.toString();

                if (templateAttributes.contains(templateName.trim())) {
                    addTemplateToTemplatesFoundMap(templatesFoundMap, templateName);
                } else if (nodeType.equals(NodeType.LTE)) {
                    createLTETemplates(templatesFoundMap, sb, templateName, scanner, templateAttributes);
                } else if (nodeType.equals(NodeType.MICRO_LTE)) {
                    createMicroLTETemplates(templatesFoundMap, sb, templateName, scanner, templateAttributes);
                } else if (nodeType.equals(NodeType.WCDMA) || nodeType.equals(NodeType.MICRO_WCDMA)) {
                    createWcdmaTemplates(templatesFoundMap, unexpectedResultMap, sb, templateName, scanner, templateAttributes);
                } else {
                    unexpectedResultMap.put(templateName, "this template is NOT created.");
                }
            }
        } catch (final Exception ex) {
            log.warn("Exception Thrown == > " + ex.toString());
        }
        if (unexpectedResultMap.size() == 0) {
            log.info("<font color=green>Pre-check on non-delivered templates is SUCCESSFUL.</font>");
            testResult = true;
        } else {
            consoleMessage = "Pre-check on non-delivered templates is failed . See problems as below:";
            testResult = failTestCase(unexpectedResultMap, consoleMessage);
        }

        consoleMessage = "Pre-check details on non-delivered templates are as below:";
        printOutDetails(templatesFoundMap, consoleMessage);
        return testResult;

    }

    private void createMicroLTETemplates(
            final LinkedHashMap<String, String> templatesFoundMap,
            final StringBuilder sb,
            final String templateName,
            final Scanner scanner,
            final String templateAttributes) {
        final String createTemplateCommand = generateMicroLTECreateTemplateCommand(templateName);

        executeCreateTemplateCommand(templatesFoundMap, sb, templateName, scanner, templateAttributes, createTemplateCommand);

    }

    private String generateMicroLTECreateTemplateCommand(final String templateName) {
        transferTemplateXmlFiles();
        final String siteInstallFileName = "SiteInstallation";
        final String createTemplateCommand = "/opt/ericsson/nms_us_templates_cli/bin/ustemplates.sh create -n " + templateName
                + " -d FileMgr -t SiteInstall -f " + transferredTemplatesDirectory + siteInstallFileName + ".xml -desc test";
        return createTemplateCommand;

    }

    private String updateTemplatesToBeCheckedBasedOnNodeType(final NodeType nodeType) {

        log.info("<font color=purple><B>Start to check all the non-delivered templates created...</B></font>");
        String templates = "TNBulkCMExample_PostLTE,RNBulkCMExample_PostLTE";
        if (nodeType.equals(NodeType.WCDMA) || nodeType.equals(NodeType.MICRO_WCDMA)) {
            templates = "SiteBasic_WCDMA,SiteEquipment_WCDMA,WCDMA_TN_Post_Install_Template,WCDMA_RN_Post_Install_Template";
        } else if (nodeType.equals(NodeType.MICRO_LTE)) {
            templates = "TNBulkCMExample_PostLTE,RNBulkCMExample_PostLTE,SiteInstall_MICRO,SiteInstallExampleIpSec_mRBS,SiteBasicExampleIpSec_mRBS";
        }
        return templates;
    }

    private void scanThroughLineAndBreakIfXmlLineFound(final StringBuilder sb, final Scanner scanner) {

        String line;
        while (scanner.hasNextLine() && (line = scanner.nextLine()) != null) {

            if (!line.trim().startsWith("<?xml")) {
                if (line.trim().length() > 0) {
                    sb.append(line + "\r\n");
                }
            } else {
                break;
            }
        }
    }

    private void addTemplateToTemplatesFoundMap(final LinkedHashMap<String, String> templatesFoundMap, final String templateName) {

        templatesFoundMap.put(templateName, "this template is created.");
        log.info("Template found ==> " + templateName);
        log.info("TemplatesFoundMap size = " + templatesFoundMap.size());
    }

    private void createLTETemplates(
            final LinkedHashMap<String, String> templatesFoundMap,
            final StringBuilder sb,
            final String templateName,
            final Scanner scanner,
            final String templateAttributes) {

        final String createTemplateCommand = generateLTECreateTemplateCommand(templateName);

        executeCreateTemplateCommand(templatesFoundMap, sb, templateName, scanner, templateAttributes, createTemplateCommand);
    }

    private String generateLTECreateTemplateCommand(final String templateName) {

        transferTemplateXmlFiles();
        String FileType = "TN";
        if (templateName.contains("RN")) {
            FileType = "RN";
        }
        final String createTemplateCommand = "/opt/ericsson/nms_us_templates_cli/bin/ustemplates.sh create -n " + FileType
                + "BulkCMExample_PostLTE -d LRAN -t PostInstallBulkCM -f " + transferredTemplatesDirectory + templateName + ".xml -desc test_template_LTE_TAF";
        return createTemplateCommand;
    }

    private void createWcdmaTemplates(
            final LinkedHashMap<String, String> templatesFoundMap,
            final LinkedHashMap<String, String> unexpectedResultMap,
            final StringBuilder sb,
            final String templateName,
            final Scanner scanner,
            final String templateAttributes) {

        final String createTemplateCommand = generateWCDMACreateTemplateCommand(templateName);
        if (createTemplateCommand.equals(FAILED)) {
            unexpectedResultMap.put(templateName, "this template is NOT created.");
            return;
        }

        executeCreateTemplateCommand(templatesFoundMap, sb, templateName, scanner, templateAttributes, createTemplateCommand);
    }

    private String generateWCDMACreateTemplateCommand(final String templateName) {

        log.info("<font color=red>Template " + templateName + " was not found, will now attempte to create template ==> " + templateName + "</font>");
        transferTemplateXmlFiles();

        String xmlFileSubstitution;
        String domain;
        String templateType;
        if (templateName.contains("SiteBasic_WCDMA")) {
            xmlFileSubstitution = "SiteBasic_WCDMA";
            domain = "SMRS";
            templateType = "SiteBasicWRAN";
            return returnCreateTemplateCommand(templateName, xmlFileSubstitution, domain, templateType);
        } else if (templateName.contains("SiteEquipment_WCDMA")) {

            xmlFileSubstitution = "SiteEquipment_WCDMA";
            templateType = "RbsEquipmentWRAN";
            domain = "SMRS";
            log.info("Command attributes updated in preparation for BulkCMExample_PostSTN");
            return returnCreateTemplateCommand(templateName, xmlFileSubstitution, domain, templateType);
        } else if (templateName.contains("WCDMA_TN")) {

            xmlFileSubstitution = "WCDMA_TN_Post_Install_Template";
            domain = "WRAN";
            templateType = "PostInstallBulkCM";
            log.info("Command attributes updated in preparation for WCDMA_TN_Post_Install_Template");
            return returnCreateTemplateCommand(templateName, xmlFileSubstitution, domain, templateType);
        } else if (templateName.contains("WCDMA_RN")) {
            xmlFileSubstitution = "WCDMA_RN_PostInstall_Template";
            templateType = "PostInstallBulkCM";
            domain = "WRAN";
            log.info("Command attributes updated in preparation for WCDMA_RN_Post_Install_Template");
            return returnCreateTemplateCommand(templateName, xmlFileSubstitution, domain, templateType);
        } else {
            log.info("<font color=red>Template generation failed</font>");
            return FAILED;
        }
    }

    private String returnCreateTemplateCommand(final String templateName, final String xmlFileSubstitution, final String domain, final String templateType) {

        final String createTemplateCommand = "/opt/ericsson/nms_us_templates_cli/bin/ustemplates.sh create -n " + templateName + " -d " + domain + " -t "
                + templateType + " -f " + transferredTemplatesDirectory + xmlFileSubstitution + ".xml -desc test_template_WCDMA_TAF";
        return createTemplateCommand;
    }

    private void executeCreateTemplateCommand(
            final LinkedHashMap<String, String> templatesFoundMap,
            final StringBuilder sb,
            final String templateName,
            final Scanner scanner,
            final String templateAttributes,
            final String createTemplateCommand) {

        log.info("The Create command for " + templateName + " is about to be run \n" + "The command is: " + createTemplateCommand);
        final String createTemplates = preCheckSshRemoteCommandExecutor.simpleExec(createTemplateCommand);
        log.info(createTemplates);
        if (createTemplates.contains("Problem with Command Arguments.")) {

        } else {
            scanThroughLineAndBreakIfXmlLineFound(sb, scanner);
        }
        if (templateAttributes.contains(templateName.trim())) {
            addTemplateToTemplatesFoundMap(templatesFoundMap, templateName);
        }
    }

    private void transferTemplateXmlFiles() {

        log.info("Transferring files to the server if they do not exist");
        if (!allFilesTransferred) {
            final String templatesFolder = "taf_non_delivered_templates/";

            log.info("Making directory " + preCheckSshRemoteCommandExecutor.simpleExec("mkdir " + templatesFolder).trim());

            final String homeDirectory = preCheckSshRemoteCommandExecutor.simpleExec("pwd ").trim();

            transferredTemplatesDirectory = homeDirectory + "/" + templatesFolder;
            log.info("Transferred templates directory " + transferredTemplatesDirectory);

            // get files required to be transferred

            final LinkedHashMap<String, String> templateNamePathMap = BsimTestCaseFileHelper.searchFilesInWorkspace(".xml", "templates/non_delivered/");

            final CLICommandHelper cmdHelper = new CLICommandHelper(host, host.getUsers(UserType.ADMIN).get(0));
            cmdHelper.openShell();
            for (final Entry<String, String> namePathPair : templateNamePathMap.entrySet()) {
                if (preCheckRemoteFileHandler.copyLocalFileToRemote(namePathPair.getValue(), transferredTemplatesDirectory)) {
                    cmdHelper.runInteractiveScript("scp " + transferredTemplatesDirectory + "/" + namePathPair.getKey() + " nmsadm@ossmaster:"
                            + transferredTemplatesDirectory);
                    cmdHelper.expect("Password:");
                    cmdHelper.interactWithShell("nms27511");
                }
                log.info("File " + namePathPair.getKey() + " transferred into the folder: " + transferredTemplatesDirectory);
            }

        }

        // final String filesTransferred = preCheckSshRemoteCommandExecutor.simpleExec("ls " + transferredTemplatesDirectory).trim();
        //
        // for (final String fileName : templateNamePathMap.keySet()) {
        //
        // if (filesTransferred.contains(fileName)) {
        // final String response = fileName + " transferred successfully";
        // log.info(response);
        // }
        // }
        //
        // allFilesTransferred = true;
        // } else {
        // log.info("File transfer skipped ==> Files already exist on the server");
        // }
    }

    private boolean failTestCase(final LinkedHashMap<String, String> unexpectedResultMap, final String consoleMessage) {

        boolean testResult;
        final StringBuilder sb = new StringBuilder(consoleMessage + "\r\n");
        for (final Entry<String, String> pair : unexpectedResultMap.entrySet()) {
            sb.append(String.format("%1$s: %2$s\r\n", pair.getKey(), pair.getValue()));
        }
        log.error(sb.toString().replaceAll("\r\n$", ""));

        testResult = false;
        return testResult;
    }

    private void printOutDetails(final LinkedHashMap<String, String> outputMap, final String consoleMessage) {

        final StringBuilder sb = new StringBuilder(consoleMessage + "\r\n");
        for (final Entry<String, String> entry : outputMap.entrySet()) {
            sb.append(String.format("%1$s: %2$s\r\n", entry.getKey(), entry.getValue()));
        }

        log.info(sb.toString());

    }

    @Override
    public String getCheckDescription() {

        return "Check the availability of templates...";
    }
}


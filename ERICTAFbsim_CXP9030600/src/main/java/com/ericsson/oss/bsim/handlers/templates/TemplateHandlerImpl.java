package com.ericsson.oss.bsim.handlers.templates;

import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertFalse;
import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertNotNull;
import static com.ericsson.cifwk.taf.assertions.TafAsserts.assertTrue;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.supercsv.io.CsvMapReader;
import org.supercsv.prefs.CsvPreference;

import com.ericsson.cifwk.taf.assertions.TafAsserts;
import com.ericsson.cifwk.taf.data.Host;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.handlers.templates.domain.TemplateCommand;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * Implementation of a Template Handler using the utitlti
 * 
 * @author ecilosh
 */
public class TemplateHandlerImpl implements TemplateHandler {

    private static Logger log = Logger.getLogger(TemplateHandlerImpl.class);

    private final Map<String, Map<String, String>> mapOfTemplateData;

    private final Host ossHost;

    private final CLICommandHelper cliCommandHelper;

    public TemplateHandlerImpl(final Host host) {
        this.ossHost = host;
        mapOfTemplateData = loadTemplateData();
        if (host.getHostname().trim().equalsIgnoreCase("ossmaster")) {
            /*
             * final OssHost ossHost = new OssHost(host);
             * final User user = host.getUsers(UserType.OPER).get(0);
             */
            log.info("User is : " + host.getUser());
            this.cliCommandHelper = new CLICommandHelper(host, HostGroup.getOssmaster().getNmsadmUser());
        } else {
            log.info("User is : " + host.getUser());
            this.cliCommandHelper = new CLICommandHelper(host, HostGroup.getOssmaster().getRootUser());
        }
    }

    @Override
    public boolean templateExists(final String templateName) {
        log.debug("Searching for " + templateName + " template in Host " + ossHost.getHostname());
        final TemplateCommandBuilder builder = SimpleTemplateCommandBuilder.builder();
        final TemplateCommand templateCommand = builder.findall().build();
        log.trace("Executing  Findall Command: " + templateCommand.getTemplateCommand());
        final String commandResult = cliCommandHelper.simpleExec(templateCommand.getTemplateCommand());
        log.trace("Output from command is \n" + commandResult);
        log.trace("Will return true if " + templateName + " is found in the output");
        return commandResult.contains(templateName);
    }

    @Override
    public boolean installTemplate(final String templateName) {
        log.info("Attempting to install template " + templateName + " on OSS");
        final String localTemplateFilePath = this.getLocalFilePathForTemplate(templateName);
        assertNotNull("Cannot find template file " + templateName + " in local workspace", localTemplateFilePath);

        final RemoteObjectHandler fileHandler = new RemoteObjectHandler(ossHost);
        final String remoteTemplateFilePath = "/tmp/" + templateName + ".xml";
        fileHandler.copyLocalFileToRemote(localTemplateFilePath, remoteTemplateFilePath);
        assertTrue(remoteTemplateFilePath + " does not exist on the OSS host.", fileHandler.remoteFileExists(remoteTemplateFilePath));

        final String domain = mapOfTemplateData.get(templateName).get("templateDomain");
        final String type = mapOfTemplateData.get(templateName).get("templateType");
        final String desc = mapOfTemplateData.get(templateName).get("templateDesc");

        assertNotNull("Domain information is not supplied in the NonDeliveredTemplateData.csv file. Please supply", domain);
        assertNotNull("Type information is not supplied in the NonDeliveredTemplateData.csv file. Please supply", type);
        assertNotNull("Description information is not supplied in the NonDeliveredTemplateData.csv file. Please supply", desc);

        final TemplateCommandBuilder builder = SimpleTemplateCommandBuilder.builder();
        final TemplateCommand templateCommand = builder.create().name(templateName).domain(domain).type(type).file(remoteTemplateFilePath).desc(desc).build();
        final String command = templateCommand.getTemplateCommand();

        log.trace("Executing install command " + command);
        final String output = cliCommandHelper.simpleExec(command);
        log.trace("Output from command is " + output.trim());

        fileHandler.deleteRemoteFile(remoteTemplateFilePath);
        assertFalse(remoteTemplateFilePath + " still exists on the OSS host.", fileHandler.remoteFileExists(remoteTemplateFilePath));

        return templateExists(templateName);
    }

    @Override
    public boolean uninstallTemplate(final String templateName) {
        log.debug("Attempting to uninstall template " + templateName + " on OSS");
        final TemplateCommandBuilder builder = SimpleTemplateCommandBuilder.builder();
        final TemplateCommand templateCommand = builder.delete().name(templateName).build();
        final String command = templateCommand.getTemplateCommand();

        log.trace("Executing command " + command);
        final String output = cliCommandHelper.simpleExec(command);
        log.trace("Output from command is " + output.trim());
        return !templateExists(templateName);
    }

    /*
     * Check to see if the template name exists in the Local workspace so that it can be installed
     */
    private String getLocalFilePathForTemplate(final String templateName) {

        final List<String> results = FileFinder.findFile(templateName + ".xml");
        for (final String s : results) {
            log.trace(s);
        }
        return !results.isEmpty() ? results.get(0) : null;
    }

    /*
     * Return a list of data from the NonDeliveredTemplateData.csv in template/non_delivered directory
     */
    private Map<String, Map<String, String>> loadTemplateData() {
        final Map<String, Map<String, String>> supportedTemplatesMap = new HashMap<String, Map<String, String>>();

        final List<String> csvFiles = FileFinder.findFile("NonDeliveredTemplateData.csv", "templates/non_delivered");
        TafAsserts
                .assertEquals(
                        "NonDeliveredTemplateData.csv does not exist. Please create the csv data file with the data needed. Please see <documentation source to be included later>",
                        false, csvFiles.isEmpty());

        try (CsvMapReader csvMapReader = new CsvMapReader(new FileReader(csvFiles.get(0)), CsvPreference.EXCEL_PREFERENCE)) {
            final String[] headers = csvMapReader.getHeader(true);
            Map<String, String> row;
            while ((row = csvMapReader.read(headers)) != null) {
                supportedTemplatesMap.put(row.get("templateName"), row);
            }
        } catch (final IOException e) {
            log.error("File Taf_Supported_Templates.csv does not have proper format or content");
            log.error(e);
        }
        return supportedTemplatesMap;
    }

}

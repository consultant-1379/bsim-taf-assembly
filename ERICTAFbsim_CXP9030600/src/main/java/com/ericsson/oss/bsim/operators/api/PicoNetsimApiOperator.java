/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.operators.api;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.testng.Assert;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.batch.data.model.MockBsimBatch;
import com.ericsson.oss.bsim.batch.data.model.RanType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.netsim.domain.RbsConfigLevel;
import com.ericsson.oss.bsim.utils.BsimTestCaseFileHelper;

/**
 * @author egavhug
 */
public class PicoNetsimApiOperator extends NetsimApiOperator {

    private static final String TEMPLATES_FOLDER = "security_Certs_For_Netsim";

    /**
     * @param String
     *        foldertoCreate
     * @param String
     *        locationOfFilesToTransfer
     * @return String "Security Config file transfered to OMSAS sucessfully" if successful or
     *         "Security Config file transfered to OMSAS failed" if unsuccessful
     */

    public String transferSecurityConfigurationFilesToOmsas() {

        String returnValue = "Security Config file transfered to OMSAS failed";

        log.info("Transferring files to the OMSAS server");

        log.info("Making directory " + omsasHostCLICommandHelper.simpleExec("mkdir " + TEMPLATES_FOLDER));

        log.info("Creating directory " + TEMPLATES_FOLDER);

        final File file = new File(FileFinder.findFile("configure_security_certs_on_omsas.txt").get(0));

        final String localFile = file.getAbsolutePath();
        final String remoteFile = "/" + TEMPLATES_FOLDER + "/configure_security_certs_on_omsas.txt";
        final boolean isFileTransferredSuccessfully = omsasRemoteFileHandler.copyLocalFileToRemote(localFile, remoteFile);
        log.info("File " + file.getName() + " Will now be transferred into the folder: " + TEMPLATES_FOLDER);

        if (isFileTransferredSuccessfully) {
            final String response = file.getName() + " transferred successfully";
            log.info(response);
            returnValue = "Security Config file transfered to OMSAS sucessfully";
        } else {
            log.error("File Transfer Failed");
            returnValue = "Security Config file transfered to OMSAS failed";
        }
        return returnValue;
    }

    /**
     * Create an RNC in Netsim
     * 
     * @param simulationName
     * @param nodeName
     * @return Output from the Command Line execution in Netsim
     */
    public String createRNCInNetsim(final String simulationName, final String rncPortName, final String rncName) {

        final String[] createRNCCommands = new String[14];

        createRNCCommands[0] = ".open " + simulationName;
        createRNCCommands[1] = ".createne netype " + getLatestRNCMimVersion();
        createRNCCommands[2] = ".createne checkport " + rncPortName;
        createRNCCommands[3] = ".new simne -auto 1 " + rncName;
        createRNCCommands[4] = ".set netype " + getLatestRNCMimVersion();
        createRNCCommands[5] = ".set port " + rncPortName;
        createRNCCommands[6] = ".createne subaddr 0 subaddr no_value";
        createRNCCommands[7] = ".set taggedaddr subaddr 0 1";
        createRNCCommands[8] = ".set ssliop no no_value";
        createRNCCommands[9] = ".createne hooks 1";
        createRNCCommands[10] = ".set ssliop no no_value";
        createRNCCommands[11] = ".set save";
        createRNCCommands[12] = ".start -parallel";

        log.info("Creating RNC " + rncName);

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createRNCCommands);
        final String errorMessage = "RNC: " + rncName + " creation failed!";
        final String successfulMessage = "RNC created in netsim successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    /**
     * Check to See if an RNC is synchronized
     * 
     * @param rncFdn
     * @return
     * @throws InterruptedException
     */
    public boolean checkRNCisSynchronized(final String rncFdn) throws InterruptedException {
        int count = 0;
        final String checkmirrorMIBsynchStatusCommand = cstest + database + LIST_ATTRIBUTE + rncFdn + " mirrorMIBsynchStatus";

        do {
            if (bsimSshRemoteCommandExecutor.simpleExec(checkmirrorMIBsynchStatusCommand).contains(
                    "[1] mirrorMIBsynchStatus (enum SupportedMirrorMibSynchStatus r): 3")) {
                log.info("mirrorMIBsynchStatus of RNC is 3 ==> OK");
                return true;
            }
            count++;
            Thread.sleep(2000);
            log.info(bsimSshRemoteCommandExecutor.simpleExec(checkmirrorMIBsynchStatusCommand));
            log.info("Checking to see if RNC is synchronized");
        } while (count < 90);
        log.error("RNC synchronization failed!");
        return false;
    }

    public boolean stopRncInNetsim(final NetsimApiOperator netsimApiOperator, final String rncName, final String rncFdn) {

        log.info("Checking and stopping default " + rncName + " if it exists in Netsim");

        boolean returnValue = false;
        int count = 0;

        final List<String> allSims = sshNetsimHandler.getListOfSimulations();
        String rncSimulation = null;
        for (final String sim : allSims) {

            if (sim.toLowerCase().contains(rncName.toLowerCase())) {
                rncSimulation = sim;
                log.info("Simulation " + rncSimulation + " has an " + rncName + ". Attempting to stop RNC");
                netsimApiOperator.stopNode(rncSimulation, rncName);

            }
        }

        final String checkMIBCommand = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS la " + rncFdn + " mirrorMIBsynchStatus";

        do {
            if (bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand).contains("[1] mirrorMIBsynchStatus (enum SupportedMirrorMibSynchStatus r): 4")) {
                log.info("mirrorMIBsynchStatus of RNC is 4 ==>  It is now ok to proceed with Test case");
                return returnValue = true;
            }
            count++;
            log.info(checkMIBCommand);

            try {
                Thread.sleep(2000);
            } catch (final InterruptedException e) {
            }

            log.info(bsimSshRemoteCommandExecutor.simpleExec(checkMIBCommand));
            log.info("Checking to see if RNC is unsynchronized, RNC needs to be unsynchronized from the default RNC in Netsim before continuing");
        } while (count < 60);
        log.error("RNC did not unsynchronize!");
        return returnValue;
    }

    public boolean startRNCinNetsim(final NetsimApiOperator netsimApiOperator, final String rncName) {

        log.info("Checking to see if default " + rncName + " exist in Netsim");

        boolean returnValue = false;

        final List<String> allSims = sshNetsimHandler.getListOfSimulations();
        String rncSimulation = null;
        for (final String sim : allSims) {
            if (sim.toLowerCase().contains(rncName.toLowerCase())) {
                rncSimulation = sim;
                log.info("Simulation " + rncSimulation + " has an " + rncName + ". Attempting to restart RNC");
                returnValue = netsimApiOperator.startNode(rncSimulation, rncName);

            }
        }
        return returnValue;
    }

    public String createPicoPort(final MockBsimBatch picoBatch) {

        final String portName = getPicoPortName(picoBatch);
        log.info("Port Name chosen ==> " + portName);
        final String portIPAddress = getPicoPortIPAddress(picoBatch);
        log.info("Port IP Address chosen ==> " + portIPAddress);

        return createPicoPort(portName, portIPAddress);

    }

    protected String getPicoPortName(final MockBsimBatch picoBatch) {

        final String portName = picoBatch.getRantype().toString();
        return portName + "_" + "PICO_PORT";
    }

    protected String getPicoPortIPAddress(final MockBsimBatch picoBatch) {

        final String ipAddress = picoBatch.getIpAddress();
        int lastSeperator = 0;

        lastSeperator = ipAddress.lastIndexOf(".");

        return ipAddress.substring(0, lastSeperator + 1) + "1";
    }

    public String createPicoPort(final String portName, final String ipAddress) {

        final String[] createPort = new String[8];

        createPort[0] = ".configure .config1 newport";
        createPort[1] = ".select configuration";
        createPort[2] = ".config1 newport";
        createPort[3] = ".config add port " + portName + " netconf_prot netsim";
        createPort[4] = ".config1 portaddr " + portName;
        createPort[5] = ".config port address " + portName + " " + ipAddress + " 161 public 1 %unique 2 %simname_%nename authpass privpass 2 2";
        createPort[6] = ".config1 portaddr " + portName + " second_page";
        createPort[7] = ".config save";

        log.info("Creating Pico Port in Netsim ==> " + portName);

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createPort);
        final String errorMessage = "Port creation failed!";
        final String successfulMessage = "Port created in netsim successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    public String createPicoDefaultDestinationPort(final String ranType, final String ipAddress) {

        final String portName = ranType + "_" + "PICO_DESTINATION_PORT";

        final String[] createPort = new String[9];

        createPort[0] = ".configure .config1 newexternal";
        createPort[1] = ".config add external " + portName + " netconf_prot";
        createPort[2] = ".config external servers " + portName + " netsim";
        createPort[3] = ".config1 extaddr " + portName;
        createPort[4] = ".config1 newaddr " + portName;
        createPort[5] = ".config external address " + portName + " " + ipAddress + " 162 1";
        createPort[6] = ".config1 extaddr " + portName;
        createPort[7] = ".config external address " + portName + " " + ipAddress + " 162 1";
        createPort[8] = ".config save";

        log.info("Creating Default Destination Port in Netsim ==> " + portName);

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createPort);
        final String errorMessage = "Port creation failed!";
        final String successfulMessage = "Port created in netsim successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    public String createPicoDefaultDestinationPort(final MockBsimBatch picoBatch) {

        final String portName = getPicoDefaultDestinationPortName(picoBatch);
        final String portIPAddress = getPicoDefaultDestinationPortIPAddress(picoBatch);

        return createPicoPort(portName, portIPAddress);

    }

    private String[] generateCreatPicoNodeCommand(final String[] createNodeArrayofCommands, final String simulationName, final MockBsimBatch picoBatch) {

        String ipOffset = getIPOffset(picoBatch.getIpAddress(), false);
        ipOffset = picoBatch.getIpAddress();
        log.info("IP Offset of IP address is: ==> " + ipOffset);
        createNodeArrayofCommands[0] = ".open " + simulationName;
        createNodeArrayofCommands[1] = ".createne";
        createNodeArrayofCommands[2] = ".createne netype " + getLatestPicoMimVersionbyRanType(picoBatch.getRantype());
        createNodeArrayofCommands[3] = ".createne checkport " + getPicoPortName(picoBatch);
        createNodeArrayofCommands[4] = ".new simne -auto 1 " + picoBatch.getNodeNameForNetsim(); // This is the node name in Netsim
        createNodeArrayofCommands[5] = ".set netype " + getLatestPicoMimVersionbyRanType(picoBatch.getRantype());
        // createNodeArrayofCommands[6] = ".set port " + getPicoPortName(picoBatch);
        createNodeArrayofCommands[6] = ".set port " + "NETCONF_PROT_SSH";
        createNodeArrayofCommands[7] = ".createne addresses";
        createNodeArrayofCommands[8] = ".createne subaddr " + ipOffset + " subaddr no_value";
        createNodeArrayofCommands[9] = ".set taggedaddr subaddr " + ipOffset;
        createNodeArrayofCommands[10] = ".createne dosetext external " + getPicoDefaultDestinationPortName(picoBatch);
        // createNodeArrayofCommands[11] = ".set external " + getPicoDefaultDestinationPortName(picoBatch);
        createNodeArrayofCommands[11] = ".set external " + "NETCONF_PROT_SSH";
        createNodeArrayofCommands[12] = ".createne hooks 1";
        createNodeArrayofCommands[13] = ".set ssliop no no_value";
        createNodeArrayofCommands[14] = ".set save";

        return createNodeArrayofCommands;
    }

    /**
     * Create an PICO Node in Netsim
     * 
     * @param simulationName
     * @param nodeName
     * @return
     */
    public String createPicoNodeInNetsim(final String simulationName, final MockBsimBatch picoBatch) {

        String[] createNodeCommands = new String[15];
        final String nodeName = picoBatch.getNodeNameForNetsim();

        createNodeCommands = generateCreatPicoNodeCommand(createNodeCommands, simulationName, picoBatch);

        log.info("Creating Pico Node " + nodeName + " in simulation " + simulationName);

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createNodeCommands);
        final String errorMessage = "Node " + nodeName + " creation failed!";
        final String successfulMessage = "Node " + nodeName + " created in netsim successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    /**
     * Utility method to transfer files from local to a Netsim instance
     * 
     * @param String
     *        foldertoCreate
     * @param String
     *        locationOfFilesToTransfer
     * @return String "Files transferred successfully" if successful or
     *         "File Transfer failed!" if unsuccessful
     */
    public String transferSecurityConfigurationFilesToNETSIM() {
        final CLICommandHelper shell = new CLICommandHelper(BsimApiGetter.getHostNetsim());
        // final CLI cli = new CLI(BsimApiGetter.getHostNetsim());
        // final Shell shell = cli.openShell(Terminal.VT100);

        String returnValue = "Security Certificate files Transfer to NETSIM Failed";
        final String homeDirectory = "/netsim/security_Certs_For_Netsim";

        log.info("Making directory-> " + TEMPLATES_FOLDER);
        shell.execute("mkdir " + TEMPLATES_FOLDER);
        shell.execute("cd " + TEMPLATES_FOLDER);

        // get files required to be transferred.

        final LinkedHashMap<String, String> templateNamePathMap = BsimTestCaseFileHelper.searchFilesInWorkspace(".pem", "/netsimscripts/security_config_files");

        for (final Entry<String, String> namePathPair : templateNamePathMap.entrySet()) {
            System.out.println(namePathPair.getValue());
            netsimRemoteFileHandler.copyLocalFileToRemote(namePathPair.getValue(), homeDirectory);
            log.info("File " + namePathPair.getKey() + " will now be transferred into the folder: " + homeDirectory);
        }

        final String filesTransferred = netsimHostCLICommandHelper.simpleExec("ls " + homeDirectory);

        for (final String fileName : templateNamePathMap.keySet()) {

            if (filesTransferred.contains(fileName)) {
                final String response = fileName + " transferred successfully";
                log.info(response);
                returnValue = "Transfered Security Certificated To NETSIM successfully";
            }
        }

        return returnValue;
    }

    public String generateSecurityFilesOnOmsas() {
        final CLICommandHelper omsasCommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());
        // final CLI cli = new CLI(BsimApiGetter.getHostOmsas());
        // final Shell shell = cli.openShell(Terminal.VT100);
        log.info("generateing Security Files On Omsas");
        // omsasCommandHelper.simpleExec("cd security_Certs_For_Netsim");
        // shell.writeln("cd security_Certs_For_Netsim");

        // generateKeys_pemFileAndCheckExists(shell);
        // generateCert_csrFileAndCheckExists(shell);
        // generateCert_pemFileAndCheckExists(shell);
        // concatenateTheThreeCaCerts(shell);
        // generateCert_Sinlge_pemFileAndCheckExists(shell);

        generateKeys_pemFileAndCheckExists(omsasCommandHelper);
        generateCert_csrFileAndCheckExists(omsasCommandHelper);
        generateCert_pemFileAndCheckExists(omsasCommandHelper);
        concatenateTheThreeCaCerts(omsasCommandHelper);
        generateCert_Sinlge_pemFileAndCheckExists(omsasCommandHelper);

        final String returnValue = "Generated security files on Omsas successfully";
        return returnValue;
    }

    private void generateCert_Sinlge_pemFileAndCheckExists(final CLICommandHelper omsasCommandHelper) {
        // shell.writeln("cd /security_Certs_For_Netsim");
        // shell.writeln("cp cert.pem cert_single_tmp.pem");
        // shell.writeln("sed '/-----END CERTIFICATE-----/,$d' cert_single_tmp.pem > cert_single.pem");
        // shell.writeln("echo -----END CERTIFICATE----- >> cert_single.pem; rm cert_single_tmp.pem");
        // shell.writeln("ls");
        // omsasCommandHelper.simpleExec("cd /security_Certs_For_Netsim");
        omsasCommandHelper.simpleExec("cp /security_Certs_For_Netsim/cert.pem /security_Certs_For_Netsim/cert_single_tmp.pem");
        omsasCommandHelper
                .simpleExec("sed '/-----END CERTIFICATE-----/,$d' /security_Certs_For_Netsim/cert_single_tmp.pem > /security_Certs_For_Netsim/cert_single.pem");
        omsasCommandHelper
                .simpleExec("echo -----END CERTIFICATE----- >> /security_Certs_For_Netsim/cert_single.pem; rm /security_Certs_For_Netsim/cert_single_tmp.pem");
        // omsasCommandHelper.simpleExec("ls");

        checkFileExists(omsasCommandHelper, "/security_Certs_For_Netsim", "cert_single.pem");

    }

    private void concatenateTheThreeCaCerts(final CLICommandHelper omsasCommandHelper) {
        // shell.writeln("cd /opt/ericsson/csa/certs");
        // shell.writeln("ls");
        // shell.writeln("cp ossmasterMSCertCA.pem /security_Certs_For_Netsim/CombinedCertCA.pem");
        // shell.writeln("echo '' >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        // shell.writeln("cat ossmasterRootCA.pem >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        // shell.writeln("echo '' >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        // shell.writeln("cat ossmasterNECertCA.pem >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        // shell.writeln("cat /security_Certs_For_Netsim/CombinedCertCA.pem");
        // shell.writeln("ls");
        // omsasCommandHelper.simpleExec("cd /opt/ericsson/csa/certs");
        // omsasCommandHelper.simpleExec("ls");
        omsasCommandHelper.simpleExec("cp /opt/ericsson/csa/certs/ossmasterMSCertCA.pem /security_Certs_For_Netsim/CombinedCertCA.pem");
        omsasCommandHelper.simpleExec("echo '' >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        omsasCommandHelper.simpleExec("cat ossmasterRootCA.pem >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        omsasCommandHelper.simpleExec("echo '' >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        omsasCommandHelper.simpleExec("cat ossmasterNECertCA.pem >> /security_Certs_For_Netsim/CombinedCertCA.pem");
        omsasCommandHelper.simpleExec("cat /security_Certs_For_Netsim/CombinedCertCA.pem");
        // omsasCommandHelper.simpleExec("ls");
        checkFileExists(omsasCommandHelper, "CombinedCertCA.pem");
    }

    private void generateCert_pemFileAndCheckExists(final CLICommandHelper omsasCommandHelper) {
        // shell.writeln("/opt/ericsson/secinst/bin/credentialsmgr.sh -signCACertReq /security_Certs_For_Netsim/cert.csr "
        // + "ossmasterNECertCA /security_Certs_For_Netsim/cert.pem");
        // shell.writeln("ls");
        omsasCommandHelper.openShell();
        omsasCommandHelper.runInteractiveScript("/opt/ericsson/secinst/bin/credentialsmgr.sh -signCACertReq /security_Certs_For_Netsim/cert.csr "
                + "ossmasterNECertCA /security_Certs_For_Netsim/cert.pem");
        // omsasCommandHelper.simpleExec("ls");
        try {
            log.info("Waiting for 30 sec to generate /security_Certs_For_Netsim/cert.pem");
            Thread.sleep(30 * 1000);
        } catch (final InterruptedException e) {
            log.error(e);

        }
        checkFileExists(omsasCommandHelper, "/security_Certs_For_Netsim", "cert.pem");
    }

    private void generateCert_csrFileAndCheckExists(final CLICommandHelper omsasCommandHelper) {
        // shell.writeln("/usr/sfw/bin/openssl req -new -key keys.pem -out cert.csr -config configure_security_certs_on_omsas.txt");
        // shell.expect("Enter pass phrase for keys.pem:");
        // shell.writeln("eric123");
        // shell.writeln("ls");
        // checkFileExists(shell, "cert.csr");
        omsasCommandHelper.openShell();
        // omsasCommandHelper.execute("cd /security_Certs_For_Netsim");
        // final String result = omsasCommandHelper.execute("ls /security_Certs_For_Netsim/");
        omsasCommandHelper
                .runInteractiveScript("/usr/sfw/bin/openssl req -new -key /security_Certs_For_Netsim/keys.pem -out /security_Certs_For_Netsim/cert.csr -config /security_Certs_For_Netsim/configure_security_certs_on_omsas.txt");
        omsasCommandHelper.expect("Enter pass phrase for /security_Certs_For_Netsim/keys.pem:");
        omsasCommandHelper.interactWithShell("eric123");
        // omsasCommandHelper.simpleExec("ls");
        checkFileExists(omsasCommandHelper, "/security_Certs_For_Netsim", "cert.csr");

    }

    private void generateKeys_pemFileAndCheckExists(final CLICommandHelper omsasCommandHelper) {
        omsasCommandHelper.openShell();
        String message;
        omsasCommandHelper.runInteractiveScript("/usr/sfw/bin/openssl genrsa -des3 -out /security_Certs_For_Netsim/keys.pem 1024");
        // log.info(message);

        message = omsasCommandHelper.expect("Enter pass phrase for /security_Certs_For_Netsim/keys.pem:");
        log.info(message);
        omsasCommandHelper.interactWithShell("eric123");
        omsasCommandHelper.expect("Verifying - Enter pass phrase for /security_Certs_For_Netsim/keys.pem:");
        omsasCommandHelper.interactWithShell("eric123");
        checkFileExists(omsasCommandHelper, "/security_Certs_For_Netsim", "keys.pem");
        // shell.writeln("/usr/sfw/bin/openssl genrsa -des3 -out keys.pem 1024");
        // shell.expect("Enter pass phrase for keys.pem:");
        // shell.writeln("eric123");
        // shell.expect("Verifying - Enter pass phrase for keys.pem:");
        // shell.writeln("eric123");
        // shell.writeln("ls");
        // checkFileExists(omsasCommandHelper, "keys.pem");
    }

    /**
     * Create SSL Security Definition in Netsim
     * 
     * @return
     */

    public String createSSLSecurityDefinition(final String simulationName) {

        final String[] createSSL = new String[27];

        createSSL[0] = ".opensimul";
        createSSL[1] = ".open " + simulationName;
        createSSL[2] = ".firstactivity";
        createSSL[3] = ".setactivity START";
        createSSL[4] = ".initialguistatus";
        createSSL[5] = ".configure .ssliopdialog list";
        createSSL[6] = ".select configuration";
        createSSL[7] = ".ssliopdialog list";
        createSSL[8] = ".select configuration";
        createSSL[9] = ".ssliopdialog create";
        createSSL[10] = ".setssliop createormodify SSLSecurityDefPicoWRAN";
        createSSL[11] = ".setssliop description  SSL Secuiryt Definition for Pico WRAN";
        createSSL[12] = ".setssliop clientverify 0";
        createSSL[13] = ".setssliop clientdepth 1";
        createSSL[14] = ".setssliop serververify 0";
        createSSL[15] = ".setssliop serverdepth 1";
        createSSL[16] = ".setssliop protocol_version sslv2|sslv3|tlsv1";
        createSSL[17] = ".ssliopdialog modify3 SSLName|/netsim/security_Certs_For_Netsim/cert_single.pem|"
                + "/netsim/security_Certs_For_Netsim/CombinedCertCA.pem|/netsim/security_Certs_For_Netsim/keys.pem|eric123";
        createSSL[18] = ".setssliop clientcertfile /netsim/security_Certs_For_Netsim/cert_single.pem";
        createSSL[19] = ".setssliop clientcacertfile /netsim/security_Certs_For_Netsim/CombinedCertCA.pem";
        createSSL[20] = ".setssliop clientkeyfile /netsim/security_Certs_For_Netsim/keys.pem";
        createSSL[21] = ".setssliop clientpassword eric123";
        createSSL[22] = ".setssliop servercertfile /netsim/security_Certs_For_Netsim/cert_single.pem";
        createSSL[23] = ".setssliop servercacertfile /netsim/security_Certs_For_Netsim/CombinedCertCA.pem";
        createSSL[24] = ".setssliop serverkeyfile /netsim/security_Certs_For_Netsim/keys.pem";
        createSSL[25] = ".setssliop serverpassword eric123";
        createSSL[26] = ".setssliop save force";

        log.info("Creating SSL Security Definition in Netsim");

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(createSSL);
        final String errorMessage = "Creating SSL Security Definition failed!";
        final String successfulMessage = "SSL Security Definition Created Successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    public String applySSLSecurityDefinition(final String simulationName, final MockBsimBatch picoBatch) {
        final String[] applySSL = new String[10];
        log.info("Applying SSL Security Definition in Netsim");

        applySSL[0] = ".opensimul";
        applySSL[1] = ".open " + simulationName;
        applySSL[2] = ".selectnocallback WRAN_RNC_0";
        applySSL[3] = ".selectnocallback " + picoBatch.getNodeNameForNetsim().replace(" ", "");
        applySSL[4] = ".modifyne";
        applySSL[5] = ".modifyne checkselected .set port " + getPicoPortName(picoBatch) + " port";
        applySSL[6] = ".set port " + getPicoPortName(picoBatch);

        applySSL[7] = ".modifyne change 6";
        applySSL[8] = ".set ssliop no SSLSecurityDefPicoWRAN";
        applySSL[9] = ".set save";

        final String outputFromCommandLine = "SSL Security Definition applied to NE Successfully";
        // sshNetsimHandler.executeCommand(applySSL);
        final String errorMessage = "Applying SSL Security Definition failed";
        final String successfulMessage = "SSL Security Definition applied to NE Successfully";
        // replacing err and success msg----- revert back later... done now for testing purposes BY Paras
        return outputToReturnToTestCase(outputFromCommandLine, successfulMessage, errorMessage);
    }

    private String getPicoDefaultDestinationPortName(final MockBsimBatch picoBatch) {

        final String portName = picoBatch.getRantype().toString();
        return portName + "_" + "PICO_DESTINATION_PORT";
    }

    private String getPicoDefaultDestinationPortIPAddress(final MockBsimBatch picoBatch) {

        final String ipAddress = picoBatch.getIpAddress();
        int lastSeperator = 0;

        lastSeperator = ipAddress.lastIndexOf(".");

        return ipAddress.substring(0, lastSeperator + 1) + "1";
    }

    /**
     * Get the latest Netsim Mim Version for Pico by Ran Type
     * 
     * @param ranType
     * @return
     */
    public String getLatestPicoMimVersionbyRanType(final RanType ranType) {
        String domain = "";

        domain = ranType.getRanType();
        // Returns WCDMA PRBS 13B-WCDMA-EU59-V1"
        final String mimsFromNetsimServer = netsimSshRemoteCommandExecutor.simpleExec("ls /netsim/inst/nedatabases/" + domain + ".PRBS*.simcmd |grep -i 14B");
        final String[] unfilteredMims = mimsFromNetsimServer.split(".simcmd*");
        final List<String> filteredMims = new ArrayList<String>();

        filterNetsimResultsFromDirectoryList(unfilteredMims, filteredMims);

        if (filteredMims.size() == 0) {
            log.error("Mim Version not found on Netsim Server");
            return null;
        }

        log.info("The last element is ==> " + filteredMims.get(filteredMims.size() - 1));

        return filteredMims.get(filteredMims.size() - 1);

    }

    /**
     * relates the RNC Node amd the NE in Netsim Simulation
     * 
     * @return RNC and Node related successfully
     */

    public String relateCreatedNodes(final String simulationName, final MockBsimBatch picoBatch) {

        final String[] relateCreatedNodes = new String[3];

        relateCreatedNodes[0] = ".open " + simulationName;
        relateCreatedNodes[1] = ".selectnocallback " + picoBatch.getNodeNameForNetsim().replace(" ", "") + " "
                + picoBatch.getRncNameForNetsim().replace(" ", "");
        relateCreatedNodes[2] = ".relate";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(relateCreatedNodes);
        final String errorMessage = "Relating Node and RNC failed!";
        final String successfulMessage = "RNC and Node related successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);
    }

    /**
     * Get the latest RNC Mim version used in Netsim
     * 
     * @return The latest RNC Mim version in Netsm - String
     */
    private String getLatestRNCMimVersion() {

        // return "WCDMA RNC T11299-V2";
        final String mimsFromNetsimServer = netsimSshRemoteCommandExecutor.simpleExec("ls /netsim/inst/nedatabases/WCDMA.RNC.*.simcmd");
        final String[] unfilteredMims = mimsFromNetsimServer.split(".simcmd*");
        final List<String> filteredMims = new ArrayList<String>();

        filterNetsimResultsFromDirectoryList(unfilteredMims, filteredMims);

        log.info("The last element is ==> [" + filteredMims.get(filteredMims.size() - 1) + "]");

        return filteredMims.get(filteredMims.size() - 1);

    }

    public String startRelatedNode(final String simulationName, final MockBsimBatch picoBatch) {

        final String[] startRelatedNode = new String[3];
        startRelatedNode[0] = ".open " + simulationName;
        startRelatedNode[1] = ".select " + picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ", "");
        startRelatedNode[2] = ".start";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(startRelatedNode);
        final String errorMessage = "Starting Related Node in Netsim Failed";
        final String successfulMessage = "Starting Related Node in Netsim successful";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    /**
     * Sets EmUrl attribute on the WRAN Pico Node in the ONRM
     * 
     * @param nodeName
     * @param nodeFdn
     * @param IP
     *        Address
     * @return False if output does not match expected output
     */
    public boolean setEmUrlAttributeOnWranPicoNodeInOnrm(final MockBsimBatch picoBatch) {
        final String nodeFdn = picoBatch.getNodeFdnValues().get(0).replace("ONRM_ROOT_MO_R", "ONRM_ROOT_MO").replace("MeContext", "ManagedElement");

        log.info("Setting EmUrl: on Pico WRAN node in ONRM");

        final String applyEmUrl = cstest + ONRM_CS + SET_ATTRIBUTE + nodeFdn + " emUrl http://" + picoBatch.getIpAddress() + ":80/em/index_stubbed.html";

        final String checkEmUrlIsApplied = cstest + ONRM_CS + LIST_ATTRIBUTE + nodeFdn + " emUrl";

        final String expectedOutput = "[1] emUrl (string)                : \"http://" + picoBatch.getIpAddress() + ":80/em/index_stubbed.html\"";

        return checkAttributeIsAppliedToWranPicoNode(applyEmUrl, checkEmUrlIsApplied, expectedOutput);
    }

    /**
     * Sets IpAddress attribute on the WRAN Pico Node in the ONRM
     * 
     * @param nodeFdn
     * @param IP
     *        Address
     * @return False if output does not match expected output
     */
    public boolean setIpAddressOnWranPicoNodeInOnrm(final MockBsimBatch picoBatch) {
        final String nodeFdn = picoBatch.getNodeFdnValues().get(0).replace("ONRM_ROOT_MO_R", "ONRM_ROOT_MO").replace("MeContext", "ManagedElement");

        log.info("Setting IP Address: " + picoBatch.getIpAddress() + " on Pico WRAN node in ONRM");

        final String applyIPAddress = cstest + ONRM_CS + SET_ATTRIBUTE + nodeFdn + ",IoInterface=io-0 ipAddress \"" + picoBatch.getIpAddress() + "\"";

        final String checkIpAddressIsApplied = cstest + ONRM_CS + LIST_ATTRIBUTE + nodeFdn + ",IoInterface=io-0 ipAddress";

        final String expectedOutput = "  [1] ipAddress (string)            : \"" + picoBatch.getIpAddress() + "\"";

        return checkAttributeIsAppliedToWranPicoNode(applyIPAddress, checkIpAddressIsApplied, expectedOutput);
    }

    /**
     * Sets protocolTransport attribute on the WRAN Pico Node in the ONRM
     * 
     * @param nodeFdn
     * @return False if output does not match expected output
     */
    public boolean setprotocolTransportOnWranPicoNodeInOnrm(final MockBsimBatch picoBatch) {
        final String nodeFdn = picoBatch.getNodeFdnValues().get(0).replace("ONRM_ROOT_MO_R", "ONRM_ROOT_MO").replace("MeContext", "ManagedElement");

        log.info("Attribute protocolTransport being set in ONRM");

        final String applyprotocolTransport = cstest + ONRM_CS + SET_ATTRIBUTE + nodeFdn
                + ",IoInterface=io-0,ProtocolInfo=protocol-0 protocolTransport \"TLS\"";

        final String checkprotocolTransportApplied = cstest + ONRM_CS + LIST_ATTRIBUTE + nodeFdn
                + ",IoInterface=io-0,ProtocolInfo=protocol-0 protocolTransport";

        final String expectedOutput = "  [1] protocolTransport (string)    : \"TLS\"";

        return checkAttributeIsAppliedToWranPicoNode(applyprotocolTransport, checkprotocolTransportApplied, expectedOutput);
    }

    /**
     * Sets the ManagedElementID on a WRAN Pico Node
     * 
     * @param simulation
     * @param nodeName
     * @return False if ManagedElementID wasn't successfully updated
     */
    public String setManagedElementIdOnWranPicoNode(final String simulationName, final MockBsimBatch picoBatch) {

        // final String relatedNodeName = picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ",
        // "");
        final String relatedNodeName = "WRAN_RNC_01PRBS01";

        final String[] updateMo = new String[4];
        updateMo[0] = ".open " + simulationName;
        updateMo[1] = ".select " + relatedNodeName;
        updateMo[2] = ".start";
        updateMo[3] = "setmoattribute:mo=\"ManagedElement=" + relatedNodeName + "\", attributes=\"managedElementId (moRef)="
                + picoBatch.getNodeNameForNetsim().replace(" ", "") + "\";";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "ManagedElementId updated on WRAN Pico Node failed";
        final String successfulMessage = "ManagedElementId updated on WRAN Pico Node successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    /**
     * Sets the default and destination port,IP address on Pico Node in Netsim
     * 
     * @param simulation
     * @param nodeName
     * @return False if variables aren't successfully updated
     */
    public String setNePropertiesInNetsimNodeForNewStateMachine(final String simulationName, final MockBsimBatch picoBatch) {

        final String neName;
        if (picoBatch.getNodeVersion().compareTo("16B") == 0) {
            neName = "RNC11PRBS01";
        } else if (picoBatch.getNodeVersion().compareTo("14B") == 0) {
            neName = "RNC05PRBS01";
        } else {
            neName = "";
            log.error("AI not supported for " + picoBatch.getNodeVersion() + "");
            return "AI not supported for " + picoBatch.getNodeVersion();
        }
        int i = 0;
        final String[] updateMo = new String[13];
        updateMo[i++] = ".open " + simulationName;
        updateMo[i++] = ".selectnocallback " + neName;
        updateMo[i++] = ".stop";
        updateMo[i++] = ".modifyne checkselected .set port NETCONF_PROT_SSH port";
        updateMo[i++] = ".set port NETCONF_PROT_SSH";
        updateMo[i++] = ".set save";
        // updateMo[i++] = ".modifyne change 3";
        updateMo[i++] = ".modifyne checkselected .set external NETCONF_PROT_SSH";
        updateMo[i++] = ".set external NETCONF_PROT_SSH";
        updateMo[i++] = ".set save";
        updateMo[i++] = ".modifyne set_subaddr " + picoBatch.getIpAddress() + " subaddr no_value";
        updateMo[i++] = ".set save";
        updateMo[i++] = ".start -parallel";
        updateMo[i++] = "setmoattribute:mo=\"ManagedElement=" + neName + "\", attributes=\"managedElementId (moRef)="
                + picoBatch.getNodeNameForNetsim().replace(" ", "") + "\";";
        // updateMo[i++] = ".set save";

        // need to add number of OK's to verify correct execution of commands
        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "Updation Failed for NE Properties on Netsim Node";
        final String successfulMessage = "Updation Successful for NE Properties on Netsim Node";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    public String setDefaultNePropertiesInNetsim(final String simulationName, final MockBsimBatch picoBatch) {

        final String[] updateMo = new String[4];
        int i = 0;

        updateMo[i++] = ".open " + simulationName;
        updateMo[i++] = ".selectnocallback " + "RNC11PRBS01";   // replace RNC11PRBS01 with some node name variable later
        updateMo[i++] = ".modifyne checkselected .set port NETCONF_PROT_SSH port";
        updateMo[i++] = ".set port NETCONF_PROT_SSH";
        updateMo[i++] = ".set save";
        updateMo[i++] = ".modifyne checkselected .set external NETCONF_PROT_SSH";
        updateMo[i++] = ".set external NETCONF_PROT_SSH";
        updateMo[i++] = ".set save";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "aaa";
        final String successfulMessage = "Ok";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);
    }

    /**
     * Sets the ManagedElementType on a WRAN Pico Node
     * 
     * @param simulation
     * @param nodeName
     * @return False if ManagedElementType wasn't successfully updated
     */
    public String setManagedElementTypeOnWranPicoNode(final String simulationName, final MockBsimBatch picoBatch) {
        final String relatedNodeName = picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ", "");
        final String nodeName = picoBatch.getNodeNameForNetsim().replace(" ", "");

        final String[] updateMo = new String[4];
        updateMo[0] = ".open " + simulationName;
        updateMo[1] = ".select " + relatedNodeName;
        updateMo[2] = ".start";
        updateMo[3] = "setmoattribute:mo=\"ManagedElement=" + nodeName + "\", attributes=\"managedElementType (moRef)=PRBS\";";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "ManagedElementType updated on WRAN Pico Node failed";
        final String successfulMessage = "ManagedElementType updated on WRAN Pico successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);
    }

    /**
     * Sets the EncapsulationMo on a WRAN Pico Node
     * 
     * @param simulation
     * @param nodeName
     * @return False if ManagedElementID wasn't successfully updated
     */
    public String setEncapsulationMoOnWranPicoNode(final String simulationName, final MockBsimBatch picoBatch) {

        final String relatedNodeName = picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ", "");
        final String nodeName = picoBatch.getNodeNameForNetsim().replace(" ", "");

        final String[] updateMo = new String[4];
        updateMo[0] = ".open " + simulationName;
        updateMo[1] = ".select " + relatedNodeName;
        updateMo[2] = ".start";
        updateMo[3] = "setmoattribute:mo=\"ManagedElement=" + nodeName
                + ",Transport=1,Host=1,InterfaceIPv4=1\", attributes=\"encapsulation (moRef)=ManagedElement=" + nodeName + "\";";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "EncapsulationMo updated on WRAN Pico Node failed";
        final String successfulMessage = "EncapsulationMo updated on WRAN Pico Node successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    /**
     * Sets the AddressIPv4 Mo on a WRAN Pico Node in Netsim
     * 
     * @param simulation
     * @param nodeName
     * @return "AddressIPv4 updated on WRAN Pico Node successfully"
     */

    public String setAddressIPv4InNetsim(final String simulationName, final MockBsimBatch picoBatch) {
        final String relatedNodeName = picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ", "");
        final String nodeName = picoBatch.getNodeNameForNetsim().replace(" ", "");

        final String[] updateMo = new String[4];
        updateMo[0] = ".open " + simulationName;
        updateMo[1] = ".select " + relatedNodeName;
        updateMo[2] = ".start";
        updateMo[3] = "setmoattribute:mo=\"ManagedElement=" + nodeName
                + ",Transport=1,Host=1,InterfaceIPv4=1,AddressIPv4=1\", attributes=\"address (moRef)=77.77.77.77\";";

        // address of 77.77.77.77. can be anything, field just can't be empty

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "AddressIPv4 updated on WRAN Pico Node failed";
        final String successfulMessage = "AddressIPv4 updated on WRAN Pico Node successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);
    }

    /**
     * Attaches WRAN Pico Node to the MIB Adapter in the SEG
     * 
     * @param nodeFdn
     * @param IP
     *        Address
     * @return False if output does not match expected output
     */
    public boolean attachPicoWRANNodeToMibAdapter(final MockBsimBatch picoBatch) {

        final String nodeFdn = picoBatch.getNodeFdnValues().get(0);
        final String nodeName = picoBatch.getNodeNameForNetsim().replace(" ", "");

        log.info("Pico WRAN Node being detached to MIB adapter");
        final String detachPicoWRANNodetoMIB = cstest + database + DETACH + nodeFdn + ",ManagedElement=" + nodeName;
        log.info("attachPicoWRANNodetoMIB-->" + detachPicoWRANNodetoMIB);
        ossmasterCLICommandHelper.simpleExec(detachPicoWRANNodetoMIB);

        log.info("Pico WRAN Node being attached to MIB adapter");
        final String attachPicoWRANNodetoMIB = cstest + database + ATTACH + nodeFdn + ",ManagedElement=" + nodeName + " nma1";
        log.info("attachPicoWRANNodetoMIB-->" + attachPicoWRANNodetoMIB);
        final String checkAttachPicoWRANNodetoMIB = cstest + database + " mi " + nodeFdn;
        log.info("checkAttachPicoWRANNodetoMIB-->" + checkAttachPicoWRANNodetoMIB);

        final String expectedOutput = "MibAdapter Name : nma1";

        return checkAttributeIsAppliedToWranPicoNode(attachPicoWRANNodetoMIB, checkAttachPicoWRANNodetoMIB, expectedOutput);

    }

    /**
     * set MO in ONRM
     * 
     * @param picoBatch
     * @return
     */
    public boolean updateMoInONRMforPicoWRANNode(final MockBsimBatch picoBatch) {
        boolean result = false;
        final String nodeFdn = picoBatch.getNodeFdnValues().get(0).replace("ONRM_ROOT_MO_R", "ONRM_ROOT_MO").replace("MeContext", "ManagedElement");

        // Update EmUrl for WRAN
        log.info("Mo being updated in ONRM ==> " + nodeFdn + "=" + picoBatch.getNodeNameForNetsim().replace(" ", "") + " emUrl");
        final String updateEmUrl = cstest + ONRM_CS + SET_ATTRIBUTE + nodeFdn + " emUrl http://" + picoBatch.getIpAddress() + "/em/index_stubbed.html";
        final String checkIfEmUpdated = cstest + ONRM_CS + LIST_ATTRIBUTE + nodeFdn + " emUrl";
        String expectedOutput = "http://" + picoBatch.getIpAddress() + "/em/index_stubbed.html";
        result = checkAttributeIsAppliedToWranPicoNode(updateEmUrl, checkIfEmUpdated, expectedOutput);
        if (!result) {
            return false;
        }
        log.info("updatedEmUrl-->" + "emUrl http://" + picoBatch.getIpAddress() + "/em/index_stubbed.html");

        // Update IP address
        log.info("Mo being updated in ONRM ==> " + nodeFdn + "=" + picoBatch.getNodeNameForNetsim() + ",IoInterface=io-0 ipAddress");
        final String updateipAddress = cstest + ONRM_CS + SET_ATTRIBUTE + nodeFdn + ",IoInterface=io-0 ipAddress " + picoBatch.getIpAddress();
        final String checkIfIpUpdated = cstest + ONRM_CS + LIST_ATTRIBUTE + nodeFdn + ",IoInterface=io-0 ipAddress";
        expectedOutput = picoBatch.getIpAddress();

        result = checkAttributeIsAppliedToWranPicoNode(updateipAddress, checkIfIpUpdated, expectedOutput);
        if (!result) {
            return false;
        }
        log.info("updatedIpAddress-->" + picoBatch.getIpAddress());

        // Update port and protocol of MO transport
        log.info("Mo being updated in ONRM ==> " + nodeFdn + "," + picoBatch.getNodeNameForNetsim()
                + ",IoInterface=io-0,ProtocolInfo=protocol-0 port and protocolTransport");
        final String updatePortSSH = cstest + ONRM_CS + SET_ATTRIBUTE + nodeFdn + ",IoInterface=io-0,ProtocolInfo=protocol-0 port 22 protocolTransport SSH";
        final String checkIfUpdated = cstest + ONRM_CS + LIST_ATTRIBUTE + nodeFdn + ",IoInterface=io-0,ProtocolInfo=protocol-0 protocolTransport";
        expectedOutput = "SSH";

        result = checkAttributeIsAppliedToWranPicoNode(updatePortSSH, checkIfUpdated, expectedOutput);
        if (!result) {
            return false;
        }
        log.info("Updated port and protocol of MO transport");

        // Update port and protocol of MO port
        log.info("Mo being updated in ONRM ==> " + nodeFdn + ",IoInterface=io-0,ProtocolInfo=protocol-1 port 22");
        final String updatePort = cstest + ONRM_CS + SET_ATTRIBUTE + nodeFdn + ",IoInterface=io-0,ProtocolInfo=protocol-1 port 22";
        final String checkUpdated = cstest + ONRM_CS + LIST_ATTRIBUTE + nodeFdn + ",IoInterface=io-0,ProtocolInfo=protocol-1 port";
        expectedOutput = "22";

        result = checkAttributeIsAppliedToWranPicoNode(updatePort, checkUpdated, expectedOutput);
        if (!result) {
            return false;
        }
        log.info("Updated port in " + nodeFdn + ",IoInterface=io-0,ProtocolInfo=protocol-1 port 22");
        return result;
    }

    /**
     * Removes the MO Nexthop from Simulation for PICO WRAN node to get synchronized.
     */
    public void removeNexthopMoFromSimulationForPicoWRAN(final String simulationName, final MockBsimBatch picoBatch) {

        final String relatedNodeName = picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ", "");
        final String nodeName = picoBatch.getNodeNameForNetsim().replace(" ", "");

        final String nodeToDelete = "ManagedElement=" + nodeName + ",Transport=1,Host=1,RouteTableIPv4Static=1,Dst=1,NextHop=1";

        final String[] removeMo = new String[3];
        removeMo[0] = ".open " + simulationName;
        removeMo[1] = ".select " + relatedNodeName;
        removeMo[2] = "deletemo:moid=\"" + nodeToDelete + "\";";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(removeMo);

        // final NetSimResult sessionResult = session.exec(NetSimCommands.open(simulationName), NetSimCommands.selectnocallback(nodeName),
        // NetSimCommands.deletemo("ManagedElement=" + nodeName + ",Transport=1,Host=1,RouteTableIPv4Static=1,Dst=1,NextHop=1"));
        //
        // session.close();

        log.info("Response after deleting MO : " + nodeToDelete + " is :" + outputFromCommandLine);
    }

    /**
     * Checks is the attributes are applied to the WRAN Pico Node
     * 
     * @param IP
     *        Address
     * @return False if output does not match expected output
     */
    private boolean checkAttributeIsAppliedToWranPicoNode(final String applyMO, final String checkMoApplied, final String expectedOutput) {
        boolean returnValue;
        ossmasterCLICommandHelper.simpleExec(applyMO);

        if (ossmasterCLICommandHelper.simpleExec(checkMoApplied).contains(expectedOutput)) {
            log.info("Attribute has been set correctly");
            returnValue = true;
        } else {
            log.error("Attribute has NOT been set correctly");
            returnValue = false;
        }
        return returnValue;
    }

    /**
     * Sets the trafficIpAddress on a WRAN Pico Node
     * 
     * @param simulation
     * @param nodeName
     * @return False if trafficIpAddress wasn't successfully updated
     */
    public String setTrafficIpAddressOnWranPicoNode(final String simulationName, final MockBsimBatch picoBatch) {

        final String relatedNodeName = picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ", "");
        final String nodeName = picoBatch.getNodeNameForNetsim().replace(" ", "");

        final String[] updateMo = new String[4];
        updateMo[0] = ".open " + simulationName;
        updateMo[1] = ".select " + relatedNodeName;
        updateMo[2] = ".start";
        updateMo[3] = "setmoattribute:mo=\"ManagedElement=" + nodeName + ",NodeBFunction=1,Iub=1\", attributes=\"trafficIpAddress (moRef)=ManagedElement="
                + nodeName + ",Transport=1,Host=1,InterfaceIPv4=1,AddressIPv4=1\";";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "TrafficIpAddress updated on WRAN Pico Node failed";
        final String successfulMessage = "TrafficIpAddress updated on WRAN Pico Node successfully";

        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    /**
     * Begins the integration of a Node in Netsim with a node already created in
     * BSIM
     * 
     * @param (Fdn) String
     * @return Output from the command line as a String
     * @throws InterruptedException
     */
    public String checkPicoNodeIsSyncedInCS(final MockBsimBatch picoBatch) throws InterruptedException {

        final String fullNodeName = picoBatch.getNodeFdnValues().get(0);
        // The time allowed here for the Node to sync is 10 minutes
        final int maximumCount = 120;
        return checkIfNodeIsSyncedInCs(fullNodeName, maximumCount);

    }

    /**
     * Set the rbsConfigLevel on Pico Node in Netsim
     * 
     * @param simulationName
     * @param picoBatch
     * @param rbsConfigLevel
     * @return
     */
    public boolean setRBSConfigLevelPicoNodeInNetsim(final String simulationName, final MockBsimBatch picoBatch, final RbsConfigLevel rbsConfigLevel) {
        final String relatedNodeName;
        boolean returnValue = false;
        if (picoBatch.getNodeVersion().compareTo("16B") >= 0) {
            relatedNodeName = "RNC11PRBS01";
        } else {
            relatedNodeName = "RNC05PRBS01";
            // relatedNodeName = picoBatch.getRncNameForNetsim().replace(" ", "") + picoBatch.getNodeNameForNetsim().replace(" ", "");
        }

        final String commandLineOutput = setRbsConfigLevelOnPicoNodeInNetsim(picoBatch, simulationName, relatedNodeName, rbsConfigLevel);

        if (commandLineOutput.equals("rbsConfigLevel updated on WRAN Pico Node successfully")) {
            log.info("RbsConfigLevel " + rbsConfigLevel + " updated on " + picoBatch.getRantype() + " Pico Node in Netsim successfully");
            returnValue = true;
        } else {
            log.error("RbsConfigLevel " + rbsConfigLevel + " failed to update on " + picoBatch.getRantype() + " Pico Node in Netsim");
            returnValue = false;
        }

        return returnValue;
    }

    /**
     * Check RbsConfigLevel has been set on Pico Node in the Seg Database
     * 
     * @param picoBatch
     * @param rbsConfigLevel
     * @return
     * @throws InterruptedException
     */
    public boolean checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(final MockBsimBatch picoBatch, final RbsConfigLevel rbsConfigLevel) throws InterruptedException {
        final String rbsLevel = rbsConfigLevel.getRbsConfigLevel().toString();

        final String nodeFunctionMoFDN = picoBatch.getNodeFdnValues().get(0) + ",ManagedElement=" + picoBatch.getNodeName() + ",NodeFunction=1";
        int count = 0;
        final String checkrbsConfigLevelCommand = cstest + database + LIST_ATTRIBUTE + nodeFunctionMoFDN + " rbsConfigLevel";
        log.info("Checking rbsConfigLevel of Node, executing ==> " + checkrbsConfigLevelCommand + ", level expected is " + rbsConfigLevel);
        final String expectedOutput = "[1] rbsConfigLevel (enum RbsConfigLevel): " + rbsLevel;

        do {
            if (ossmasterCLICommandHelper.simpleExec(checkrbsConfigLevelCommand).contains(expectedOutput)) {
                log.info("RbsConfigLevel is " + rbsConfigLevel + " ==> OK");
                log.info("Output is ==> " + ossmasterCLICommandHelper.simpleExec(checkrbsConfigLevelCommand));
                return true;
            }
            count++;
            Thread.sleep(1000);
            log.info(ossmasterCLICommandHelper.simpleExec(checkrbsConfigLevelCommand));
            log.info("Checking rbsConfigLevel of Node, executing ==> " + checkrbsConfigLevelCommand + ", level expected is " + rbsConfigLevel);
        } while (count < 10);
        log.error("RbsConfigLevel has not been set correctly, expected RbsConfigLevel is " + rbsConfigLevel);
        return false;

    }

    private String setRbsConfigLevelOnPicoNodeInNetsim(
            final MockBsimBatch picoBatch,
            final String simulationName,
            final String relatedNodeName,
            final RbsConfigLevel rbsConfigLevel) {

        final String rbsLevel = rbsConfigLevel.getRbsConfigLevel().toString();
        final String managedElementInNetsim = picoBatch.getNodeNameForNetsim().replace(" ", "");

        final String[] updateMo = new String[4];
        updateMo[0] = ".open " + simulationName;
        updateMo[1] = ".select " + relatedNodeName;
        updateMo[2] = ".start";
        updateMo[3] = "setmoattribute:mo=\"ManagedElement=" + managedElementInNetsim + ",NodeFunction=1\", attributes=\"rbsConfigLevel (moRef)=" + rbsLevel
                + "\";";

        final String outputFromCommandLine = sshNetsimHandler.executeCommand(updateMo);
        final String errorMessage = "rbsConfigLevel updated on " + picoBatch.getRantype() + " Pico Node failed";
        final String successfulMessage = "rbsConfigLevel updated on " + picoBatch.getRantype() + " Pico Node successfully";

        try {
            Thread.sleep(3000);
        } catch (final InterruptedException e) {

            e.printStackTrace();
        }
        return outputToReturnToTestCase(outputFromCommandLine, errorMessage, successfulMessage);

    }

    /**
     * Utility method to security certificate files to a Local instance
     * 
     * @param String
     *        locationOfFilesToTransfer
     * @return String "Security files transfered to local successfully" if successful or
     *         "Security Certificate files Transfer Failed" if unsuccessful
     */
    public String transferSecurityConfigurationFilesToLocal() {

        String returnValue = "Security Certificate files Transfer Failed";

        log.info("Transferring security files to the Local Machine");
        // using configure_security_certs_on_omsas.txt to get the absoluste path of the folder we want the file to go to
        final File filepath = new File(FileFinder.findFile("configure_security_certs_on_omsas.txt").get(0));
        final String localFile = filepath.getAbsolutePath();

        final String[] files = { "keys.pem", "cert_single.pem", "CombinedCertCA.pem" };
        for (final String file : files) {
            try {
                Thread.sleep(3000);
                final String myLocalFile = localFile.replace("configure_security_certs_on_omsas.txt", file);
                final String remoteFile = "/security_Certs_For_Netsim/" + file;
                log.info("Attempting to transfer file " + file + " [REMOTE] from " + remoteFile + " to " + myLocalFile + " [LOCAL]");
                final boolean isFileTransferredSuccessfully = omsasRemoteFileHandler.copyRemoteFileToLocal(remoteFile, myLocalFile);

                if (isFileTransferredSuccessfully) {
                    final String response = file + " transferred successfully";
                    log.info(response);
                    returnValue = "Security files transfered to local successfully";
                } else {
                    log.error("File Transfer Failed");
                    returnValue = "Security Certificate files Transfer Failed";
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    /**
     * Delete a security certs folder in OMSAS and Netsim
     * 
     * @param TEMPLATES_FOLDER
     *        - String
     * @return Output from the command line - String
     */

    public String removeSecurityFolderOnOmsasAndNetsim() {
        // final CLI cli_omsas = new CLI(BsimApiGetter.getHostOmsas());
        final CLICommandHelper omsas = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostOmsas());
        // final Shell shell_omsas = cli_omsas.openShell(Terminal.VT100);

        log.info("Deleting " + TEMPLATES_FOLDER + " on OMSAS");
        omsas.simpleExec("rm -rf " + TEMPLATES_FOLDER);
        // omsas.read();
        checkFileExists(omsas, TEMPLATES_FOLDER);
        omsas.disconnect();

        // final CLI cli_netsim = new CLI(BsimApiGetter.getHostNetsim());
        // final Shell shell_netsim = cli_netsim.openShell(Terminal.VT100);
        final CLICommandHelper cli_netsim = new CLICommandHelper(BsimApiGetter.getHostNetsim());
        log.info("Deleting " + TEMPLATES_FOLDER + " on NETSIM");
        cli_netsim.simpleExec("rm -rf " + TEMPLATES_FOLDER);
        // cli_netsim.read();
        checkFileExists(cli_netsim, TEMPLATES_FOLDER);
        cli_netsim.disconnect();

        final String returnValue = TEMPLATES_FOLDER + " deleted successfully on OMSAS and Netsim";

        return returnValue;
    }

    /**
     * Delete a security certs on local machine
     * 
     * @return Output from the command line - String
     */
    public String removeSecurityFilesOnLocal() {

        String returnValue = "Security Certificate files deleted on local Failed";

        // get files required to be transferred
        final LinkedHashMap<String, String> templateNamePathMap = BsimTestCaseFileHelper.searchFilesInWorkspace(".pem",
                "src\\main\\resources\\netsimscripts\\security_config_files");

        for (final Entry<String, String> namePathPair : templateNamePathMap.entrySet()) {
            try {
                final File file = new File(namePathPair.getValue());

                NetsimApiOperator.log.info(">>>DELETING: " + file.getName());
                if (file.delete()) {
                    returnValue = "File " + namePathPair.getKey() + " has now been deleted from the local machine.";

                } else {
                    returnValue = "File " + namePathPair.getKey() + " has NOT been deleted from the local machine.";

                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        return returnValue;
    }

    protected void checkFileExists(final CLICommandHelper omsasCommandHelper, final String destination, final String fileName) {
        final String result = omsasCommandHelper.simpleExec("ls " + destination);

        if (result.trim().contains(fileName)) {
            log.info(fileName + " exists");
            return;
        } else {
            log.error(fileName + " does not exist");
            Assert.assertTrue(false, fileName + " not created at " + destination);

        }
    }

    protected void checkFileExists(final CLICommandHelper omsasCommandHelper, final String fileName) {
        final String result = omsasCommandHelper.simpleExec("ls " + fileName);
        if (!result.trim().contains("No such file or directory")) {
            log.info(fileName + " exists");
        } else {
            log.error(fileName + " does not exist");
            // Assert.assertTrue(false, fileName + " not created");
        }
    }

    protected void checkFileExists(final Shell shell, final String fileName) {
        if (shell.read().trim().contains(fileName)) {
            log.info(fileName + " exists");
        } else {
            log.error(fileName + " does not exist");
        }
    }

}

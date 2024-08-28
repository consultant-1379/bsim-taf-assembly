/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.operators.api;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.ApiOperator;
import com.ericsson.cifwk.taf.data.User;
import com.ericsson.cifwk.taf.data.UserType;
import com.ericsson.cifwk.taf.tools.cli.CLI;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.cifwk.taf.tools.cli.Shell;
import com.ericsson.cifwk.taf.tools.cli.Terminal;
import com.ericsson.oss.bsim.batch.data.model.MockBsimPicoBatch;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.taf.hostconfigurator.OssHost;

/**
 * @author egavhug
 */
public class BsimAddPicoBatchApiOperator implements ApiOperator {

    private final Logger log = Logger.getLogger(BsimAddPicoBatchApiOperator.class);

    final BsimRemoteCommandExecutor masterHostExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    final CLICommandHelper infraServerCli = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostInfraServer());

    /**
     * @param qRCodePICOLocation
     * @param qrCodeName
     * @return
     */
    public boolean checkQRCodeExists(final String qRCodePICOLocation, final String qrCodeName) {
        log.info("Checking to see if QR Code has been Generated");
        final OssHost ossHost = new OssHost(BsimApiGetter.getHostMaster());
        final User user = ossHost.getUsers(UserType.OPER).get(0);
        final CLI cli_Master = new CLI(ossHost, user);
        final Shell shell_Master = cli_Master.openShell(Terminal.VT100);
        shell_Master.writeln("cd " + qRCodePICOLocation);
        shell_Master.writeln("ls");

        if (shell_Master.read().trim().contains(qrCodeName)) {
            log.info("QR Code: " + qrCodeName + " created successfully");
            return true;
        }
        log.error("QR Code: " + qrCodeName + " was not created successfully");
        return false;
    }

    public Boolean checkDoesSmrsAccountExistforBatch(final MockBsimPicoBatch mockBsimPicoBatch, final boolean expectedResult) {

        log.info("Checking does SMRS account exist for Batch");
        Boolean smrsAccounExists = false;

        final String checkNodeReferences = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS la AutoIntegrationApp=AIP_ROOT,AutoProvisionProperties="
                + mockBsimPicoBatch.getName() + ",AutoProvisionNodes=1 nodeReferences";

        final String outPutFromcheckingNodeReferences = masterHostExecutor.simpleExec(checkNodeReferences);

        if (!outPutFromcheckingNodeReferences.contains("NotExisting")) {

            log.info("Checking nodeReferences returned ==> " + outPutFromcheckingNodeReferences);

            final String[] fdnArray = outPutFromcheckingNodeReferences.split("\\sSubNetwork");

            String checkDoesSmrsExist = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS la SubNetwork" + fdnArray[1];

            checkDoesSmrsExist = checkDoesSmrsExist + " smrsInfo";

            log.info("Check Does SMRS Exist Executing ==> " + checkDoesSmrsExist);

            final String outPutFromcheckingDoesSmrsExist = masterHostExecutor.simpleExec(checkDoesSmrsExist.replace("\r\n", ""));

            if (outPutFromcheckingDoesSmrsExist.contains("[1] smrsInfo")) {

                log.info("OutPut from checking Does Smrs Exist returned ==> " + outPutFromcheckingDoesSmrsExist);

                final String[] outPutFromcheckingDoesSmrsExistAsArray = outPutFromcheckingDoesSmrsExist.split("\"");

                final String smrsAccountName = outPutFromcheckingDoesSmrsExistAsArray[1];

                log.info("Smrs Account Name returned ==> " + smrsAccountName);
                final String command = "cat /ericsson/smrs/etc/smrs_config | grep " + smrsAccountName;
                final String returnValue = infraServerCli.simpleExec(command);
                log.info("Checking SMRS Account ==> " + smrsAccountName);

                if (returnValue.contains(smrsAccountName) && expectedResult == true) {
                    log.info("Success ==> SMRS Account Found");
                    smrsAccounExists = true;
                } else {
                    log.error("Failure ==> SMRS Account does not exist ");
                    smrsAccounExists = false;
                }
            }
        } else {
            if (expectedResult == true) {
                log.error("SMRS Account was expected to be created ==> SMRS Account not found");
            } else {
                log.info("SMRS Account does not exist ==> as expected");
            }
            smrsAccounExists = false;
        }
        return smrsAccounExists;

    }
}

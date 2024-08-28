/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.cases;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.osgi.client.ContainerNotReadyException;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.operators.api.ClientHelper;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

public class PrepareBsimTest extends TorTestCaseHelper implements TestCase {
    private ClientHelper client;

    private static final Logger log = Logger.getLogger(PrepareBsimTest.class);

    private final boolean activationStartTimedOut = true;

    private int checkHeartBeat;

    private int maxActivationTime = 0;

    private static BsimRemoteCommandExecutor preCheckSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    private static BsimRemoteCommandExecutor rootRemoteExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMasterRoot());

    private static RemoteObjectHandler remoteExecutor = BsimApiGetter.getRemoteFileHandler(BsimApiGetter.getHostMaster());

    /**
     * @throws InterruptedException
     * @DESCRIPTION This test case verifies the launching of the CEx GUI
     * @Nodes are connected and synchronised. MC is online and MBeans objects exist.
     * @PRIORITY HIGH
     */
    @TestId(id = "OSS-77441_Func_1", title = "Prepare BSIM and get Osgi Container ready")
    @Context(context = { Context.API })
    @Test(groups = { "KGB", "CDB", "GAT", "Feature" })
    public void prepareTheRun() throws ContainerNotReadyException, IOException, InterruptedException {

        setTestStep("Pre-check before running Cex test suite");
        updateOwnerConfigFile();

        do {
            // updateCexConfigFile();
            clientProcessKiller();   // Killing all the cex client process
            checkHeartBeat = checkClientReturn();

            if (maxActivationTime == 4) {
                restartMc();
            }
            maxActivationTime++;
            if (maxActivationTime == 5) {
                break;
            }

        } while (checkHeartBeat == 1);

        if (checkHeartBeat == 0) {
            log.info("Application is launched successfully");
            assertTrue(activationStartTimedOut);
        } else {
            assertFalse(activationStartTimedOut);
        }
    }

    public int checkClientReturn() {

        try {
	    log.info("Credentials: " + HostGroup.getOssmaster().getRootUser().toString());
            client = new ClientHelper(HostGroup.getOssmaster());
            client.prepareCex();
            client.setClient(client.getOsgiClient());

            return 0;

        } catch (final Exception e) {
            log.info("Application startup has been interrupted,Re-trying Again....", e);

            try {
                client.stopApplication();

            } catch (final Exception ex) {
                log.info("Application close has been interrupted.", e);
            }
            return 1;
        }

    }

    /**
     * Overriding cex_client_application.ini to same path in server
     * 
     * @param cex_client_application
     */
    public void updateCexConfigFile() {

        String scriptFolder = DataHandler.getAttribute("cex_config_file").toString();
        scriptFolder = scriptFolder + File.separator;
        final List<String> groovyFiles = FileFinder.findFile(".ini", scriptFolder);
        log.info("Overriding cex_client_application.ini in same path - /opt/ericsson/nms_cex_client/bin");
        remoteExecutor.copyLocalFileToRemote(groovyFiles.get(0), "/opt/ericsson/nms_cex_client/bin");
    }

    /**
     * Changing the permission root cex_client_application.ini to nmsadm
     * 
     * @param cex_client_application
     */
    public void updateOwnerConfigFile() {

        try {
            rootRemoteExecutor.simpleExec("/usr/bin/chown nmsadm:nms /opt/ericsson/nms_cex_client/bin/cex_client_application.ini");
        } catch (final Exception e) {
            log.info(e.getMessage());
        }

    }

    /**
     * Killing the Cex Client Process
     */
    public void clientProcessKiller() {

        try {
            final String clientProcess = preCheckSshRemoteCommandExecutor
                    .simpleExec("ps -eaf | grep -i /opt/ericsson/nms_cex_client/bin/cex_client_application");

            log.info(clientProcess);

            final List<String> listProcess = new ArrayList<String>(Arrays.asList(clientProcess.split("\n")));

            for (int i = 0; i < listProcess.size(); i++) {
                preCheckSshRemoteCommandExecutor.simpleExec("kill -9 " + listProcess.get(i).substring(8, 15));
            }
        } catch (final Exception e) {
            log.info(e.getMessage());
        }

    }

    /**
     * Restarting Cex Mc
     * 
     * @param oss_cex
     */
    public void restartMc() {

        preCheckSshRemoteCommandExecutor.simpleExec("/opt/ericsson/nms_cif_sm/bin/smtool cold oss_cex -reason=other -reasontext=TAF_RUN");
        try {
            log.info("<font color=purple><B>1> Restarting Cex & Waiting for 3m...</B></font>");
            Thread.sleep(180000);
        } catch (final InterruptedException e) {
            log.debug(e.getMessage());
        }
    }

    @AfterSuite
    public void stopClient() throws ContainerNotReadyException, IOException {

        try {
            client.stopApplication();
            log.info("Application is Closed successfully");
        } catch (final Exception e) {
            log.info("Application close has been interrupted.", e);
            clientProcessKiller();
        }

    }

}

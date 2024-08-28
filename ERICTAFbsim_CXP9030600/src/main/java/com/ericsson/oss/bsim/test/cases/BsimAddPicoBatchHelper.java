package com.ericsson.oss.bsim.test.cases;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;
import org.testng.Assert;

import se.ericsson.jcat.fw.ng.JcatNGTestBase;

import com.ericsson.cifwk.taf.data.DataHandler;
import com.ericsson.cifwk.taf.tools.cli.handlers.impl.RemoteObjectHandler;
import com.ericsson.cifwk.taf.utils.FileFinder;
import com.ericsson.oss.bsim.batch.data.model.MockBsimBatch;
import com.ericsson.oss.bsim.batch.data.model.MockBsimPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.MockWLRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.MockWRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.RanType;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.netsim.domain.PicoWcdmaRbsConfigLevels;
import com.ericsson.oss.bsim.operators.BsimBatchOperator;
import com.ericsson.oss.bsim.operators.BsimOperator;
import com.ericsson.oss.bsim.operators.api.BsimAddPicoBatchApiOperator;
import com.ericsson.oss.bsim.operators.api.NetsimApiOperator;
import com.ericsson.oss.bsim.operators.api.PicoNetsimApiOperator;
import com.ericsson.oss.bsim.utils.RemoteFilesHandler;

public class BsimAddPicoBatchHelper extends JcatNGTestBase {

    private static Logger log = Logger.getLogger(BsimAddPicoBatchHelper.class);

    private String simulationName = "DummyName";

    private final BsimAddPicoBatchApiOperator bsimAddPicoBatchApiOperator = new BsimAddPicoBatchApiOperator();

    private final static String RNC_PORT_NAME = "TAF_WRAN_RNC_Port";

    private BsimOperator netsimOperator;

    private boolean netsimHasBeenUsed = false;

    private final PicoNetsimApiOperator picoNetsimApiOperator = new PicoNetsimApiOperator();

    private final RemoteFilesHandler remoteFilesHandler = new RemoteFilesHandler();

    private static final String QR_CODE_LOCATION = "/opt/ericsson/nms_umts_bsim_server/dat/batch/batch_qr_codes/";

    public void doAddBatch(final BsimBatchOperator bsimBatchOperator, final MockBsimPicoBatch mockBsimPicoBatch) {

        final RanType ranType = mockBsimPicoBatch.getRantype();
        setTestStep("Add " + ranType.toString() + " Batch to CS via calling BSIM Service...");
        String actualResult = "unsuccessful";
        switch (ranType) {
            case LRAN:
                actualResult = bsimBatchOperator.addLRANBatch((MockLRANPicoBatch) mockBsimPicoBatch);
                break;
            case WRAN:
                actualResult = bsimBatchOperator.addWRANBatch((MockWRANPicoBatch) mockBsimPicoBatch);
                break;
            case WLRAN:
                actualResult = bsimBatchOperator.addWLRANBatch((MockWLRANPicoBatch) mockBsimPicoBatch);
                break;
            default:
                log.error("Invalid Ran Type");
        }
        log.info("Actual Result for Add (ranTypeF)" + ranType + " Batch: " + actualResult);

        Assert.assertFalse(actualResult.toLowerCase().contains("unsuccessful"), "Result was unsuccessful.");
    }

    public List<String> bindBatch(
            final BsimBatchOperator bsimBatchOperator,
            final MockBsimPicoBatch mockBsimPicoBatch,
            final int nodesToBind,
            final String expectedResult) {

        final RanType ranType = mockBsimPicoBatch.getRantype();
        setTestStep("Binding " + ranType.toString() + " batch nodes via calling BSIM Service...");
        List<String> actualResult = Collections.emptyList();
        switch (ranType) {
            case LRAN:
                final ArrayList<String> serialNumbers = new ArrayList<String>();
                for (int i = 1; i <= nodesToBind; i++) {
                    final String serialNumber = BsimOperator.generateSerialNumber();
                    serialNumbers.add(serialNumber);
                }
                actualResult = bsimBatchOperator.bindLRANBatch((MockLRANPicoBatch) mockBsimPicoBatch, nodesToBind, serialNumbers);
                break;
            case WRAN:
                actualResult = bsimBatchOperator.bindWRANBatch((MockWRANPicoBatch) mockBsimPicoBatch, nodesToBind);
                break;
            case WLRAN:
                actualResult = bsimBatchOperator.bindWLRANBatch((MockWLRANPicoBatch) mockBsimPicoBatch, nodesToBind);
                break;
            default:
                log.error("Failed to bind batch, due to invalid Ran Type");
        }
        // String success = "Unsuccessful";
        // if (actualResult.size() == nodesToBind) {
        // success = "Successful";
        // }
        // Assert.assertEquals(success, expectedResult);
        // setTestStep("Verify SMRS Account has been created after Batch Bind");
        // Assert.assertTrue(bsimAddPicoBatchApiOperator.checkDoesSmrsAccountExistforBatch(mockBsimPicoBatch, true));
        return actualResult;
    }

    /**
     * @param bsimBatchOperator
     * @param mockLRANPicoBatch
     * @param nodesToBind
     * @param expectedResult
     * @return
     *         Used for NoHardware Bind Test Case.
     *         Executes a NoHardware Bind & verifies that it has ran correctly.
     */
    public List<String> executeNoHardwareBindLRANBatch(
            final BsimBatchOperator bsimBatchOperator,
            final MockLRANPicoBatch mockLRANPicoBatch,
            final int nodesToBind,
            final String expectedResult) {

        setTestStep("Performing No Hardware Bind via calling BSIM Service...");

        final List<String> actualResult = bsimBatchOperator.executeNoHardwareBind(mockLRANPicoBatch, nodesToBind);
        /*
         * String success = "Unsuccessful";
         * if (actualResult.size() == nodesToBind) {
         * success = "Successful";
         * }
         * Assert.assertEquals(success, expectedResult);
         * setTestStep("Verify SMRS Account has been created after Batch Bind");
         * Assert.assertTrue(bsimBatchOperator.checkDoesSmrsAccountExistforBatch(mockLRANPicoBatch, true));
         * setTestStep("Verify CCF created");
         * Assert.assertTrue(bsimBatchOperator.checkCCFFileExists());
         */

        return actualResult;
    }

    /**
     * Check that QR Code has been successfully generated in the specified Directory
     * 
     * @param bsimBatchOperator
     * @param qrCodeName
     */
    public void checkQRCodeIsGenerated(final BsimBatchOperator bsimBatchOperator, final String qrCodeName) {

        Assert.assertTrue(bsimBatchOperator.checkQRCodeExists(QR_CODE_LOCATION, qrCodeName));
    }

    public void deleteBindWRANBatchNodes(final BsimBatchOperator bsimBatchOperator, final List<String> nodesToDelete, final String expectedResult) {

        if (nodesToDelete.size() >= 1) {
            setTestStep("Deleting WRAN batch nodes via calling BSIM Service...");
            final String actualResult = bsimBatchOperator.deletebindWRANBatchNodes(nodesToDelete);
            Assert.assertEquals(actualResult, expectedResult);
        } else {
            log.info("No nodes were bound during test case ==> No nodes have been deleted");
        }
    }

    // WLRAN
    public void deleteBindWLRANBatchNodes(final BsimBatchOperator bsimBatchOperator, final List<String> nodesToDelete, final String expectedResult) {

        if (nodesToDelete.size() >= 1) {
            setTestStep("Deleting WLRAN batch nodes via calling BSIM Service...");
            final String actualResult = bsimBatchOperator.deletebindWLRANBatchNodes(nodesToDelete);
            Assert.assertEquals(actualResult, expectedResult);
        } else {
            log.info("No nodes were bound during test case ==> No nodes have been deleted");
        }
    }

    public void deleteWranBatchesAfterTestExecution(final BsimBatchOperator bsimBatchOperator, final List<MockWRANPicoBatch> addedBatches) {

        for (final MockWRANPicoBatch addedBatch : addedBatches) {
            try {
                log.info("Deleting Batch ==> " + addedBatch.getName());
                bsimBatchOperator.deleteWRANBatch(addedBatch);
                setTestStep("Verify SMRS Accounts have been deleted for Batch" + addedBatch.getName());
                Assert.assertFalse(bsimBatchOperator.checkDoesSmrsAccountExistforBatch(addedBatch, false));
            } catch (final Exception e) {
                log.error("Failed to delete Batch ==> " + addedBatch.getName() + "with exception " + e.getMessage());
            } catch (final AssertionError e) {
                log.warn("Assertion Error Trace: " + e);
                // ensures @AfterClass cleanup will complete
            }
        }
    }

    // wlran
    public void deleteWlranBatchesAfterTestExecution(final BsimBatchOperator bsimBatchOperator, final List<MockWLRANPicoBatch> addedBatches) {

        for (final MockWLRANPicoBatch addedBatch : addedBatches) {
            try {
                log.info("Deleting Batch ==> " + addedBatch.getName());
                bsimBatchOperator.deleteWLRANBatch(addedBatch);
                setTestStep("Verify SMRS Accounts have been deleted for Batch" + addedBatch.getName());
                Assert.assertFalse(bsimBatchOperator.checkDoesSmrsAccountExistforBatch(addedBatch, false));
            } catch (final Exception e) {
                log.error("Failed to delete Batch ==> " + addedBatch.getName() + "with exception " + e.getMessage());
            } catch (final AssertionError e) {
                // ensures @AfterClass cleanup will complete
            }
        }
    }

    public void doNetsimSynchronization(final MockBsimBatch picoBatch) throws InterruptedException {
        log.info("End to End Test Case, Netsim will now be run");
        simulationName = "TAF_PICO_" + picoBatch.getRantype().toString() + "_SIMULATION";
        log.info("Simulation Name chosen ==> " + simulationName);
        try {
            prepareNetsim(picoBatch, false);
            /*
             * if (picoBatch.getRantype().equals(RanType.WRAN)) {
             * checkIfRncNeedsToBeCreated(picoBatch);
             * }
             */
            runNetsimIntegration(picoBatch);
            log.info("AI complete, proceeding for Cleanup of netsim");
        } catch (final Exception e) {
            log.info(e.getMessage());
        } finally {
            cleanUpNetsim();

        }
    }

    public void doNetsimSynchronizationForNewStateMachine(final MockBsimBatch picoBatch, final String domain) throws InterruptedException {
        log.info("End to End Test Case for " + domain + ", Netsim will now be run for New State Machine");
        switch (domain) {
            case "WRAN":
                // Please update with correct simulation name if changed in server.
                // Although i have insured simulation is imported form taf directory to server,
                // it takes some time and its always advisable to use catalogs simulations
                if (picoBatch.getNodeVersion().compareTo("16B") == 0) {
                    simulationName = "RNCV8248x1-FT-PRBS16Bx3-RBSU4360x5-RXIK190x2-RNC11";
                } else {
                    simulationName = "NA";
                }
                break;
            case "LRAN":
                simulationName = "LTE16B-WCDMA-V2x3-FT-PICO-FDD-LTE47";
                break;
            case "MSRAN":
                simulationName = "";
                // TODO in 17B
                break;
            default:
                log.info("Wrong domain, returning!!!");
                assertFalse(true);
                return;
        }

        try {
            prepareNetsim(picoBatch, true);
            netsimOperator = new BsimOperator();

            runNetsimIntegrationForNewStateMachine(picoBatch);
            log.info("AI complete, proceeding for Cleanup of netsim");
        } catch (final Exception e) {
            log.info(e.getMessage());
        } finally {
            cleanUpNetsim(picoBatch);

        }
    }

    public void prepareNetsim(final MockBsimBatch mockWRANPicoBatch, final boolean isForNewStateMachine) {

        if (!netsimHasBeenUsed) {
            setTestStep("Prepare for Netsim Integration");
            log.info("Netsim not configured ==> Netsim will be now be configured");
            NetsimApiOperator.prepareNetsim();
            netsimOperator = new BsimOperator();

            setTestStep("Prepare Netsim");
            netsimOperator.checkAndStopIpAddressIfStartedInNetsim(mockWRANPicoBatch.getIpAddress());
            final RemoteObjectHandler netsimFileHandler = BsimApiGetter.getNetsimRemoteFileHandler();

            log.info("Checking if Simulation " + simulationName + " exists for " + mockWRANPicoBatch.getRantype());
            final boolean isSimulationPresent = netsimOperator.checkSimulationExists(simulationName);

            if (isSimulationPresent) {
                log.info("Simulation Present in server....moving forward");
            } else {

                switch (mockWRANPicoBatch.getRantype()) {
                    case WRAN:
                        if (mockWRANPicoBatch.getNodeVersion().compareTo("16B") == 0) {
                            simulationName = "TAF_PICO_WRAN_16B";
                        } else {
                            simulationName = "TAF_PICO_WRAN_14B";
                        }
                        break;
                    case LRAN:
                        simulationName = "";
                        break;
                    case WLRAN:
                        simulationName = "";
                        break;
                    default:
                        simulationName = "";
                }
                if (simulationName.isEmpty()) {
                    log.error("Invalid domain for AI ");
                    assertFalse(true);
                }

                // Exporting simulations to server form local dir :-> ERICTAFbsim_CXP9030600/src/main/resources/netsimSimulations
                transferAndImportSimulations(netsimFileHandler, mockWRANPicoBatch);
                /*
                 * if (!isForNewStateMachine) {
                 * transferKertayleScripts(netsimFileHandler);
                 * netsimOperator.deleteSimulation(simulationName);
                 * assertEquals("Create Simulation Successful", netsimOperator.createNewSimulation(simulationName));
                 * }
                 */

            }

            netsimHasBeenUsed = true;

            // assertEquals("Security Config file transfered to OMSAS sucessfully",
            // netsimOperator.transferSecurityConfigurationFilesToOmsas());

        }
    }

    public void runNetsimIntegration(final MockBsimBatch picoBatch) throws InterruptedException {

        setTestStep("Run Netsim Integration");
        log.info("Test case is an End to End - Auto Integration Test case ==> Netsim will now be run");

        // checkSecurityFilesForNetsimGeneratedAndTransfered();

        checkPicoPortsCreatedSuccessfully(picoBatch);

        assertEquals("Node " + picoBatch.getNodeNameForNetsim() + " created in netsim successfully",
                picoNetsimApiOperator.createPicoNodeInNetsim(simulationName, picoBatch));

        assertEquals(false, netsimOperator.stopNode(simulationName, picoBatch.getNodeNameForNetsim()));
        // Removed on 30th march 2016 as SSL security definition not required in WRAN AI
        // assertEquals("SSL Security Definition applied to NE Successfully",
        // picoNetsimApiOperator.applySSLSecurityDefinition(simulationName, picoBatch));

        // checkRncAndNodeHaveBeenRelated(picoBatch);

        checkAttributesHaveBeenUpdatedCorrectly(picoBatch);

        assertEquals(true, picoNetsimApiOperator.checkRNCisSynchronized(picoBatch.getRncFdn()));
        assertEquals(true, picoNetsimApiOperator.attachPicoWRANNodeToMibAdapter(picoBatch));
        // assertEquals(true, picoNetsimApiOperator.updateMoInONRMforPicoWRANNode(picoBatch));
        // Removal of NextHop Mo from Simulation before checking synchronization
        picoNetsimApiOperator.removeNexthopMoFromSimulationForPicoWRAN(simulationName, picoBatch);
        assertEquals("Node synchronization successful", netsimOperator.checkPicoWRANNodeSynced(picoBatch));

        checkRbsconfigLevelUpdatedAndPlanActivated(picoBatch);

    }

    public void runNetsimIntegrationForNewStateMachine(final MockBsimBatch picoBatch) throws InterruptedException {

        setTestStep("Run Netsim Integration");
        log.info("Test case is an End to End - Auto Integration Test case 16B and above ==> Netsim will now be run");

        // assertEquals("Generated security files on Omsas successfully", picoNetsimApiOperator.generateSecurityFilesOnOmsas());
        assertEquals(true, picoNetsimApiOperator.attachPicoWRANNodeToMibAdapter(picoBatch));
        checkAttributesHaveBeenUpdatedCorrectlyForNewStateMachine(picoBatch);

        // assertEquals(true, picoNetsimApiOperator.checkRNCisSynchronized(picoBatch.getRncFdn()));
        assertEquals(true, picoNetsimApiOperator.attachPicoWRANNodeToMibAdapter(picoBatch));
        // assertEquals(true, picoNetsimApiOperator.updateMoInONRMforPicoWRANNode(picoBatch));
        // Removal of NextHop Mo from Simulation before checking synchronization
        assertEquals("Node synchronization successful", netsimOperator.checkPicoWRANNodeSynced(picoBatch));

        checkRbsconfigLevelUpdatedAndPlanActivated(picoBatch);

    }

    private void checkRbsconfigLevelUpdatedAndPlanActivated(final MockBsimBatch picoBatch) throws InterruptedException {
        assertEquals(true, picoNetsimApiOperator.setRBSConfigLevelPicoNodeInNetsim(simulationName, picoBatch, PicoWcdmaRbsConfigLevels.INTEGRATION_ONGOING));
        assertEquals(true, picoNetsimApiOperator.checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(picoBatch, PicoWcdmaRbsConfigLevels.INTEGRATION_ONGOING));

        assertEquals(true, picoNetsimApiOperator.setRBSConfigLevelPicoNodeInNetsim(simulationName, picoBatch, PicoWcdmaRbsConfigLevels.INTEGRATION_COMPLETE));
        // No need to check as RBCConfigLevel sets to OSS_CONFIGURATION_SUCCESSFUL after INTEGRATION_COMPLETE automatically in Auto Plan
        // assertEquals(true, picoNetsimApiOperator.checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(picoBatch,
        // PicoWcdmaRbsConfigLevels.INTEGRATION_COMPLETE));

        // Commented for now as AI not possible with manual plan currently
        // netsimOperator.activatePlannedConfiguration(picoBatch.getPlanName(), picoBatch.getRncFdn(), picoBatch.getRncName());
        // assertEquals(true,
        // picoNetsimApiOperator.setRBSConfigLevelPicoNodeInNetsim(simulationName, picoBatch,
        // PicoWcdmaRbsConfigLevels.OSS_CONFIGURATION_SUCCESSFUL));
        // assertEquals(true, picoNetsimApiOperator.checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(picoBatch,
        // PicoWcdmaRbsConfigLevels.OSS_CONFIGURATION_SUCCESSFUL));
        //
        //
        if (picoBatch.getNodeVersion().compareTo("16B") >= 0) {
            assertEquals(true,
                    picoNetsimApiOperator.checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(picoBatch, PicoWcdmaRbsConfigLevels.OSS_MIB_SYNCHRONIZATION_SUCCESSFUL));
            assertEquals(true, picoNetsimApiOperator.setRBSConfigLevelPicoNodeInNetsim(simulationName, picoBatch, PicoWcdmaRbsConfigLevels.READY_FOR_SERVICE));
            assertEquals(true, picoNetsimApiOperator.checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(picoBatch, PicoWcdmaRbsConfigLevels.READY_FOR_SERVICE));

        } else {
            assertEquals(true,
                    picoNetsimApiOperator.checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(picoBatch, PicoWcdmaRbsConfigLevels.OSS_CONFIGURATION_SUCCESSFUL));
            assertEquals(true, picoNetsimApiOperator.setRBSConfigLevelPicoNodeInNetsim(simulationName, picoBatch, PicoWcdmaRbsConfigLevels.READY_FOR_SERVICE));
            assertEquals(true, picoNetsimApiOperator.checkRbsConfigLevelHasBeenSetOnPicoNodeInSeg(picoBatch, PicoWcdmaRbsConfigLevels.READY_FOR_SERVICE));

        }
    }

    @SuppressWarnings("unused")
    private void checkRncAndNodeHaveBeenRelated(final MockBsimBatch picoBatch) {
        assertEquals(false, netsimOperator.stopNode(simulationName, picoBatch.getRncNameForNetsim()));
        assertEquals("RNC and Node related successfully", picoNetsimApiOperator.relateCreatedNodes(simulationName, picoBatch));
        assertEquals(true, netsimOperator.startNode(simulationName, picoBatch.getRncNameForNetsim()));
        assertEquals("Starting Related Node in Netsim successful", picoNetsimApiOperator.startRelatedNode(simulationName, picoBatch));
    }

    private void checkAttributesHaveBeenUpdatedCorrectly(final MockBsimBatch picoBatch) {
        assertEquals("ManagedElementId updated on WRAN Pico Node successfully",
                picoNetsimApiOperator.setManagedElementIdOnWranPicoNode(simulationName, picoBatch));
        assertEquals("TrafficIpAddress updated on WRAN Pico Node successfully",
                picoNetsimApiOperator.setTrafficIpAddressOnWranPicoNode(simulationName, picoBatch));
        // assertEquals(true, picoNetsimApiOperator.setEmUrlAttributeOnWranPicoNodeInOnrm(picoBatch));
        // assertEquals(true, picoNetsimApiOperator.setIpAddressOnWranPicoNodeInOnrm(picoBatch));
        // assertEquals(true, picoNetsimApiOperator.setprotocolTransportOnWranPicoNodeInOnrm(picoBatch));
        assertTrue(picoNetsimApiOperator.updateMoInONRMforPicoWRANNode(picoBatch));

        assertEquals("EncapsulationMo updated on WRAN Pico Node successfully",
                picoNetsimApiOperator.setEncapsulationMoOnWranPicoNode(simulationName, picoBatch));
        assertEquals("AddressIPv4 updated on WRAN Pico Node successfully", picoNetsimApiOperator.setAddressIPv4InNetsim(simulationName, picoBatch));
        assertEquals("ManagedElementType updated on WRAN Pico successfully",
                picoNetsimApiOperator.setManagedElementTypeOnWranPicoNode(simulationName, picoBatch));
    }

    private void checkAttributesHaveBeenUpdatedCorrectlyForNewStateMachine(final MockBsimBatch picoBatch) {
        assertEquals("Updation Successful for NE Properties on Netsim Node",
                picoNetsimApiOperator.setNePropertiesInNetsimNodeForNewStateMachine(simulationName, picoBatch));

        assertTrue(picoNetsimApiOperator.updateMoInONRMforPicoWRANNode(picoBatch));

    }

    private void checkPicoPortsCreatedSuccessfully(final MockBsimBatch picoBatch) {
        assertEquals("Port created in netsim successfully", picoNetsimApiOperator.createPicoPort(picoBatch));
        assertEquals("Port created in netsim successfully",
                picoNetsimApiOperator.createPicoDefaultDestinationPort(picoBatch.getRantype().toString(), "192.168.103.1"));
    }

    private void checkSecurityFilesForNetsimGeneratedAndTransfered() {
        assertEquals("Generated security files on Omsas successfully", picoNetsimApiOperator.generateSecurityFilesOnOmsas());
        assertEquals("Security files transfered to local successfully", picoNetsimApiOperator.transferSecurityConfigurationFilesToLocal());
        assertEquals("Transfered Security Certificated To NETSIM successfully", picoNetsimApiOperator.transferSecurityConfigurationFilesToNETSIM());
        assertEquals("SSL Security Definition Created Successfully", picoNetsimApiOperator.createSSLSecurityDefinition(simulationName));
    }

    public void cleanUpNetsim() {
        setTestStep("Deleting Netsim Simulation");
        netsimOperator.deleteSimulation(simulationName);
        setTestStep("Removing secuity folder/files on netsim,OMSAS and Local");
        picoNetsimApiOperator.removeSecurityFolderOnOmsasAndNetsim();
        picoNetsimApiOperator.removeSecurityFilesOnLocal();

    }

    public void cleanUpNetsim(final MockBsimBatch picoBatch) {
        setTestStep("Deleting Netsim Simulation!");
        netsimOperator.deleteSimulation(simulationName);
        log.info("I am here");
        if (!simulationName.contains("TAF")) {
            final boolean isImportSimsSuccessful = netsimOperator.importSimulations(simulationName + ".zip");
            log.info("Netsim simulations imported successfully?: " + isImportSimsSuccessful);
            if (!isImportSimsSuccessful) {
                log.error("Need to manually import simulation on Netsim server to use again!");
            }
        } else {
            netsimOperator.deleteFileFromRemoteHost("/netsim/netsimdir/" + simulationName + ".zip");
        }

        setTestStep("Removing secuity folder/files on netsim,OMSAS and Local");
        picoNetsimApiOperator.removeSecurityFolderOnOmsasAndNetsim();
        picoNetsimApiOperator.removeSecurityFilesOnLocal();

    }

    public void restartOriginalRNC(final String rncName) {
        log.info("Restarting default " + rncName);
        netsimOperator.startRNCinNetsim(rncName);
    }

    private void checkIfRncNeedsToBeCreated(final MockBsimBatch picoBatch) throws InterruptedException {

        NodeType nodeType = null;

        nodeType = getNodeTypeFromRanType(picoBatch, nodeType);

        setTestStep("Stop default RNC in Netsim");
        assertEquals(true, netsimOperator.stopRncInNetsim(picoBatch.getRncName(), picoBatch.getRncFdn()));
        setTestStep("Create RNC Port");
        assertEquals("Port created in netsim successfully", netsimOperator.createPort(RNC_PORT_NAME, picoBatch.getRncIpAddress()));

        setTestStep("Create RNC in Netsim ");
        assertEquals("RNC created in netsim successfully", netsimOperator.createRNCInNetsim(simulationName, RNC_PORT_NAME, picoBatch.getRncNameForNetsim()));

        setTestStep("Configure RNC node for BSIM");
        assertEquals("configureNodeForBsim command executed successfully",
                netsimOperator.executeConfigureNodeForBsimScript(simulationName, picoBatch.getRncNameForNetsim(), nodeType));
        Thread.sleep(10000);

        assertEquals(true, netsimOperator.startNode(simulationName, picoBatch.getRncNameForNetsim()));
    }

    private NodeType getNodeTypeFromRanType(final MockBsimBatch picoBatch, NodeType nodeType) {
        if (picoBatch.getRantype().equals(RanType.WRAN)) {
            nodeType = NodeType.PICO_WCDMA;
        } else if (picoBatch.getRantype().equals(RanType.LRAN)) {
            nodeType = NodeType.PICO_LTE;
        } else if (picoBatch.getRantype().equals(RanType.WLRAN)) {
            nodeType = NodeType.MSRAN;
        }
        return nodeType;
    }

    /**
     * Transfer Netsim simulations from the resources folder
     * 
     * @param netsimFileHandler
     */
    private void transferAndImportSimulations(final RemoteObjectHandler netsimFileHandler, final MockBsimBatch mockWRANPicoBatch) {
        final String remoteFolderForSims = "/netsim/netsimdir/";

        final String simulationToExport;
        switch (mockWRANPicoBatch.getNodeVersion()) {
            case "14B":
                simulationToExport = "TAF_PICO_WRAN_14B.zip";
                break;
            case "16B":
                simulationToExport = "TAF_PICO_WRAN_16B.zip";
                break;
            default:
                // add more versions as and when required
                simulationToExport = "";
                log.error("AI support for node version " + mockWRANPicoBatch.getNodeVersion() + " not available yet, kindly contact CI SPOC for support.");
                assertFalse(true);
        }

        String scriptFolder = DataHandler.getAttribute("picoSims").toString();
        scriptFolder = scriptFolder + File.separator;
        final List<String> netsimSimulations = FileFinder.findFile(simulationToExport, scriptFolder);
        // log.info("Finding the Pico Simulations- : " + netsimSimulations);
        // if (netsimSimulations.isEmpty()) {
        // log.info("Running in KGB, modifying file path...");
        // netsimSimulations = FileFinder.findFile("PICO_SIM.zip", "/target/");
        //
        // for (final Iterator<String> iterator = netsimSimulations.iterator(); iterator.hasNext();) {
        // final String ss = iterator.next();
        // if (ss.contains("/")) {
        // iterator.remove();
        // }
        // }
        // }
        boolean transferResult = false;
        boolean isImportSimsSuccessful = false;
        for (final String simulationLocation : netsimSimulations) {
            log.info("Trying to export from Netsim Simulations Location: " + simulationLocation);
            transferResult = remoteFilesHandler.transferLocalFilesToRemote(netsimFileHandler, remoteFolderForSims, java.util.Arrays.asList(simulationLocation));
            if (transferResult == true) {
                isImportSimsSuccessful = netsimOperator.importSimulations(simulationLocation);
                log.info("Netsim simulations imported successfully?: " + isImportSimsSuccessful);
                break;
            }

        }
        assertEquals(true, transferResult);

        assertEquals(true, isImportSimsSuccessful);
    }

    /**
     * Transfer Kertayle scripts from the resources folder
     * 
     * @param netsimFileHandler
     */
    private void transferKertayleScripts(final RemoteObjectHandler netsimFileHandler) {
        final String folderForKertayleScripts = "user_cmds_taf/";
        // List<String> kertayleScripts = FileFinder.findFile(".mo", "/target");
        // if (kertayleScripts.isEmpty()) {
        // log.info("Running in KGB, modifying file path...");
        // }
        final List<String> kertayleScripts = FileFinder.findFile(".mo");

        for (final String scriptLocation : kertayleScripts) {
            log.info("Script Simulations Location: " + scriptLocation);
        }
        BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostNetsim()).simpleExec("mkdir " + folderForKertayleScripts);
        final boolean areFilesTransferred = remoteFilesHandler.transferLocalFilesToRemote(netsimFileHandler, "/netsim/" + folderForKertayleScripts,
                kertayleScripts);
        log.info("Kertayle scripts transferred sucessfully: " + areFilesTransferred);
        assertEquals(true, areFilesTransferred);
    }

    /**
     * @param mockWRANPicoBatch
     * @param b
     */
    public void verfiySMRS(final MockWRANPicoBatch mockWRANPicoBatch, final boolean b) {
        // TODO Auto-generated method stub (Dec 26, 2016:6:58:45 AM by xriskas)
        Assert.assertTrue(bsimAddPicoBatchApiOperator.checkDoesSmrsAccountExistforBatch(mockWRANPicoBatch, b));

    }

}

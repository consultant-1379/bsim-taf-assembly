package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.bsim.batch.data.model.MockWRANPicoBatch;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimBatchOperator;
import com.ericsson.oss.bsim.operators.api.BsimDeleteNodeApiOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckLocalTempDir;
import com.ericsson.oss.bsim.test.data.BsimTestData;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

public class BsimWRANBatchE2E extends TorTestCaseHelper implements TestCase {

    private static Logger log = Logger.getLogger(BsimWRANBatchE2E.class);

    private final List<String> nodesToDeletebyBSIM = new ArrayList<String>();

    private final List<String> nodesToDeletebyARNE = new ArrayList<String>();

    private boolean nodesDeletedbyBsim = false;

    @SuppressWarnings("unused")
    private final boolean nodesDeletedbyArne = false;

    private final List<MockWRANPicoBatch> addedBatches = new ArrayList<MockWRANPicoBatch>();

    private BsimDeleteNodeApiOperator deleteNodeOperator;

    @BeforeClass
    public void prepareTheRun() {

        // assertTrue(new PreCheckUpgradePackages().checkUpgradePackages());

        setTestcase("BSIM_PREPARE_FOR_PICO_WRAN_BATCH_E2E_KGB", "Launch CEX if not running");

        setTestStep("Pre-check before running PICO WCDMA test suite");
        assertTrue(new PreCheckLocalTempDir().createTempDir());
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.PICO_WCDMA, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(preCheckManager.doAllPreChecks(), true);

    }

    @Test(dataProvider = "newWRANPicoBatchE2E", dataProviderClass = BsimTestData.class, groups = { "KGB" })
    public void addWRANPicoBatch(
            @TestId @Input("TC ID") final String tcId,
            final String tcTitle,
            final String tcDesc,
            final MockWRANPicoBatch mockWRANPicoBatch,
            final String bind,
            final String nodesTobind,
            final String deleteBoundNodes,
            final String batchExpectedResult,
            final String bindExpectedResult,
            final String deleteExpectedResult,
            final String isEndToEnd) throws InterruptedException {

        setTestcase(tcId, tcTitle);
        setTestInfo(tcDesc);

        final BsimBatchOperator bsimBatchOperator = new BsimBatchOperator();
        final BsimAddPicoBatchHelper addPicoBatchHelper = new BsimAddPicoBatchHelper();

        addPicoBatchHelper.doAddBatch(bsimBatchOperator, mockWRANPicoBatch);

        addedBatches.add(mockWRANPicoBatch);

        addPicoBatchHelper.checkQRCodeIsGenerated(bsimBatchOperator, mockWRANPicoBatch.getName());

        List<String> boundNodeFdns = new ArrayList<String>();
        // List<String> actualResult = new ArrayList<String>();

        if (bind.equalsIgnoreCase("true") && !nodesTobind.equals("")) {
            boundNodeFdns = addPicoBatchHelper.bindBatch(bsimBatchOperator, mockWRANPicoBatch, Integer.parseInt(nodesTobind), bindExpectedResult);

            if (!mockWRANPicoBatch.IsEndToEnd()) {
                log.info("Starting Wran Bind for " + mockWRANPicoBatch.getName());
                if (boundNodeFdns.size() != 0) {
                    nodesToDeletebyBSIM.addAll(boundNodeFdns);
                }
            }

            String success = "Unsuccessful";
            if (boundNodeFdns.size() == Integer.parseInt(nodesTobind)) {
                success = "Successful";
            }
            Assert.assertEquals(success, bindExpectedResult);
            setTestStep("Verify SMRS Account has been created after Batch Bind");

            addPicoBatchHelper.verfiySMRS(mockWRANPicoBatch, true);

        }

        if (mockWRANPicoBatch.IsEndToEnd()) {
            log.info("Starting Wran End to End test case for " + mockWRANPicoBatch.getName());
            if (mockWRANPicoBatch.getNodeVersion().compareTo("16B") == 0 || mockWRANPicoBatch.getNodeVersion().compareTo("14B") == 0) {
                addPicoBatchHelper.doNetsimSynchronizationForNewStateMachine(mockWRANPicoBatch, "WRAN");
            } else {
                log.error("AI support for node version : " + mockWRANPicoBatch.getNodeVersion() + " not implimented yet!");
                // addPicoBatchHelper.doNetsimSynchronization(mockWRANPicoBatch);
            }
            nodesToDeletebyARNE.addAll(boundNodeFdns);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        setTestcase("BSIM_CLEANUP_WRAN_BATCH", "TAF - clean up server after tests have run");
        setTestStep("After Class ==> Deleting Nodes");
        final BsimBatchOperator bsimBatchOperator = new BsimBatchOperator();
        final BsimAddPicoBatchHelper addPicoBatchHelper = new BsimAddPicoBatchHelper();

        deleteNodeOperator = new BsimDeleteNodeApiOperator();
        for (final MockWRANPicoBatch picoBatch : addedBatches) {

            if (!picoBatch.getIsAutoPlan()) {
                deleteNodeOperator.deletePCA(picoBatch.getPlanName());
            }
        }
        for (final String nodeFdnValue : nodesToDeletebyARNE) {
            deleteNodeOperator.deleteE2EPicoNodeUsingARNE(nodeFdnValue, getNodeName(nodeFdnValue));
        }

        if (nodesDeletedbyBsim == false) {

            addPicoBatchHelper.deleteBindWRANBatchNodes(bsimBatchOperator, nodesToDeletebyBSIM, "Successful");
            nodesDeletedbyBsim = true;
        }
        setTestStep("After Class ==> Deleting Batches");
        addPicoBatchHelper.deleteWranBatchesAfterTestExecution(bsimBatchOperator, addedBatches);

    }

    /**
     * @param nodeFdnValue
     * @return
     */
    private String getNodeName(final String nodeFdnValue) {

        return nodeFdnValue.substring(nodeFdnValue.lastIndexOf("="));

    }

}

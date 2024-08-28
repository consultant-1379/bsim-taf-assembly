package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.bsim.batch.data.model.MockWLRANPicoBatch;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimBatchOperator;
import com.ericsson.oss.bsim.operators.api.BsimAddPicoBatchApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimDeleteNodeApiOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.test.data.BsimTestData;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

public class BsimWLRANBatch extends TorTestCaseHelper implements TestCase {

    private final List<String> nodesToDeletebyBSIM = new ArrayList<String>();

    private final List<String> nodesToDeletebyARNE = new ArrayList<String>();

    private boolean nodesDeletedbyBsim = false;

    private boolean nodesDeletedbyArne = false;

    private final List<MockWLRANPicoBatch> addedBatches = new ArrayList<MockWLRANPicoBatch>();

    private BsimDeleteNodeApiOperator deleteNodeOperator;

    private final BsimAddPicoBatchApiOperator bsimAddPicoBatchApiOperator = new BsimAddPicoBatchApiOperator();

    @BeforeClass
    public void prepareTheRun() {

        // assertTrue(new PreCheckUpgradePackages().checkUpgradePackages());

        setTestcase("BSIM_PREPARE_FOR_PICO_WLRAN_BATCH", "Launch CEX if not running");

        setTestStep("Pre-check before running PICO multirat test suite");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.MSRAN, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(preCheckManager.doAllPreChecks(), true);

    }

    @Test(dataProvider = "newWLRANPicoBatch", dataProviderClass = BsimTestData.class, groups = { "KGB" })
    public void addWLRANPicoBatch(
            @TestId @Input("TC ID") final String tcId,
            final String tcTitle,
            final String tcDesc,
            final MockWLRANPicoBatch mockWLRANPicoBatch,
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

        addPicoBatchHelper.doAddBatch(bsimBatchOperator, mockWLRANPicoBatch);

        addedBatches.add(mockWLRANPicoBatch);

        addPicoBatchHelper.checkQRCodeIsGenerated(bsimBatchOperator, mockWLRANPicoBatch.getName());

        List<String> boundNodeFdns = new ArrayList<String>();
        if (bind.equalsIgnoreCase("true") && !nodesTobind.equals("")) {
            boundNodeFdns = addPicoBatchHelper.bindBatch(bsimBatchOperator, mockWLRANPicoBatch, Integer.parseInt(nodesTobind), bindExpectedResult);
            if (!mockWLRANPicoBatch.IsEndToEnd()) {
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

            Assert.assertTrue(bsimAddPicoBatchApiOperator.checkDoesSmrsAccountExistforBatch(mockWLRANPicoBatch, true));
            //
        }
        // This method is for future use for AI of MRAT.
        if (mockWLRANPicoBatch.IsEndToEnd()) {

            addPicoBatchHelper.doNetsimSynchronization(mockWLRANPicoBatch);
            nodesToDeletebyARNE.addAll(boundNodeFdns);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        setTestcase("BSIM_CLEANUP_WLRAN_BATCH", "TAF - clean up server after tests have run");
        setTestStep("After Class ==> Deleting Nodes");
        final BsimBatchOperator bsimBatchOperator = new BsimBatchOperator();
        final BsimAddPicoBatchHelper addPicoBatchHelper = new BsimAddPicoBatchHelper();

        deleteNodeOperator = new BsimDeleteNodeApiOperator();
        for (final MockWLRANPicoBatch picoBatch : addedBatches) {
            // if (picoBatch.getIsAutoPlan()) {
            // deleteNodeOperator.deletePCAwhenAuto(picoBatch);
            // }
            if (!picoBatch.getIsAutoPlan()) {
                deleteNodeOperator.deletePCA(picoBatch.getPlanName());
            }
            // This if part will only run for future use for AI of MRAT.
            if (picoBatch.IsEndToEnd() && nodesDeletedbyArne == false) {
                final String nodeName = picoBatch.getNodeName();
                for (final String nodeFdnValue : picoBatch.getNodeFdnValues()) {
                    deleteNodeOperator.deleteE2EPicoNodeUsingARNE(nodeFdnValue, nodeName);
                }
                nodesDeletedbyArne = true;

            } else if (nodesDeletedbyBsim == false) {

                addPicoBatchHelper.deleteBindWLRANBatchNodes(bsimBatchOperator, nodesToDeletebyBSIM, "Successful");
                nodesDeletedbyBsim = true;
            }
        }

        setTestStep("After Class ==> Deleting Batches");
        addPicoBatchHelper.deleteWlranBatchesAfterTestExecution(bsimBatchOperator, addedBatches);

    }
}

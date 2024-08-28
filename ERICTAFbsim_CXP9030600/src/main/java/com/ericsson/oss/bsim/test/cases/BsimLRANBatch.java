package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.log4testng.Logger;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.Context;
import com.ericsson.cifwk.taf.annotations.DataDriven;
import com.ericsson.cifwk.taf.annotations.Input;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimBatchOperator;
import com.ericsson.oss.bsim.operators.api.AddDeleteNodeClusterOperator;
import com.ericsson.oss.bsim.operators.api.BsimAddPicoBatchApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimDeleteNodeApiOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckLocalTempDir;
import com.ericsson.oss.bsim.test.cases.BsimAddPicoBatchHelper;
import com.ericsson.oss.bsim.test.data.BsimTestData;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

public class BsimLRANBatch extends TorTestCaseHelper implements TestCase {

    private static Logger log;

    private final List<String> nodesToDelete = new ArrayList<String>();

    private final List<MockLRANPicoBatch> addedBatches = new ArrayList<MockLRANPicoBatch>();

    String clusterType;

    String elementIds;

    AddDeleteNodeClusterOperator operator;

    private final BsimAddPicoBatchApiOperator bsimAddPicoBatchApiOperator = new BsimAddPicoBatchApiOperator();

    @BeforeClass
    public void prepareTheRun() {
        operator = new AddDeleteNodeClusterOperator();
        assertTrue(new PreCheckLocalTempDir().createTempDir());

        // assertTrue(new PreCheckUpgradePackages().checkUpgradePackages());
        log = Logger.getLogger(BsimLRANBatch.class);

        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.PICO_LTE, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(preCheckManager.doAllPreChecks(), true);

    }

    @TestId(id = "OSS-39876_FUNC_1", title = "Add/Delete Node Cluster")
    @DataDriven(name = "AddDeleteCluster")
    @Context(context = { Context.API })
    @Test(groups = { "KGB", "VCDB" })
    public void addDeleteCluster(
            @Input("ClusterType") final String clusterType,
            @Input("NoOfRbs") final int noOfRbs,
            @Input("NoOfErbs") final int noOfErbs,
            @Input("NoOfRnc") final int noOfRnc,
            @Input("NoOfPrbs") final int noOfPrbs,
            @Input("NoOfPerbs") final int noOfPerbs) {

        this.clusterType = clusterType;
        setTestStep("Get " + clusterType + " from cex domain cache using TopologyManager.");
        final String elementIds = operator.getElementIds(clusterType, noOfRbs, noOfErbs, noOfRnc, noOfPrbs, noOfPerbs);
        setTestInfo("verify that the list of nodes retrieved is not empty");
        assertNotNull(elementIds);
        this.elementIds = elementIds;
        setTestStep("Create cluster using cex TopologyManager.");
        assertTrue(operator.createCluster(clusterType, elementIds));
        setTestInfo("Verify that the cluster is created in cex domain cache with the correct elements.");
        assertTrue(operator.verifyChangesInDomain(clusterType, elementIds));

        setTestStep("Edit the created cluster using cex TopologyManager and make it empty.");
        assertTrue(operator.editCluster(clusterType, elementIds));

    }

    @Test(dataProvider = "newLRANPicoBatch", dataProviderClass = BsimTestData.class, groups = { "KGB" })
    public void addLRANPicoBatch(
            @TestId @Input("TC ID") final String tcId,
            final String tcTitle,
            final String tcDesc,
            final MockLRANPicoBatch mockLRANPicoBatch,
            final String bind,
            final String nodesTobind,
            final String deleteBoundNodes,
            final String batchExpectedResult,
            final String bindExpectedResult,
            final String deleteExpectedResult,
            final String addCluster) throws InterruptedException {

        setTestcase(tcId, tcTitle);
        setTestInfo(tcDesc);

        final BsimBatchOperator bsimBatchOperator = new BsimBatchOperator();
        final BsimAddPicoBatchHelper bsimAddPicoBatchHelper = new BsimAddPicoBatchHelper();

        final List<String> fdnPreCheckList = new ArrayList<String>();
        final Iterator<String> iterator = mockLRANPicoBatch.getNodeFdnValues().iterator();
        String s = "";
        while (iterator.hasNext()) {
            s = iterator.next();
            if (moExist(bsimBatchOperator, s).equals("true")) {
                log.error("PreCheck: MO Exists " + s);
                fdnPreCheckList.add(s);
            }
        }

        if (!fdnPreCheckList.isEmpty()) {
            log.info("PreCheck: Attempting to delete found nodes");
            deleteBindLRANBatchNodes(bsimBatchOperator, fdnPreCheckList, deleteExpectedResult);
        }

        bsimAddPicoBatchHelper.doAddBatch(bsimBatchOperator, mockLRANPicoBatch);
        bsimAddPicoBatchHelper.checkQRCodeIsGenerated(bsimBatchOperator, mockLRANPicoBatch.getName());
        addedBatches.add(mockLRANPicoBatch);

        if (bind.equalsIgnoreCase("true") && !nodesTobind.equals("")) {
            List<String> boundNodeFdns;

            if (!mockLRANPicoBatch.getIsNoHardwareBind()) {
                boundNodeFdns = bsimAddPicoBatchHelper.bindBatch(bsimBatchOperator, mockLRANPicoBatch, Integer.parseInt(nodesTobind), bindExpectedResult);

                if (boundNodeFdns.size() != 0) {
                    nodesToDelete.addAll(boundNodeFdns);
                }

                String success = "Unsuccessful";
                if (boundNodeFdns.size() == Integer.parseInt(nodesTobind)) {
                    success = "Successful";
                }
                Assert.assertEquals(success, bindExpectedResult);
                setTestStep("Verify SMRS Account has been created after Batch Bind");

                Assert.assertTrue(bsimAddPicoBatchApiOperator.checkDoesSmrsAccountExistforBatch(mockLRANPicoBatch, true));
            } else {
                boundNodeFdns = bsimAddPicoBatchHelper.executeNoHardwareBindLRANBatch(bsimBatchOperator, mockLRANPicoBatch, Integer.parseInt(nodesTobind),
                        bindExpectedResult);
                if (boundNodeFdns.size() != 0) {
                    nodesToDelete.addAll(boundNodeFdns);
                }
                String success = "Unsuccessful";
                if (boundNodeFdns.size() == Integer.parseInt(nodesTobind)) {
                    success = "Successful";
                }
                Assert.assertEquals(success, bindExpectedResult);

                setTestStep("Verify SMRS Account has been created after Batch Bind");
                Assert.assertTrue(bsimBatchOperator.checkDoesSmrsAccountExistforBatch(mockLRANPicoBatch, true));

                setTestStep("Verify CCF created");
                Assert.assertTrue(bsimBatchOperator.checkCCFFileExists());
            }
            // nodesToDelete.addAll(boundNodeFdns);
        }
        if (addCluster.equalsIgnoreCase("TRUE")) {

            checkClusterNodes();
        }

    }

    private String moExist(final BsimBatchOperator bsimBatchOperator, final String s) {

        return bsimBatchOperator.moExist(s);
    }

    private void checkClusterNodes() {

        setTestInfo("Verify in cex domain cache that the Node is added in cluster.");
        assertEquals(operator.verifyChangesInDomain(clusterType, elementIds), true);
    }

    private String deleteBindLRANBatchNodes(final BsimBatchOperator bsimBatchOperator, final List<String> nodesToDelete, final String expectedResult) {

        final String actualResult = bsimBatchOperator.deletebindLRANBatchNodes(nodesToDelete);
        Assert.assertEquals(expectedResult, actualResult);
        return actualResult;
    }

    @AfterClass(alwaysRun = true)
    private void cleanUp() {

        setTestcase("BSIM_CLEANUP_LRAN_BATCH", "TAF - clean up server after tests have run");
        setTestStep("After Class ==> Deleting Nodes");

        final BsimBatchOperator bsimBatchOperator = new BsimBatchOperator();

        if (!nodesToDelete.isEmpty()) {
            setTestStep("Deleting LRAN batch nodes via calling BSIM Service...");
            try {
                final String actualResult = deleteBindLRANBatchNodes(bsimBatchOperator, nodesToDelete, "Successful");
                Assert.assertEquals(actualResult, "Successful");
            } catch (final Exception e) {
                log.error("Error when deleting bound nodes with exception: " + e.getMessage());
            } catch (final AssertionError e) {
                // allow Cleanup to complete
            }
        } else {
            log.info("No nodes were bound during test case ==> No nodes have been deleted");
        }
        setTestStep("After Class ==> Checking Nodes have been deleted");
        Assert.assertTrue(checkBoundNodesDeletedInCs(bsimBatchOperator));
        setTestStep("After Class ==> Deleting Batches");
        deleteLranBatchesAfterTestExecution(bsimBatchOperator);
        setTestStep("After Class ==> Delete CCF");
        deleteNoHardwareCCFAfterTestExecution();
        setTestStep("After Class ==> Delete Cluster");
        deleteAnEmptyCluster();

    }

    public void deleteAnEmptyCluster() {

        operator.deleteCluster();
        assertTrue(operator.verifyClusterDeletedInDomain());

    }

    private boolean checkBoundNodesDeletedInCs(final BsimBatchOperator bsimBatchOperator) {
        return bsimBatchOperator.checkBoundNodesDeletedInCS(nodesToDelete);
    }

    private void deleteLranBatchesAfterTestExecution(final BsimBatchOperator bsimBatchOperator) {
        for (final MockLRANPicoBatch addedBatch : addedBatches) {
            try {
                log.info("Deleting Batch ==> " + addedBatch.getName());
                final String actualResult = bsimBatchOperator.deleteLRANBatch(addedBatch);
                Assert.assertEquals(actualResult, "true");
                setTestStep("Verify SMRS Account has been deleted after Clean Up");
                Assert.assertFalse(bsimBatchOperator.checkDoesSmrsAccountExistforBatch(addedBatch, false));
            } catch (final Exception e) {
                log.error("Failed to delete Batch ==> " + addedBatch.getName() + "with exception " + e.getMessage());
            } catch (final AssertionError e) {
                // allow the cleanup to complete despite AssertionError
            }
        }
    }

    /**
     * Deletes Combined Configuration File from the server after the NoHardware Bind Test has ran
     */
    private void deleteNoHardwareCCFAfterTestExecution() {

        final BsimDeleteNodeApiOperator deleteNodeOperator = new BsimDeleteNodeApiOperator();
        deleteNodeOperator.deleteCCFFile();
    }
}

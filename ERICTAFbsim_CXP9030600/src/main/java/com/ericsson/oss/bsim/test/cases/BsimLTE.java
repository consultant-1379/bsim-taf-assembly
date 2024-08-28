package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.test.data.BsimTestData;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;
import com.ericsson.oss.bsim.utils.IpAddressManager;
import com.ericsson.oss.bsim.utils.NodeCopier;

public class BsimLTE extends TorTestCaseHelper implements TestCase {

    private static Logger log = Logger.getLogger(BsimLTE.class);

    private final ArrayList<BsimNodeData> addedNodes = new ArrayList<BsimNodeData>();

    @BeforeClass
    public void prepareTheRun() {

        setTestcase("BSIM_PRECHECK_FOR_LTE", "Preparation for LTE Test Run");

        setTestStep("Pre-check before running LTE test suite");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.LTE, new BsimTestCaseReportHelper(this));

        Assert.assertEquals(preCheckManager.doAllPreChecks(), true);

        addedNodes.clear();

    }

    @Test(dataProvider = "newLTENodes", dataProviderClass = BsimTestData.class, groups = { "KGB" })
    public void addLTENode(
            final String tcId,
            final String tcTitle,
            final String tcDesc,
            final BsimNodeData nodeData,
            final boolean expectedResult,
            final String numberOfNodes,
            final String timeToAdd) throws InterruptedException {

        setTestcase(tcId, tcTitle);
        setTestInfo(tcDesc);

        final BsimAddMacroNodeHelper addMacroNodeHelper = new BsimAddMacroNodeHelper();

        if (numberOfNodes == null || numberOfNodes.equalsIgnoreCase("1")) {

            addMacroNodeHelper.doDataValidation(nodeData);
            addMacroNodeHelper.setRequiredPasParameters(nodeData);
            addMacroNodeHelper.doExecution(nodeData, expectedResult);
            addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));
            addedNodes.add(nodeData);
            addMacroNodeHelper.doVerification(nodeData, expectedResult);

            if (nodeData.getAifData().isWithoutLaptop()) {
                addMacroNodeHelper.bindMacroLteNode(nodeData);
            }

            addMacroNodeHelper.doNetsimSynchronization(nodeData);

        } else {
            final int nodesToAdd = Integer.valueOf(numberOfNodes);
            final long expectedTime = Long.valueOf(timeToAdd);

            final NodeCopier nodeCopier = new NodeCopier(nodeData);
            String nextIpAddress = nodeData.CriticalData.getIpAddress();

            final BsimNodeData newnodeData[] = new BsimNodeData[nodesToAdd];

            for (int i = 0; i < newnodeData.length; i++) {
                if (i > 0) {
                    nextIpAddress = IpAddressManager.nextIpAddress(nextIpAddress);
                }
                nodeCopier.createNode(nextIpAddress, newnodeData, i);

                addedNodes.add(newnodeData[i]);

                addMacroNodeHelper.doDataValidation(newnodeData[i]);
                addMacroNodeHelper.setRequiredPasParameters(nodeData);
                addMacroNodeHelper.doExecution(newnodeData[i], expectedResult);
            }
            ExecuteAndTime(addMacroNodeHelper, nodesToAdd, expectedTime);
        }
    }

    private void ExecuteAndTime(final BsimAddMacroNodeHelper addMacroNodeHelper, final int nodesToAdd, final long expectedTime) {

        final long startTime = System.currentTimeMillis();
        // pass in number of nodes as a check, could get from list size in groovy code to but possibly better to check this way to.
        final String addSucessNodes = addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(nodesToAdd));
        final int addSucessNodesNum = Integer.parseInt(addSucessNodes);

        Assert.assertEquals(addSucessNodesNum, nodesToAdd, "Failed to add " + nodesToAdd + " nodes " + addSucessNodes + " nodes added");
        final long finishTime = System.currentTimeMillis();

        final double actualTimeMinutes = (finishTime - startTime) * 0.000016666666666666667;

        boolean result = false;
        if (actualTimeMinutes <= expectedTime) {
            result = true;
        }

        log.info("Time taken to execute test case was  ==> " + actualTimeMinutes + " minutes");
        Assert.assertEquals(result, true, "Test failed was expected to be less than 30 minutes and was " + actualTimeMinutes);

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        setTestcase("BSIM_CLEANUP_LTE", "TAF - clean up server after tests have run");

        final BsimOperator operator = new BsimOperator();
        final BsimAddMacroNodeHelper addMacroNodeHelper = new BsimAddMacroNodeHelper();

        setTestStep("Deleting Nodes...");
        operator.doCleanUp(addedNodes);
        setTestStep("Clean up netsim...");
        addMacroNodeHelper.cleanUpNetsim();
        setTestStep("Verifying Clean Up");
        addMacroNodeHelper.doCleanUpVerification(addedNodes, false);
    }

}

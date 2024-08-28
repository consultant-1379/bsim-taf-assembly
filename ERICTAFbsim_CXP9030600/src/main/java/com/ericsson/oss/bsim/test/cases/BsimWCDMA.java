package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;

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

public class BsimWCDMA extends TorTestCaseHelper implements TestCase {

    private final ArrayList<BsimNodeData> addedNodes = new ArrayList<BsimNodeData>();

    @BeforeClass
    public void prepareTheRun() {
        setTestcase("BSIM_PRECHECK_FOR_WCDMA", "Preparation for WCDMA Test Run");
        setTestStep("Pre-check before running WCDMA test suite");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.WCDMA, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());
    }

    @Test(dataProvider = "newWCDMANodes", dataProviderClass = BsimTestData.class, groups = { "KGB", "VCDB" })
    public void addWCDMANode(
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

        // Do validation for test data
        addMacroNodeHelper.doDataValidation(nodeData);

        // Set Pas Parameter for Geo
        addMacroNodeHelper.setRequiredPasParameters(nodeData);

        // Execution of adding node
        addMacroNodeHelper.doExecution(nodeData, expectedResult);

        addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));

        // add node to deleteNode list
        addedNodes.add(nodeData);

        // Verification
        addMacroNodeHelper.doVerification(nodeData, expectedResult);

        // Manual Bind
        if (nodeData.getAifData().isAutoIntegrate()) {
            addMacroNodeHelper.bindMicroWcdmaNode(nodeData);
        }

        /*
         * if (nodeData.getAifData().isManualBind()) {
         * setTestStep("******  Deleting SiteInstallation File after Manual Bind" + nodeData.getNodeName() + "******************");
         * deleteSiteInstallationFile();
         * }
         */

        if (nodeData.isEndToEnd()) {
            addMacroNodeHelper.doNetsimSynchronization(nodeData);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        setTestcase("BSIM_CLEANUP_WCDMA", "TAF - clean up server after tests have run");
        final BsimOperator operator = new BsimOperator();
        for (final BsimNodeData nodeData : addedNodes) {
            setTestStep("****** Deleting node " + nodeData.getNodeName() + " ******");
            operator.deleteNode(nodeData);
        }
    }
}


/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.annotations.TestId;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.test.data.BsimTestData;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

/**
 * @author xnehmis
 */
public class BsimMicroMacroLTE extends TorTestCaseHelper implements TestCase {
    private final ArrayList<BsimNodeData> addedNodes = new ArrayList<BsimNodeData>();

    @BeforeClass
    public void prepareTheRun() {

        setTestcase("BSIM_PRECHECK_FOR_MICRO_Macro_LTE", "Preparation for MICRO/MACROLTE Test Run");

        setTestStep("Pre-check before running LTE test suite");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.MICRO_MACRO_LTE, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());

    }

    @Test(dataProvider = "newMicroMacroLTENodes", dataProviderClass = BsimTestData.class, groups = { "KGB", "VCDB" })
    public void addLTENode(
            @TestId final String tcId,
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

        // Execution of adding node
        addMacroNodeHelper.doExecution(nodeData, expectedResult);
        addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));

        // add node to deleteNode list
        addedNodes.add(nodeData);

        // Verification
        String result = addMacroNodeHelper.doVerification(nodeData, expectedResult);
        if (result.equalsIgnoreCase("false")) {
            Thread.sleep(5000);
            String name = nodeData.getNodeName();
            name = name + "123";
            nodeData.setNodeName(name);
            addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));
            result = addMacroNodeHelper.doVerification(nodeData, expectedResult);
        }

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        setTestcase("BSIM_CLEANUP_MICRO_MACRO_LTE", "TAF - clean up server after tests have run");

        setTestStep("Deleting Nodes...");
        final BsimOperator operator = new BsimOperator();
        for (final BsimNodeData nodeData : addedNodes) {
            setTestStep("****** Deleting node " + nodeData.getNodeName() + " ******");
            operator.deleteNode(nodeData);
        }

        // in case netsim integration failed
        final BsimAddMacroNodeHelper addMacroNodeHelper = new BsimAddMacroNodeHelper();
        if (addMacroNodeHelper.hasNetsimUsed()) {
            setTestStep("Clean up netsim...");
            try {
                addMacroNodeHelper.cleanUpNetsim();
            } catch (final NullPointerException npe) {
                // catch exception if there no simulation to delete
            }
        }
    }

}


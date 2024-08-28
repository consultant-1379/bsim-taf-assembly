/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
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
import com.ericsson.oss.bsim.operators.api.BsimDeleteNodeApiOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.test.data.BsimTestData;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

/**
 * @author xnehmis
 */
public class BsimSpitfire extends TorTestCaseHelper implements TestCase {
    private final ArrayList<BsimNodeData> addedNodes = new ArrayList<BsimNodeData>();

    @BeforeClass
    public void prepareTheRun() {

        setTestcase("BSIM_PRECHECK_FOR_SPITFIRE", "Preparation for SPITFIRE Test Run");

        setTestStep("Pre-check before running SPITFIRE test suite");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.Router_6672, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());

    }

    @Test(dataProvider = "newSpitfireNodes", dataProviderClass = BsimTestData.class, groups = { "KGB", "VCDB" })
    public void addSpitfireNode(
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

        // Preparing Spitfire Local Files
        addMacroNodeHelper.prepareLocalSpitfireFiles(nodeData);

        // Execution of adding node
        addMacroNodeHelper.doExecution(nodeData, expectedResult);
        addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));
        // MANUAL BIND
        if (nodeData.getAifData().isManualBind()) {
            addMacroNodeHelper.bindSpitFireNode(nodeData);
        }
        if (nodeData.getAifData().isManualBind()) {
            setTestStep("******  Deleting SiteInstallation File after Manual Bind" + nodeData.getNodeName() + "******************");
            deleteSiteInstallationFile();
        }

        // add node to deleteNode list
        addedNodes.add(nodeData);

        // Verification
        String result = addMacroNodeHelper.doVerification(nodeData, expectedResult);
        if (result.equalsIgnoreCase("false")) {
            Thread.sleep(5000);
            addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));
            result = addMacroNodeHelper.doVerification(nodeData, expectedResult);
        }
        // Deletion of Input SiteBasic \tmp\
        deleteLocalBasicOSSFiles();

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        setTestcase("BSIM_CLEANUP_SPITFIRE", "TAF - clean up server after tests have run");

        setTestStep("Deleting Nodes...");
        final BsimOperator operator = new BsimOperator();
        for (final BsimNodeData nodeData : addedNodes) {
            setTestStep("****** Deleting node " + nodeData.getNodeName() + " ******");
            operator.deleteNode(nodeData);
        }
    }

    private void deleteLocalBasicOSSFiles() {

        final BsimDeleteNodeApiOperator deleteNodeOperator = new BsimDeleteNodeApiOperator();
        deleteNodeOperator.deleteLocalBasicOSSFiles();
    }

    private void deleteSiteInstallationFile() {

        final BsimDeleteNodeApiOperator deleteNodeOperator = new BsimDeleteNodeApiOperator();
        deleteNodeOperator.deleteSiteInstallationFile();
    }
}


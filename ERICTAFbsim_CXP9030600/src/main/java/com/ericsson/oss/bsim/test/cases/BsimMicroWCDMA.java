/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
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
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckRegisteredService;

/**
 * @author efitrob
 */
public class BsimMicroWCDMA extends TorTestCaseHelper implements TestCase {

    private final ArrayList<BsimNodeData> addedNodes = new ArrayList<BsimNodeData>();

    protected static BsimRemoteCommandExecutor bsimSshRemoteCommandExecutor = BsimApiGetter.getRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    @BeforeClass
    public void prepareTheRun() {
        setTestcase("BSIM_PRECHECK_FOR_MICRO_WCDMA", "Preparation for MICRO WCDMA Test Run");
        setTestStep("Pre-check before running MICRO WCDMA test suite");
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.MICRO_WCDMA, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());
    }

    @Test(dataProvider = "newMicroWCDMANodes", dataProviderClass = BsimTestData.class, groups = { "KGB", "VCDB" })
    public void addMicroWCDMANode(
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

        addMacroNodeHelper.doDataValidation(nodeData);
        addMacroNodeHelper.setRequiredPasParameters(nodeData);
        addMacroNodeHelper.doExecution(nodeData, expectedResult);
        addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));
        addedNodes.add(nodeData);
        addMacroNodeHelper.doVerification(nodeData, expectedResult);

        if (nodeData.getAifData().isAutoIntegrate()) {
            addMacroNodeHelper.bindMicroWcdmaNode(nodeData);
        }

        addMacroNodeHelper.doNetsimSynchronization(nodeData);

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {

        setTestcase("BSIM_CLEANUP_MICRO_WCDMA", "TAF - clean up server after tests have run");

        final BsimOperator operator = new BsimOperator();
        final BsimAddMacroNodeHelper addMacroNodeHelper = new BsimAddMacroNodeHelper();

        setTestStep("Deleting Nodes...");
        operator.doCleanUp(addedNodes);
        setTestStep("Clean up netsim");
        addMacroNodeHelper.cleanUpNetsim();
    }

}

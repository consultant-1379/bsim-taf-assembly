package com.ericsson.oss.bsim.test.cases;

import java.util.ArrayList;

import org.junit.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimOperator;
import com.ericsson.oss.bsim.operators.api.BsimDeleteNodeApiOperator;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckLocalTempDir;
import com.ericsson.oss.bsim.test.data.BsimTestData;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

public class BsimDG2 extends TorTestCaseHelper implements TestCase {

    private final ArrayList<BsimNodeData> addedNodes = new ArrayList<BsimNodeData>();

    @BeforeClass
    public void prepareTheRun() {

        setTestcase("BSIM_PRECHECK_FOR_DG2", "Preparation for DG2 Test Run");
        setTestStep("Pre-check before running DG2 test suite.");
        assertTrue(new PreCheckLocalTempDir().createTempDir());
        // assertTrue(new PreCheckUpgradePackages().checkUpgradePackages());

        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.DG2, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());

    }

    @Test(dataProvider = "newDG2Nodes", dataProviderClass = BsimTestData.class, groups = { "KGB", "VCDB" })
    public void addDG2Node(
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

        // Preparing DG2 Local Files
        addMacroNodeHelper.prepareLocalDG2Files(nodeData);

        // Execution of adding node
        addMacroNodeHelper.doExecution(nodeData, expectedResult);
        addMacroNodeHelper.executeAddNodeCommandOnBsimServer(String.valueOf(1));

        // add node to deleteNode list
        addedNodes.add(nodeData);

        // No HW Bind of node

        if (nodeData.getAifData().getIsNoHardwareBind()) {
            addMacroNodeHelper.executeNoHardwareBindDG2(nodeData);
        }

        if (nodeData.isEndToEnd()) {
            addMacroNodeHelper.doNetsimSynchronization(nodeData);
        }
        // Deletion of SiteInstallation File
        if (nodeData.getAifData().getIsNoHardwareBind()) {
            setTestStep("******  Deleting SiteInstallation File after No HW Bind" + nodeData.getNodeName() + "******************");
            deleteSiteInstallationFile();
        }

        // Manual Bind of RadioNode
        if (nodeData.getAifData().isManualBind()) {
            addMacroNodeHelper.bindRadioNode(nodeData);
        }

        // Deletion of SiteInstallation File
        if (nodeData.getAifData().isManualBind()) {
            setTestStep("******  Deleting SiteInstallation File after Manual Bind" + nodeData.getNodeName() + "******************");
            deleteSiteInstallationFile();
        }

        // Verification

        if (!nodeData.isEndToEnd()) {
            addMacroNodeHelper.doVerification(nodeData, expectedResult);
        }

        // Deletion of Input SiteBasic & SiteEquipment from \tmp\
        deleteInputBasicnEquipmentnOSSFiles();

    }

    @AfterClass(alwaysRun = true)
    public void cleanUp() {
        setTestcase("BSIM_CLEANUP_RADIO_NODE", "TAF - clean up server after tests have run");

        final BsimOperator operator = new BsimOperator();
        for (final BsimNodeData nodeData : addedNodes) {
            setTestStep("****** Deleting node " + nodeData.getNodeName() + " ******");
            operator.deleteNode(nodeData);

        }
    }

    private void deleteInputBasicnEquipmentnOSSFiles() {

        final BsimDeleteNodeApiOperator deleteNodeOperator = new BsimDeleteNodeApiOperator();
        deleteNodeOperator.deleteLocalBasicnEquipmentnOSSFiles();
    }

    private void deleteSiteInstallationFile() {

        final BsimDeleteNodeApiOperator deleteNodeOperator = new BsimDeleteNodeApiOperator();
        deleteNodeOperator.deleteSiteInstallationFile();
    }
}

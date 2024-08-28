package com.ericsson.oss.bsim.robustness;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.robustness.precheck.IBsimPreChecker;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckAiwsIsReady;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckBSIMUserinDHCPServer;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckBsimJksFile;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckConfiguredPicoNodesInOSSRC;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckCoreNetSize;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckDefaultNetworkAccessAssigned;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckFTPServices;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckLacAndSac;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckLocalTempDir;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckMCOnline;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckNetconfJksFile;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckNonDeliveredTemplates;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckOsgiBundles;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckRbsGroupExists;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckSecurityRoleAssigned;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckSite;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckSubNetwork;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckSyncedRNC;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckNewBsimJksFile;

public class BsimPreCheckManager {

    BsimTestCaseReportHelper reportHelper;

    NodeType nodeType;

    List<IBsimPreChecker> preCheckList = new ArrayList<IBsimPreChecker>();

    public BsimPreCheckManager(final NodeType nodeType, final BsimTestCaseReportHelper reportHelper) {

        this.reportHelper = reportHelper;
        this.nodeType = nodeType;

        if (nodeType.equals(NodeType.PICO_WCDMA) || nodeType.equals(NodeType.PICO_LTE) || nodeType.equals(NodeType.MSRAN)) {
            preCheckList.add(new PreCheckLocalTempDir());
            preCheckList.add(new PreCheckSecurityRoleAssigned());
            preCheckList.add(new PreCheckConfiguredPicoNodesInOSSRC());
            preCheckList.add(new PreCheckCoreNetSize());
            //preCheckList.add(new PreCheckAiwsIsReady());
            preCheckList.add(new PreCheckNetconfJksFile());
            preCheckList.add(new PreCheckBsimJksFile());
            if (nodeType.equals(NodeType.PICO_WCDMA) || nodeType.equals(NodeType.MSRAN)) {
                preCheckList.add(new PreCheckLacAndSac());
                preCheckList.add(new PreCheckSyncedRNC());
                preCheckList.add(new PreCheckRbsGroupExists());
                preCheckList.add(new PreCheckDefaultNetworkAccessAssigned());
            }
        }

        else if (nodeType.equals(NodeType.DG2) || nodeType.equals(NodeType.BasebandT)) {
            preCheckList.add(new PreCheckLocalTempDir());
            preCheckList.add(new PreCheckFTPServices());
            preCheckList.add(new PreCheckOsgiBundles());
            preCheckList.add(new PreCheckBsimJksFile());
        }

        else if (nodeType.equals(NodeType.LTE) || nodeType.equals(NodeType.MICRO_LTE) || nodeType.equals(NodeType.WCDMA)
                || nodeType.equals(NodeType.MICRO_WCDMA) || nodeType.equals(NodeType.MICRO_MACRO_LTE)) {
            preCheckList.add(new PreCheckLocalTempDir());
            preCheckList.add(new PreCheckMCOnline());
            preCheckList.add(new PreCheckSecurityRoleAssigned());
            preCheckList.add(new PreCheckDefaultNetworkAccessAssigned());
            preCheckList.add(new PreCheckOsgiBundles());
            preCheckList.add(new PreCheckNonDeliveredTemplates());
            preCheckList.add(new PreCheckBSIMUserinDHCPServer());
            preCheckList.add(new PreCheckFTPServices());
            preCheckList.add(new PreCheckSite());

            if (nodeType.equals(NodeType.WCDMA) || nodeType.equals(NodeType.MICRO_WCDMA)) {
                preCheckList.add(new PreCheckLacAndSac());
                preCheckList.add(new PreCheckSyncedRNC());
            } else if (nodeType.equals(NodeType.LTE)) {
                preCheckList.add(new PreCheckBsimJksFile());
            }
            if (nodeType.equals(NodeType.LTE) || nodeType.equals(NodeType.MICRO_LTE) || nodeType.equals(NodeType.MICRO_MACRO_LTE)) {
                preCheckList.add(new PreCheckSubNetwork());
            }
            // Temporary Fix for SCS Exception in CDB, Needs to be removed once security gives a permanent fix
            // if (nodeType.equals(NodeType.MICRO_LTE)) {
            // preCheckList.add(new PreCheckRestartCadm());
            // }
        }else if (nodeType.equals(NodeType.Setup)) {
            preCheckList.add(new PreCheckNewBsimJksFile());
        }
    }

    public boolean doAllPreChecks() {

        boolean isValidated = true;
        for (final IBsimPreChecker preChecker : preCheckList) {
            reportHelper.setTestStepForTestCaseOutput(preChecker.getCheckDescription());
            if (!preChecker.doPreCheck(nodeType)) {
                isValidated = false;
            }
        }
        return isValidated;
    }
}

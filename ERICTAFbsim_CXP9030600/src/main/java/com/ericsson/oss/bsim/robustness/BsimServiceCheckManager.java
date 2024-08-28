/*------------------------------------------------------------------------
 *
 *
 *      COPYRIGHT (C)                   ERICSSON RADIO SYSTEMS AB, Sweden
 *
 *      The  copyright  to  the document(s) herein  is  the property of
 *      Ericsson Radio Systems AB, Sweden.
 *
 *      The document(s) may be used  and/or copied only with the written
 *      permission from Ericsson Radio Systems AB  or in accordance with
 *      the terms  and conditions  stipulated in the  agreement/contract
 *      under which the document(s) have been supplied.
 *
 *------------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.robustness;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.robustness.precheck.IBsimPreChecker;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckOsgiBundles;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckRegisteredService;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

public class BsimServiceCheckManager {
    BsimTestCaseReportHelper reportHelper;

    List<IBsimPreChecker> preCheckList = new ArrayList<IBsimPreChecker>();

    public BsimServiceCheckManager(final BsimTestCaseReportHelper reportHelper) {

        this.reportHelper = reportHelper;

        preCheckList.add(new PreCheckOsgiBundles());
        preCheckList.add(new PreCheckRegisteredService());

    }

    public boolean doAllPreChecks(final NodeType nodeType) {

        boolean isValidated = true;

        for (final IBsimPreChecker preChecker : preCheckList) {
            reportHelper.setTestStepForTestCaseOutput(preChecker
                    .getCheckDescription());
            if (!preChecker.doPreCheck(nodeType)) {
                isValidated = false;
            }
        }
        return isValidated;
    }
}

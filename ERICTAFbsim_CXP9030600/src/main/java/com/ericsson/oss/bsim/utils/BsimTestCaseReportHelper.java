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
package com.ericsson.oss.bsim.utils;

import com.ericsson.cifwk.taf.TorTestCaseHelper;

public class BsimTestCaseReportHelper {
	TorTestCaseHelper helper;

	public BsimTestCaseReportHelper(final TorTestCaseHelper testCaseHelper) {
		helper = testCaseHelper;
	}

	public void setTestStepForTestCaseOutput(final String testStep) {
		helper.setTestStep(testStep);
	}
}

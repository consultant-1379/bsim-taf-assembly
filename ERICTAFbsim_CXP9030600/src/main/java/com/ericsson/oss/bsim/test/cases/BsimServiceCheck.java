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
package com.ericsson.oss.bsim.test.cases;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.oss.bsim.robustness.BsimServiceCheckManager;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;

public class BsimServiceCheck extends TorTestCaseHelper implements TestCase {
	@Test
	public void checkBsimService() {
		setTestcase("ID_TBD",
				"Check required bundles and registered service for new installed package.");

		final BsimServiceCheckManager serviceCheckManager = new BsimServiceCheckManager(
				new BsimTestCaseReportHelper(this));
		Assert.assertEquals(serviceCheckManager.doAllPreChecks(null), true);
	}
}

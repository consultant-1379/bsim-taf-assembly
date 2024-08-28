/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2016 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.cases;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.robustness.BsimPreCheckManager;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckLocalTempDir;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckUpgradePackages;
import com.ericsson.oss.bsim.utils.BsimTestCaseReportHelper;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckAllUpgradePackages;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckInstallLicense;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckBasicPackages;

/**
 * @author xriskas
 *         Test class created for prerequisite setup on server
 */
public class BsimSetup extends TorTestCaseHelper implements TestCase {

    /**
     * 
     */
    
    @Test
    public void JksSetup() {
        setTestcase("ID_TBD", " JKS Setup on server");
        // TODO Auto-generated constructor stub (Dec 28, 2016:1:48:23 PM by xriskas)
        final BsimPreCheckManager preCheckManager = new BsimPreCheckManager(NodeType.Setup, new BsimTestCaseReportHelper(this));
        Assert.assertEquals(true, preCheckManager.doAllPreChecks());
    }
   
   @Test
    public void upgradePackageSetup() {
        setTestcase("ID_TBD", "Upgrade package import");
        // TODO Auto-generated constructor stub (Dec 28, 2016:1:48:23 PM by xriskas)
        assertTrue(new PreCheckLocalTempDir().createTempDir());
        assertTrue(new PreCheckAllUpgradePackages().checkUpgradePackages());
    }
   
   @Test
    public void BsimInstallLicense() {
        setTestcase("ID_TBD", "Install License import");
        assertTrue(new PreCheckLocalTempDir().createTempDir());
        assertTrue(new PreCheckInstallLicense().checkInstallLicenses());
    }
   
    @Test
    public void BsimBasicPackages() {
        setTestcase("ID_TBD", "Basic Packages import");
	assertTrue(new PreCheckLocalTempDir().createTempDir());
        assertTrue(new PreCheckBasicPackages().checkBasicPackages());
    }
}

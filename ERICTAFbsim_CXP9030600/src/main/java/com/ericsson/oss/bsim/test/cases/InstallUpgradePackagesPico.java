/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.cases;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.oss.bsim.robustness.precheck.PreCheckMCOnline;

/**
 * @author xsriset
 */
public class InstallUpgradePackagesPico extends TorTestCaseHelper implements TestCase {

    private final Logger logger = Logger.getLogger(InstallUpgradePackagesPico.class);

    @BeforeClass
    public void prepareTheRun() {

        setTestcase("BSIM_INSTALL_UPGRADE_PACKAGES_FOR_PICO", "Install Upgrade Packages Required for Pico LRAN and WRAN");

        logger.info("Checking if MC is online before attempting to install the Upgrade Packages..");
        Assert.assertEquals(new PreCheckMCOnline().checkMCOnline(), true);

    }

    @Test
    public void installUpgradePackages() {

        logger.info("Installing the Upgrade Pacakges..");
        // assertTrue(new PreCheckUpgradePackages().checkUpgradePackages());

    }

}

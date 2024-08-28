/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.robustness.precheck;

import java.io.File;

import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.NodeType;

/**
 * @author xsonaro
 */
public class PreCheckDG2LocalFiles implements IBsimPreChecker {

    private static Logger log = Logger.getLogger(PreCheckDG2LocalFiles.class);

    @Override
    public boolean doPreCheck(final NodeType nodeType) {
        return createTempLocal();
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.robustness.precheck.IBsimPreChecker#getCheckDescription()
     */
    @Override
    public String getCheckDescription() {
        // TODO Auto-generated method stub (Oct 7, 2015:9:29:18 AM by xsonaro)
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.ericsson.oss.bsim.robustness.precheck.IBsimPreChecker#doPreCheck()
     */
    @Override
    public void doPreCheck() {
        // TODO Auto-generated method stub (Oct 7, 2015:9:29:18 AM by xsonaro)

    }

    private boolean createTempLocal() {
        try {
            boolean isBasic;
            boolean isEquipment;
            final File basicFile = new File("/tmp/SiteBasic.xml");
            isBasic = basicFile.createNewFile();
            if (isBasic) {
                log.info("Basic File is successfully created");
                log.info("Basic File path: " + basicFile.getAbsolutePath());

            } else {
                log.info("Basic File is not created");
            }
            final File equipFile = new File("/tmp/SiteEquipment.xml");
            isEquipment = equipFile.createNewFile();
            if (isEquipment) {
                log.info("Site Equipment File is successfully created");
                log.info("Site Equipment File path: " + equipFile.getAbsolutePath());

            } else {
                log.info("Equipment File is not created");
            }

            if (isBasic && isEquipment) {
                log.info(" Local Files Created " + basicFile.getAbsolutePath() + " " + equipFile.getAbsolutePath());
                return true;
            } else {
                log.info("Unable to create Local Files");
                return false;
            }
        } catch (final Exception e) {
            log.info("Exception occurs");
        }
        return false;

    }
}

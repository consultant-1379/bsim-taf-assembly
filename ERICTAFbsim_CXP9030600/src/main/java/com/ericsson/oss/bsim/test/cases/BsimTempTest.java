
package com.ericsson.oss.bsim.test.cases;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;

import com.ericsson.cifwk.taf.TestCase;
import com.ericsson.cifwk.taf.TorTestCaseHelper;
import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

public class BsimTempTest extends TorTestCaseHelper implements TestCase {


    private static Logger log = Logger.getLogger(BsimTempTest.class);

    //    public static final String DATA_FILE = "BSIM_WCDMA_END_TO_END.csv";

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static String SEG_MASTER_SERVICE = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS";

    private static String CHECK_LAST_UTRANCELL = " lt UtranCell | tail -1";

    @Test
    public void checkBsimService() throws IOException {

        final String lastUtranCell = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + CHECK_LAST_UTRANCELL).trim();

        log.info("lastUtranCell: "+lastUtranCell);

        final String myCommand = SEG_MASTER_SERVICE+" la "+lastUtranCell+" cId";

        final String lastUtranCellscId = ossMasterCLICommandHelper.simpleExec(myCommand);

        log.info("lastUtranCellscId: "+lastUtranCellscId);

        final String[] cId = lastUtranCellscId.split(":");

        final int cIdAsInt = Integer.parseInt(cId[1].trim());

        log.info("The cId value is " + cIdAsInt);

        final int returnValue = cIdAsInt+1;

        log.info(returnValue);


    }
}
package com.ericsson.oss.bsim.operators;

import com.ericsson.cifwk.taf.tools.http.HttpResponse;

public interface ImportAndUpgradePackageOperator {

    HttpResponse importvalidupgradepackage(String filename, String hiddenDescription);
}

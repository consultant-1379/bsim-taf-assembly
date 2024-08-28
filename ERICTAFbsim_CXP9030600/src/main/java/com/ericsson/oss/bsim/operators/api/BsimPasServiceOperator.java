package com.ericsson.oss.bsim.operators.api;

import com.ericsson.oss.bsim.getters.api.BsimApiGetter;
import com.ericsson.oss.bsim.getters.api.BsimRemoteCommandExecutor;

public class BsimPasServiceOperator {
    
    public static final String AIF_SERVER_PAS_PACKAGE = "com.ericsson.oss.aif.server";
    public static final String BSIM_SERVER_PAS_PACKAGE = "com.ericsson.oss.bsim.server";
    public static final String BULKCM_TENG_PAS_PACKAGE = "com.ericsson.oss.teng.bulkcm";    
    
    BsimRemoteCommandExecutor bsimCommmandExecutorMasterServiceSysAdm = new BsimRemoteCommandExecutor(BsimApiGetter.getHostMaster());

    /**
     * update a pas parameter on master server host by executing a pas set command
     * @param packageName
     * @param pasParamName
     * @param pasParamValue
     * @return
     */
    public boolean setPasParameterOnMasterServer(final String packageName, final String pasParamName, final String pasParamValue) {
        boolean operationSucessful = false;
        
        final String setPasCommand = String.format("/opt/ericsson/nms_cif_pas/bin/pastool -set %1$s %2$s %3$s", packageName, pasParamName, pasParamValue);
        
        bsimCommmandExecutorMasterServiceSysAdm.simpleExec(setPasCommand);
        
        return operationSucessful;
    }
    
}

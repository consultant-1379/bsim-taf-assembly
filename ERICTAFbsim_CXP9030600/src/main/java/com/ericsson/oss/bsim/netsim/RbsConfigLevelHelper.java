package com.ericsson.oss.bsim.netsim;

import java.io.File;
import java.io.IOException;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.utils.file.LocalTempFileManager;

public class RbsConfigLevelHelper {

    // default level to start integration LTE is 5 (SITE_CONFIG_COMPLETE)
    private static final String lteRbsConfigLevelScript = "SET \n" + "( \n" + "    mo \"ManagedElement=1,NodeManagementFunction=1,RbsConfiguration=1\" \n"
            + "    exception none \n" + "    nrOfAttributes 1 \n" + "   \"rbsConfigLevel\" Integer %1$s \n" + ")";

    // default level to start integration WCDMA is 4
    private static final String wcdmaRbsConfigLevelScript = "SET \n" + "(\n" + "    mo \"ManagedElement=1,NodeBFunction=1,RbsConfiguration=1\"\n"
            + "    exception none\n" + "    nrOfAttributes 1\n" + "    \"rbsConfigLevel\" Integer %1$s\n" + ")";

    // default level to start integration MICRO_WCDMA is 13 (SITE_CONFIG_COMPLETE)
    private static final String microWcdmaRbsConfigLevelScript = "SET \n" + "(\n" + "    mo \"ManagedElement=1,NodeBFunction=1,RbsConfiguration=1\"\n"
            + "    exception none\n" + "    nrOfAttributes 1\n" + "    \"rbsConfigLevel\" Integer %1$s\n" + ")";

    /*
     * private static final String DG2ConfigLevelScript = "SET \n" + "(\n" +
     * "    mo \"ManagedElement=1,NodeSupport=1,AutoProvisioning=1\"\n"
     * + "    exception none\n" + "    nrOfAttributes 1\n" + "    \"rbsConfigLevel\" Integer %1$s\n" + ")";
     */

    private String getRbsConfigLevelScriptContent(final int rbsConfigLevelToSet, final NodeType nodeType, final BsimNodeData nodeData) {

        switch (nodeType) {
            case LTE:
                return String.format(lteRbsConfigLevelScript, rbsConfigLevelToSet);
            case MICRO_LTE:
                return String.format(lteRbsConfigLevelScript, rbsConfigLevelToSet);
            case MICRO_MACRO_LTE:
                return String.format(lteRbsConfigLevelScript, rbsConfigLevelToSet);
            case WCDMA:
                return String.format(wcdmaRbsConfigLevelScript, rbsConfigLevelToSet);
            case MICRO_WCDMA:
                return String.format(microWcdmaRbsConfigLevelScript, rbsConfigLevelToSet);
            case DG2:
            case BasebandT:
                final String DG2ConfigLevelScript = "SET \n" + "(\n" + "    mo \"ManagedElement=" + nodeData.getNodeName()
                        + ",NodeSupport=1,AutoProvisioning=1\"\n" + "    exception none\n" + "    nrOfAttributes 1\n" + "    \"rbsConfigLevel\" Integer %1$s\n"
                        + ")";
                return String.format(DG2ConfigLevelScript, rbsConfigLevelToSet);
            default:
                return null;
        }

    }

    /**
     * Write an RbsConfigLevel netsim Kertayle script to set the RbsConfigLevel of a node to the required level
     * Use a class that implements the RbsConfigLevel interface to pass the required integer value
     * It is up to the caller of this function to delete the returned file from the server when the file is no longer required
     * 
     * @param rbsConfigLevelToSet
     * @param nodeType
     * @return
     * @throws IOException
     *         if file cannot be written for some reason (e.g. not a supported node type or permission issues writing file)
     */
    public File writeRbsConfigLevelFile(final int rbsConfigLevelToSet, final NodeType nodeType, final BsimNodeData nodeData) throws IOException {

        final LocalTempFileManager tempFileManager = new LocalTempFileManager();
        final String scriptContent = getRbsConfigLevelScriptContent(rbsConfigLevelToSet, nodeType, nodeData);
        final File rbsConfigNetsimCommandFile = tempFileManager.createTempFile("rbsConfigLevelToSet_" + rbsConfigLevelToSet, ".mo", scriptContent);

        return rbsConfigNetsimCommandFile;
    }
}


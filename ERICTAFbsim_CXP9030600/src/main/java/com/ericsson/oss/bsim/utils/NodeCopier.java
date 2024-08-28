package com.ericsson.oss.bsim.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ericsson.cifwk.taf.tools.cli.CLICommandHelper;
import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.getters.api.BsimApiGetter;

public class NodeCopier {

    private static Logger log = Logger.getLogger(NodeCopier.class);

    private static CLICommandHelper ossMasterCLICommandHelper = BsimApiGetter.getCLICommandHelper(BsimApiGetter.getHostMaster());

    private static String SEG_MASTER_SERVICE = "/opt/ericsson/nms_cif_cs/etc/unsupported/bin/cstest -s Seg_masterservice_CS";

    private static String RETRIEVE_ENBID_COMMAND = " lt ENodeBFunction -an eNBId | grep -i eNBId";

    final List<String> enbid = new ArrayList<String>();

    BsimNodeData nodeData;

    public NodeCopier(final BsimNodeData nodeData) {
        this.nodeData = nodeData;
    }

    public void createNode(final String nextIpAddress, final BsimNodeData[] newnodeData, final int i) {
        newnodeData[i] = new BsimNodeData(nodeData);

        newnodeData[i].CriticalData.setIpAddress(nextIpAddress);
        newnodeData[i].setNodeName(nodeData.getNodeType().toString() + "Node_TAF_" + System.currentTimeMillis() + "_" + i);
        newnodeData[i].CriticalData.setSite(newnodeData[i].getNodeName() + "_Site_" + i);

        newnodeData[i].setNodeFdn(String.format("SubNetwork=ONRM_ROOT_MO_R,SubNetwork=%1$s,MeContext=%2$s",
                newnodeData[i].getNodeTemplateAttrs().get("SubNetwork Group"), newnodeData[i].getNodeName()));

        newnodeData[i].getNodeTemplateAttrs().put("IP Address", newnodeData[i].CriticalData.getIpAddress());

        newnodeData[i].getNodeTemplateAttrs().put("Site", newnodeData[i].CriticalData.getSite());
        newnodeData[i].getNodeTemplateAttrs().put("Node Name", newnodeData[i].getNodeName());

        // radio template change needed for each node
        newnodeData[i].getRadioTemplateAttrs().put("tac", String.valueOf(i));
        newnodeData[i].getRadioTemplateAttrs().put("Node Name", newnodeData[i].getNodeName());

        getPossibleEnbIdFromServer();

        // transport template change needed for each node
        newnodeData[i].getTransportTemplateAttrs().put("eNB ID", generateENodeBId());
        newnodeData[i].getTransportTemplateAttrs().put("Node Name", newnodeData[i].getNodeName());

        // transport template change needed for each node
        newnodeData[i].getTransportTemplateAttrs().put("eNB ID", generateENodeBId());
        newnodeData[i].getTransportTemplateAttrs().put("Node Name", newnodeData[i].getNodeName());

        // site basic template change needed for each node
        newnodeData[i].getAifData().getSiteBasicTemplateAttrs().put("Node Name", newnodeData[i].getNodeName());

        // site equipment change needed for each node
        newnodeData[i].getAifData().getSiteEquipmentTemplateAttrs().put("Node Name", newnodeData[i].getNodeName());

        // site install change needed for each node
        newnodeData[i].getAifData().getSiteInstallationTemplateAttrs().put("Node Name", newnodeData[i].getNodeName());

    }

    public String generateENodeBId() {
        log.info("Generating ENodeB ID...");
        String output = "";
        final Random generator = new Random();
        int flag = -1;
        while (flag == -1) {
            final int random = generator.nextInt(10485);
            output = String.valueOf(random);
            if (!enbid.contains(output)) {
                flag = 0;
                enbid.add(output);
                log.info("ENodeB ID is : " + random);
            }
        }
        return output;
    }

    public void getPossibleEnbIdFromServer() {
        log.info("Getting ENodeB ID From Server...");
        final String result = ossMasterCLICommandHelper.simpleExec(SEG_MASTER_SERVICE + RETRIEVE_ENBID_COMMAND).trim();
        if (result.contains("eNBId (long -1..1048575)")) {
            final String[] rows = result.split("\n");
            for (final String row : rows) {
                enbid.add(row.split("\\:")[1].replace("\r", " ").trim());
            }
        }
    }

}
/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2014 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.test.verification;

/**
 * @author egavhug
 *
 */
import org.apache.log4j.Logger;

import com.ericsson.oss.bsim.data.model.BsimNodeData;
import com.ericsson.oss.bsim.data.model.NodeType;
import com.ericsson.oss.bsim.operators.BsimOperator;

public class DG2QrCodeVerification implements IBSIMVerification {

    private final BsimNodeData nodeData;

    private final Logger log = Logger.getLogger(QrCodeVerification.class);

    private static final String QR_CODE_LOCATION_DG2 = "/opt/ericsson/nms_umts_bsim_server/dat/node_qr_codes_dg2/";

    public DG2QrCodeVerification(final BsimNodeData nodeData) {

        this.nodeData = nodeData;
    }

    @Override
    public boolean doVerification(final BsimOperator bsimOperator) {
        int count = 0;
        final int maximumCount = 5;
        final NodeType nodeType = nodeData.getNodeType();

        do {

            try {
                if (bsimOperator.checkQRCodeExists(assignQrCodeLoactionDG2(nodeType), nodeData.getNodeName())) {

                    return true;
                }
            } catch (final Exception e1) {
                log.error(e1.getMessage());

            }
            count++;
            try {
                Thread.sleep(3000);
            } catch (final InterruptedException e) {
            }

        } while (count < maximumCount);
        log.error("QR Code Was Not Generated");
        return false;
    }

    /**
     * @param nodeType
     * @return
     * @throws Exception
     */
    private String assignQrCodeLoactionDG2(final NodeType nodeType) throws Exception {

        if (nodeData.getNodeType() == NodeType.DG2 || nodeData.getNodeType() == NodeType.BasebandT) {
            return QR_CODE_LOCATION_DG2;
        }
        throw new Exception("QR Code was not Generated for the node type " + nodeType);

    }
}

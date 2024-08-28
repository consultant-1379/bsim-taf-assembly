package com.ericsson.oss.bsim.operators;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.cifwk.taf.GenericOperator;
import com.ericsson.oss.bsim.batch.data.model.MockBsimPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.MockLRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.MockWLRANPicoBatch;
import com.ericsson.oss.bsim.batch.data.model.MockWRANPicoBatch;
import com.ericsson.oss.bsim.operators.api.BsimAddLRANPicoBatchApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimAddPicoBatchApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimAddWLRANPicoBatchApiOperator;
import com.ericsson.oss.bsim.operators.api.BsimAddWRANPicoBatchApiOperator;

public class BsimBatchOperator implements GenericOperator {

    BsimAddWRANPicoBatchApiOperator bsimAddWRANPicoBatchApiOperator = new BsimAddWRANPicoBatchApiOperator();

    BsimAddLRANPicoBatchApiOperator bsimAddLRANPicoBatchApiOperator = new BsimAddLRANPicoBatchApiOperator();

    BsimAddPicoBatchApiOperator bsimAddPicoBatchApiOperator = new BsimAddPicoBatchApiOperator();

    BsimAddWLRANPicoBatchApiOperator bsimAddWLRANPicoBatchApiOperator = new BsimAddWLRANPicoBatchApiOperator();

    // WRAN
    public String addWRANBatch(final MockWRANPicoBatch mockWRANPicoBatch) {

        return bsimAddWRANPicoBatchApiOperator.addWRANPicoBatch(mockWRANPicoBatch);
    }

    public List<String> bindWRANBatch(final MockWRANPicoBatch mockWRANPicoBatch, final int nodesToBind) {

        return bsimAddWRANPicoBatchApiOperator.executeWRANPicoBindJob(mockWRANPicoBatch.getName(), mockWRANPicoBatch.getNodeFdnValues(), nodesToBind);
    }

    public String deletebindWRANBatchNodes(final List<String> nodesToDelete) {

        return bsimAddWRANPicoBatchApiOperator.executeWRANPicoDeleteNodes(nodesToDelete);
    }

    public String deleteWRANBatch(final MockWRANPicoBatch mockWRANPicoBatch) {

        return bsimAddWRANPicoBatchApiOperator.deleteWRANPicoBatch(mockWRANPicoBatch.getName());
    }

    public Boolean checkDoesSmrsAccountExistforBatch(final MockBsimPicoBatch mockBsimPicoBatch, final Boolean expectedResult) {
        return bsimAddPicoBatchApiOperator.checkDoesSmrsAccountExistforBatch(mockBsimPicoBatch, expectedResult);
    }

    // WLRAN
    public String addWLRANBatch(final MockWLRANPicoBatch mockWLRANPicoBatch) {

        return bsimAddWLRANPicoBatchApiOperator.addWLRANPicoBatch(mockWLRANPicoBatch);
    }

    public List<String> bindWLRANBatch(final MockWLRANPicoBatch mockWLRANPicoBatch, final int nodesToBind) {

        return bsimAddWLRANPicoBatchApiOperator.executeWLRANPicoBindJob(mockWLRANPicoBatch.getName(), mockWLRANPicoBatch.getNodeFdnValues(), nodesToBind);
    }

    public String deletebindWLRANBatchNodes(final List<String> nodesToDelete) {

        return bsimAddWLRANPicoBatchApiOperator.executeWLRANPicoDeleteNodes(nodesToDelete);
    }

    public String deleteWLRANBatch(final MockWLRANPicoBatch mockWLRANPicoBatch) {

        return bsimAddWLRANPicoBatchApiOperator.deleteWLRANPicoBatch(mockWLRANPicoBatch.getName());
    }

    // LRAN
    public String addLRANBatch(final MockLRANPicoBatch mockLRANPicoBatch) {

        return bsimAddLRANPicoBatchApiOperator.addLRANPicoBatch(mockLRANPicoBatch);
    }

    public List<String> bindLRANBatch(final MockLRANPicoBatch mockLRANPicoBatch, final int nodesToBind, final ArrayList<String> hardwareID) {

        return bsimAddLRANPicoBatchApiOperator.executeLRANPicoBindJob(mockLRANPicoBatch.getName(), mockLRANPicoBatch.getNodeFdnValues(), nodesToBind,
                hardwareID);
    }

    /**
     * @param mockLRANPicoBatch
     * @param nodesToBind
     * @return
     *         Starts a NoHardware Bind for a batch.
     */
    public List<String> executeNoHardwareBind(final MockLRANPicoBatch mockLRANPicoBatch, final int nodesToBind) {

        return bsimAddLRANPicoBatchApiOperator.executeLRANPicoNoHardwareBindJob(mockLRANPicoBatch.getName(), mockLRANPicoBatch.getNodeFdnValues(), nodesToBind,
                mockLRANPicoBatch.getSiteInstallTemplateAttrs(), mockLRANPicoBatch.getSiteInstallTemplateName());
    }

    public String deletebindLRANBatchNodes(final List<String> nodesToDelete) {

        return bsimAddLRANPicoBatchApiOperator.executeLRANPicoDeleteNodes(nodesToDelete);
    }

    public boolean checkBoundNodesDeletedInCS(final List<String> boundNodes) {
        return bsimAddLRANPicoBatchApiOperator.checkBoundNodesDeletedInCS(boundNodes);
    }

    public String deleteLRANBatch(final MockLRANPicoBatch mockLRANPicoBatch) {

        return bsimAddLRANPicoBatchApiOperator.deleteLRANPicoBatch(mockLRANPicoBatch.getName());
    }

    public String moExist(final String s) {

        return bsimAddLRANPicoBatchApiOperator.moExist(s);
    }

    public boolean checkQRCodeExists(final String qRCodePICOLocation, final String qrCodeName) {
        return bsimAddPicoBatchApiOperator.checkQRCodeExists(qRCodePICOLocation, qrCodeName);
    }

    /**
     * @return
     *         Returns true if the Combined Configuration File exists on the server.
     */
    public boolean checkCCFFileExists() {
        return bsimAddLRANPicoBatchApiOperator.checkCCFFileExists();
    }

}

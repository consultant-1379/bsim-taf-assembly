package com.ericsson.oss.bsim.test.data;

import java.util.Iterator;
import org.testng.annotations.DataProvider;

import com.ericsson.cifwk.taf.TestData;
import com.ericsson.oss.bsim.data.BsimDG2DataProvider;
import com.ericsson.oss.bsim.data.BsimLRANPicoBatchDataProvider;
import com.ericsson.oss.bsim.data.BsimLTEDataProvider;
import com.ericsson.oss.bsim.data.BsimLTEStCDBDataProvider;
import com.ericsson.oss.bsim.data.BsimMacroLTEDataProvider;
import com.ericsson.oss.bsim.data.BsimMicroLTEDataProvider;
import com.ericsson.oss.bsim.data.BsimMicroMacroLTEDataProvider;
import com.ericsson.oss.bsim.data.BsimMicroMacroLTEDataProvider_KGB;
import com.ericsson.oss.bsim.data.BsimMicroWCDMADataProvider;
import com.ericsson.oss.bsim.data.BsimSpitfireDataProvider;
import com.ericsson.oss.bsim.data.BsimWCDMADataProvider;
import com.ericsson.oss.bsim.data.BsimWRANPicoBatchDataProvider;
import com.ericsson.oss.bsim.data.BsimWRANPicoBatchDataProviderE2E;
import com.ericsson.oss.bsim.data.BsimWLRANPicoBatchDataProvider;

public class BsimTestData implements TestData {

    @DataProvider(name = "newLTENodes")
    public static Iterator<Object[]> addNodeData() {

        final BsimLTEDataProvider bsimAddLTENodeData = new BsimLTEDataProvider();

        return bsimAddLTENodeData.getTestDataList().iterator();
    }

    @DataProvider(name = "newStCdbLTENodes")
    public static Iterator<Object[]> addLTENodeData() {

        final BsimLTEStCDBDataProvider bsimAddLTENodeData = new BsimLTEStCDBDataProvider();

        return bsimAddLTENodeData.getTestDataList().iterator();
    }

    @DataProvider(name = "newDG2Nodes")
    public static Iterator<Object[]> addDG2NodeData() {

        final BsimDG2DataProvider bsimAddDG2NodeData = new BsimDG2DataProvider();

        return bsimAddDG2NodeData.getTestDataList().iterator();
    }

    @DataProvider(name = "newMacroLTENodes")
    public static Iterator<Object[]> addMacroLTENodeData() {

        final BsimMacroLTEDataProvider bsimAddMacroLTENodeData = new BsimMacroLTEDataProvider();

        return bsimAddMacroLTENodeData.getTestDataList().iterator();
    }

    @DataProvider(name = "newMicroLTENodes")
    public static Iterator<Object[]> addLTEMicroNodeData() {

        final BsimMicroLTEDataProvider bsimAddMicroLTENodeData = new BsimMicroLTEDataProvider();

        return bsimAddMicroLTENodeData.getTestDataList().iterator();
    }

    @DataProvider(name = "newWCDMANodes")
    public static Iterator<Object[]> addWCDMANodeData() {

        final BsimWCDMADataProvider bsimAddWCDMANodeData = new BsimWCDMADataProvider();

        return bsimAddWCDMANodeData.getTestDataList().iterator();
    }

    @DataProvider(name = "newWRANPicoBatch")
    public static Iterator<Object[]> addWRANBatchData() {

        final BsimWRANPicoBatchDataProvider bsimWRANPicoBatchDataProvider = new BsimWRANPicoBatchDataProvider();

        return bsimWRANPicoBatchDataProvider.getTestDataList().iterator();
    }

    // ONLY FOR KGB
    @DataProvider(name = "newWRANPicoBatchE2E")
    public static Iterator<Object[]> addWRANBatchDataE2E() {

        final BsimWRANPicoBatchDataProviderE2E bsimWRANPicoBatchDataProviderE2E = new BsimWRANPicoBatchDataProviderE2E();

        return bsimWRANPicoBatchDataProviderE2E.getTestDataList().iterator();
    }
    @DataProvider(name = "newWLRANPicoBatch")
    public static Iterator<Object[]> addWLRANBatchData() {
        final BsimWLRANPicoBatchDataProvider bsimWLRANPicoBatchDataProvider = new BsimWLRANPicoBatchDataProvider();
        return bsimWLRANPicoBatchDataProvider.getTestDataList().iterator();
    
    }

    @DataProvider(name = "newLRANPicoBatch")
    public static Iterator<Object[]> addLRANBatchData() {

        final BsimLRANPicoBatchDataProvider bsimLRANPicoBatchDataProvider = new BsimLRANPicoBatchDataProvider();

        return bsimLRANPicoBatchDataProvider.getTestDataList().iterator();
    }

    @DataProvider(name = "newMicroWCDMANodes")
    public static Iterator<Object[]> addWCDMAMicroNodeData() {

        final BsimMicroWCDMADataProvider bsimAddMicroWCDMANodeData = new BsimMicroWCDMADataProvider();

        return bsimAddMicroWCDMANodeData.getTestDataList().iterator();
    }

    @DataProvider(name = "newMicroMacroLTENodes")
    public static Iterator<Object[]> addMicroMacroNodeData() {

        final BsimMicroMacroLTEDataProvider bsimAddMicroMacroLTEDataProvider = new BsimMicroMacroLTEDataProvider();
        return bsimAddMicroMacroLTEDataProvider.getTestDataList().iterator();
    }
   @DataProvider(name = "newMicroMacroLTENodesKGB")
    public static Iterator<Object[]> addMicroMacroNodeDataKGB() {

        final BsimMicroMacroLTEDataProvider_KGB bsimAddMicroMacroLTEDataProviderKGB = new BsimMicroMacroLTEDataProvider_KGB();
        return bsimAddMicroMacroLTEDataProviderKGB.getTestDataList().iterator();
    }

    @DataProvider(name = "newSpitfireNodes")
    public static Iterator<Object[]> addSpitfireNodeData() {

        final BsimSpitfireDataProvider bsimSpitfireDataProvider = new BsimSpitfireDataProvider();
        return bsimSpitfireDataProvider.getTestDataList().iterator();

    }
}




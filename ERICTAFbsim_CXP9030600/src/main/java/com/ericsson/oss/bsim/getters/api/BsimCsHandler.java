/**
 * -----------------------------------------------------------------------
 *     Copyright (C) 2015 LM Ericsson Limited.  All rights reserved.
 * -----------------------------------------------------------------------
 */
package com.ericsson.oss.bsim.getters.api;

import java.util.Iterator;
import java.util.List;

import com.ericsson.oss.taf.cshandler.CSDatabase;
import com.ericsson.oss.taf.cshandler.CSTestHandler;
import com.ericsson.oss.taf.cshandler.model.Fdn;
import com.ericsson.oss.taf.hostconfigurator.HostGroup;

/**
 * @author xsriset
 */
public class BsimCsHandler {

    private CSTestHandler csTestHandler = null;

    public BsimCsHandler(final String database) {
        if (database.equalsIgnoreCase("segment")) {
            csTestHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Segment);
        } else if (database.equalsIgnoreCase("onrm")) {
            csTestHandler = new CSTestHandler(HostGroup.getOssmaster(), CSDatabase.Onrm);
        }
    }

    /**
     * @param myCmd
     * @return
     */
    public String getFdnByType(final String myCmd) {
        String output = null;
        final List<Fdn> listOfNodes = csTestHandler.getByType(myCmd);
        final Iterator it = listOfNodes.iterator();
        while (it.hasNext()) {
            output = (String) it.next();
            break;
        }
        return output;

    }

    public String getListByType(final String myCmd) {
        return csTestHandler.getByType(myCmd).toString();

    }

    /**
     * @param fdn
     * @param attribute
     * @return
     */
    public String getAttributesForFdn(final Fdn fdn, final String attribute) {
        return csTestHandler.getAttributes(fdn, attribute).toString();
    }

    private String getNodeName(final String nodeFdn) {
        final String nodeName = nodeFdn.substring(nodeFdn.trim().lastIndexOf("=") + 1, nodeFdn.length());
        return nodeName;
    }

}

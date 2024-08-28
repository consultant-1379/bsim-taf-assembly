import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ericsson.oss.bsim.ui.core.BsimServiceManager;

class BsimDeleteNodeOperator{
    
    public String deleteCreatedNode(final String nodeType, final String nodeFdn) {

        boolean isDeleted = false;
        StringBuilder sb = new StringBuilder("\r\n");
        try {
            String nodeName = null;
            String subNetworkName = null;
            final Pattern pattern = Pattern.compile(",SubNetwork=(.*?),MeContext=(.*)");
            final Matcher m = pattern.matcher(nodeFdn);
            if (m.find()) {
                subNetworkName = m.group(1);
                nodeName = m.group(2);                
            }
        
            // for wcdma, the format for deletion is "RNC/NodeName"
            if ("wcdma".equalsIgnoreCase(nodeType)) {
                nodeName = subNetworkName + "/" + nodeName;
            }
                             
            try {
                // try to delete using node fdn
                final List<String> tmpNodeList = new ArrayList<String>();
                tmpNodeList.add(nodeFdn);
                sb.append("Delete node using Node Fdn: " + nodeFdn + "\r\n");
                BsimServiceManager.getInstance().getBsimService().deleteNetworkElement(tmpNodeList);                                                              
            }
            catch (final Exception ex1) {
                // handle exception first
                sb.append("Delete node failed using Node Fdn: " + ex1.getMessage() + "\r\n");
            }
            
            try {                
                // before O14, bsim code deletes node using node name, rather than node fdn
                if(!isDeleted){
                    // try to delete using node name
                    final List<String> tmpNodeList = new ArrayList<String>();
                    tmpNodeList.add(nodeName);
                    sb.append("Delete node using Node Name: " + nodeName + "\r\n");
                    BsimServiceManager.getInstance().getBsimService().deleteNetworkElement(tmpNodeList);                                       
                }
            }
            catch (final Exception ex2) {
                // handle exception
                sb.append("Delete node failed using Node Name: " + ex2.getMessage() + "\r\n");
            }
            
            // sleep for a while to wait the execution finished
            Thread.sleep(2000);
        }
        catch (final Exception exp) {
            sb.append("Delete node failed: " + exp.getMessage() + "\r\n");
        }
                    
        return sb.toString();
    }

    public String deleteCreatedNode_O14(final String nodeFdn) {
        
        // since O14.0.8, BSIM service delete node using node fdn
        
        boolean isDeleted = false;
        StringBuilder sb = new StringBuilder("\r\n");
        try {
            // try to delete using node fdn
            final List<String> tmpNodeList = new ArrayList<String>();
            tmpNodeList.add(nodeFdn);
            sb.append("Delete node using Node Fdn: " + nodeFdn + "\r\n");
            BsimServiceManager.getInstance().getBsimService().deleteNetworkElement(tmpNodeList);
            
            sb.append("Delete node successfully using Node Fdn: " + nodeFdn + "\r\n");
            isDeleted = true;
        }
        catch (final Exception ex) {
            // handle exception
            sb.append("Delete node failed using Node Fdn: " + ex.getMessage() + "\r\n");
        }
        
        if(isDeleted){
            return "Node deleted successfully.";
        }else{
            return sb.toString();
        }
    }
}
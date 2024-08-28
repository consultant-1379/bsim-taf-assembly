
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import com.ericsson.oss.utilities.templates.service.ITemplatesService;
import com.ericsson.oss.utilities.templates.domain.Template;
import org.springframework.beans.factory.FactoryBean;
import com.ericsson.oss.bsim.ui.core.BsimServiceManager;
import com.ericsson.oss.bsim.domain.TemplateTypes;
import com.ericsson.oss.bsim.domain.NodeType;
import com.ericsson.oss.bsim.domain.NetworkType;
import com.ericsson.oss.bsim.ui.constants.FtpTypes;
import com.ericsson.oss.common.cm.service.CMException;
import com.ericsson.oss.common.cm.service.ICMService;
import com.ericsson.oss.domain.modetails.IManagedObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.lang.System;
import java.util.Collection;
import java.util.Iterator;

class MoDetailsRetriever {
        
        private ICMService cmService;
                
        MoDetailsRetriever(){
                final String templatesServiceUrl = "rmi://masterservice:50042/CCMService";
                cmService= (ICMService) getRmiService(templatesServiceUrl, ICMService.class);
        }
        
        public def getAvailableSiteName(){
           final String[] strings = [ "Site" ];
           final Collection<IManagedObject> siteMos = cmService.findManagedObjects("SubNetwork=ONRM_ROOT_MO_R",strings , null, null);
           String result = siteMos.isEmpty() ? "" : siteMos.iterator().next().getFdn().toString();
           final String site = result.substring(result.lastIndexOf(',') + 1);
           return site.substring(site.lastIndexOf('=') + 1);;
        }
        
        public def getAvailableSiteFdn(){
                final String[] strings = [ "Site" ];
                final Collection<IManagedObject> siteMos = cmService.findManagedObjects("SubNetwork=ONRM_ROOT_MO_R",strings , null, null);
                
                return siteMos.iterator().next().getFdn().toString();
         }
        
        public def getFirstAvailableRnc() {
                
                                final String[] strings = [ "SubNetwork" ];
                                final Collection<IManagedObject> rncMos = cmService.findManagedObjects("SubNetwork=ONRM_ROOT_MO_R",strings , null, null);
                
                                final java.util.Iterator<IManagedObject> itr = rncMos.iterator();
                                while (itr.hasNext()) {
                                        final Object element = itr.next();
                                        if (!element.toString().contains("RNC")) {
                                                itr.remove();
                                        }
                                }
                
                                final String result = rncMos.isEmpty() ? "" : rncMos.iterator().next().getFdn().toString();
                                final String rnc = result.substring(result.lastIndexOf(',') + 1);
                                return rnc.substring(rnc.lastIndexOf('=') + 1);
                
                        }
        
        // does not work properly
        public String moExists(final String fdn) {

                String exists = "true";
                try {
                        cmService.findManagedObject(fdn);
                } catch (final Exception e) {
                        exists = "false" + e.getMessage();;
                }
                return exists;
        }
        
        private static Object getRmiService(final String url, final Class clazz) {
                final RmiProxyFactoryBean plannedManagementRmiFactory = new RmiProxyFactoryBean();
                plannedManagementRmiFactory.setServiceInterface(clazz);
                plannedManagementRmiFactory.setServiceUrl(url);
                plannedManagementRmiFactory.setRefreshStubOnConnectFailure(true);
                plannedManagementRmiFactory.afterPropertiesSet();
                
                return ((FactoryBean) plannedManagementRmiFactory).getObject();
        }
}
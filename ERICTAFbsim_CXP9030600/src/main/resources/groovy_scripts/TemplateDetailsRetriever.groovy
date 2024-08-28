
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
import com.ericsson.oss.utilities.templates.domain.Template;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.lang.System;
import java.util.Collection;
import java.util.Iterator;

class TemplateDetailsRetriever {
	
	private ITemplatesService templatesService;
		
	TemplateDetailsRetriever(){
		final String templatesServiceUrl = "rmi://masterservice:50042/templatesService";
		templatesService= (ITemplatesService) getRmiService(templatesServiceUrl, ITemplatesService.class);
	}
	
	public def getAllTemplates() {
		List<Template> templates = templatesService.findAllTemplates();		
	}
	
	public def getAllTemplateNames() {		
		List<Template> templates = templatesService.findAllTemplates();
		
		final List<String> templatesName = new ArrayList<String>();
		for (final Template t : templates) {
			templatesName.add(t.getName());
		}
		
		return templatesName;
	}
	
	public String retrieveTemplateByName(final String TemplateName) {
		return templatesService.findTemplateByName(TemplateName).getName();
		
	}
	
	public List<String> retrieveTemplateSubstitutionsByName(final String TemplateName) {
		final List<String> subs = templatesService.getSupportedSubstitutions(TemplateName);
		return subs;		
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
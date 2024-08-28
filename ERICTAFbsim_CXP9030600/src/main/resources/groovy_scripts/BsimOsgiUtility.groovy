import com.ericsson.oss.bsim.ui.core.BsimServiceManager;
import com.ericsson.oss.bsim.ui.service.BsimService;
import com.ericsson.oss.bsim.domain.TemplateTypes;
import com.ericsson.oss.bsim.domain.NodeType;
import com.ericsson.oss.bsim.domain.GroupType;
import com.ericsson.oss.bsim.domain.NetworkType;
import com.ericsson.oss.bsim.ui.constants.FtpTypes;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.beans.factory.FactoryBean;
import com.ericsson.oss.utilities.templates.domain.Template;
import com.ericsson.oss.utilities.templates.service.ITemplatesService;
import com.ericsson.oss.utilities.profiles.domain.Profile;
import com.ericsson.oss.common.cm.service.CMException;
import com.ericsson.oss.common.cm.service.ICMService;
import com.ericsson.oss.domain.modetails.IManagedObject;
import com.ericsson.oss.bsim.ui.service.ProfileTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;
import java.lang.System;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;

class BsimOsgiUtility{
	final BsimService bsimService = BsimServiceManager.getInstance().getBsimService();
    //final BsimServiceManager BsimServiceManagerInstance = BsimServiceManager.getInstance();
	
	public def getMimversions(){
		return bsimService.getMimVersions(NodeType.ERbs).toString();
	}
	
//	public def getLteSubnetworks(){
//	    bsimService.loadSubNetworkGroups();
//	    final NetworkType[] networkTypes = [ NetworkType.LTE ];
//	    final String[] subNetworks = bsimService.getSubNetworkGroups(networkTypes);
//	    final int idx = new Random().nextInt(subNetworkStrArr.length);
//	    System.out.println("subNetworkStrArr[idx] = " + subNetworkStrArr[idx]);
//	    System.out.println("idx = " + idx);
//	    return subNetworkStrArr[idx];
//	}
//
//	public def getLteMimVersion(){
//	    final String[] mimVersion = bsimService.getMimVersions(NodeType.ERbs);
//	    final TreeSet mimVersionTreeSet = new TreeSet();
//	    if (mimVersion.length > 0) {
//	        for (int i=0; i < mimVersion.length-1; i++) { 
//	            mimVersionTreeSet.add(mimVersion[i]);          
//	            } 	    
//	    } else {
//	        System.err.println("ERROR : getLteMimVersion - empty String Array mimVersion");                  
//	        // exit(); ??
//	    }
//	    return mimVersionTreeSet.last().toString();
//	            }


	public def getFtpConfig(){
		final String[] ftpServiceTypes = [ FtpTypes.FTP_TYPE_AUTO_INTEGRATION, FtpTypes.FTP_TYPE_SW_STORE ];
		final NetworkType[] networkTypes = [ NetworkType.LTE ];
		bsimService.loadFtpServices(ftpServiceTypes);
		final String[] ftpAutoIntegration = bsimService.getFtpServices(FtpTypes.FTP_TYPE_AUTO_INTEGRATION,networkTypes);
		final String[] ftpSwStore = bsimService.getFtpServices(FtpTypes.FTP_TYPE_SW_STORE, networkTypes);
		return ftpAutoIntegration[0];
	}

	public def getSubnetworks(){
		bsimService.loadSubNetworkGroups();
		final NetworkType[] networkTypes = [ NetworkType.LTE ];
		return bsimService.getSubNetworkGroups(networkTypes).toString();
	}
	
	public def getFirstSubnetworkByNetworkType(String networkType){
		bsimService.loadSubNetworkGroups();
		final NetworkType[] networkTypes;
		if(networkType.equalsIgnoreCase(NetworkType.LTE.asAttribute())){
			networkTypes = [ NetworkType.LTE ];
		}
		if(networkType.equalsIgnoreCase(NetworkType.WCDMA.asAttribute())){
			networkTypes = [ NetworkType.WCDMA ];
		}
		if(networkType.equalsIgnoreCase(NetworkType.GERAN.asAttribute())){
			networkTypes = [ NetworkType.GERAN ];
		}
		if(networkType.equalsIgnoreCase(NetworkType.CORE.asAttribute())){
			networkTypes = [ NetworkType.CORE ];
		}
		return bsimService.getSubNetworkGroups(networkTypes)[0];

	}
	
	public def getFtpServiceByType(String type){
		final String[] ftpServiceTypes = [ FtpTypes.FTP_TYPE_AUTO_INTEGRATION, FtpTypes.FTP_TYPE_SW_STORE, FtpTypes.FTP_TYPE_BACKUP_STORE, FtpTypes.FTP_TYPE_LICENSEKEY_STORE ];
		final NetworkType[] networkTypes = [ NetworkType.LTE ];
		bsimService.loadFtpServices(ftpServiceTypes);
		final String[] ftpService = bsimService.getFtpServices(type,networkTypes);
		return ftpService[0];
	}
	
	public def getFirstFtpServiceByFtpAndNetworkType(String ftpType, String networkType){
		final String[] ftpServiceTypes = [ FtpTypes.FTP_TYPE_AUTO_INTEGRATION, FtpTypes.FTP_TYPE_SW_STORE, FtpTypes.FTP_TYPE_BACKUP_STORE, FtpTypes.FTP_TYPE_LICENSEKEY_STORE ];
		final NetworkType[] networkTypes;
		if(networkType.equalsIgnoreCase(NetworkType.LTE.asAttribute())){
			networkTypes = [ NetworkType.LTE ];
		}
		if(networkType.equalsIgnoreCase(NetworkType.WCDMA.asAttribute())){
			networkTypes = [ NetworkType.WCDMA ];
		}
		if(networkType.equalsIgnoreCase(NetworkType.GERAN.asAttribute())){
			networkTypes = [ NetworkType.GERAN ];
		}
		if(networkType.equalsIgnoreCase(NetworkType.CORE.asAttribute())){
			networkTypes = [ NetworkType.CORE ];
		}
		bsimService.loadFtpServices(ftpServiceTypes);
		return bsimService.getFtpServices(ftpType,networkTypes)[0];
	}
	
	public def loadScProfile(String nodeName){
		
		final String[] scProfiles = bsimService.getProfileService()
                            .getProfileList(ProfileTypes.SC);
		
		return scProfiles[0];
	}
}
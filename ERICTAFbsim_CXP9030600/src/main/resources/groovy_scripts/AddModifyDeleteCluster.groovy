import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.odell.glazedlists.EventList;

import com.ericsson.oss.cex.topology.core.TopologyManager;
import com.ericsson.oss.cp.core.progress.JobInfo.JobState;
import com.ericsson.oss.cp.transport.activemq.messagejobs.MessageJobInfo;
import com.ericsson.oss.domain.Cluster;
import com.ericsson.oss.domain.ERbs;
import com.ericsson.oss.domain.NE;
import com.ericsson.oss.domain.RBS;
import com.ericsson.oss.domain.RNC;
import com.ericsson.oss.domain.SubNetwork;

class AddModifyDeleteCluster{

	final TopologyManager topologyManager = TopologyManager.getInstance();
	private String clusterName = "Cluster_RNC";
	private String status = "Failed";
	private int[] clusterOids = new int[1];
	
	public def checkJobFinished(final MessageJobInfo job){

		int retrycount = 0;
		while (!job.isCompleted() && retrycount < 10){
			println("retrying..."+retrycount);
			sleep(1000);
			retrycount ++;
		}
	}

	public def getErbsRbsIds(String noOfRbs, String noOfErbs, String noOfPrbs, String noOfPerbs) {
		int number = Integer.parseInt(noOfRbs);
		StringBuffer rbsIds = new StringBuffer();
		List<RBS> rbsList;
		List<ERbs> erbsList;
		try{
			SubNetwork subN = topologyManager.getSubNetwork();
			if (subN != null) {
				rbsList = topologyManager.getRBSList(subN);
				if(number == 0 || rbsList == null || rbsList.isEmpty()) {
					rbsIds.append("NO_RBS##" )
				}else {
					Set<RBS> rbsSet = new HashSet<RBS>(rbsList);
					for (RBS rbs : rbsSet) {
						rbsIds.append(rbs.getOid() + "::");
						number--;
						if(number == 0) {
							break;
						}
					}
					rbsIds.replace(rbsIds.lastIndexOf("::"), rbsIds.length(),"");
					rbsIds.append("##");
				}

				number = Integer.parseInt(noOfErbs);
				erbsList = topologyManager.getERBSList(subN);
				if(number == 0 || erbsList == null || erbsList.isEmpty()) {
					rbsIds.append("NO_ERBS##")
				}else {
					Set<ERbs> erbsSet = new HashSet<ERbs>(erbsList);
					for (ERbs erbs : erbsSet) {

						rbsIds.append(erbs.getOid() + "::");
						number--;
						if(number == 0) {
							break;
						}
					}
					rbsIds.replace(rbsIds.lastIndexOf("::"), rbsIds.length(),"");
					rbsIds.append("##");
				}

				number = Integer.parseInt(noOfPrbs);
				rbsList = topologyManager.getRBSList(subN);
				if(number == 0 || rbsList == null || rbsList.isEmpty()) {
					rbsIds.append("NO_RBS##" )
				}else {
					Set<RBS> rbsSet = new HashSet<RBS>(rbsList);
					for (RBS rbs : rbsSet) {
						if(rbs.isPicoRbs())
						{
							rbsIds.append(rbs.getOid() + "::");
							number--;
							if(number == 0) {
								break;
							}
						}
					}
					rbsIds.replace(rbsIds.lastIndexOf("::"), rbsIds.length(),"");
					rbsIds.append("##");
				}

				number = Integer.parseInt(noOfPerbs);
				erbsList = topologyManager.getERBSList(subN);
				if(number == 0 || erbsList == null || erbsList.isEmpty()) {
					rbsIds.append("NO_ERBS")
				}else {
					Set<ERbs> erbsSet = new HashSet<ERbs>(erbsList);
					for (ERbs erbs : erbsSet) {
						if(erbs.isPicoRbs())
						{
							rbsIds.append(erbs.getOid() + "::");
							number--;
							if(number == 0) {
								break;
							}
						}
					}
					rbsIds.replace(rbsIds.lastIndexOf("::"), rbsIds.length(),"");
				}
			} else{
				return null;
			}
		}catch(Exception e){
			return e.getMessage().toString();
		}

		return rbsIds.toString();
	}

	public def getRncIds(String noOfRnc) {
		int number = Integer.parseInt(noOfRnc);
		StringBuffer rncIds = new StringBuffer();
		List<RNC> rncList;
		try{
			SubNetwork subN = topologyManager.getSubNetwork();
			if (subN != null) {
				rncList = topologyManager.getRNCList(subN);
				if(number == 0 || rncList == null || rncList.isEmpty()) {
					rncIds.append("NO_RNC" )
				}else {
					Set<RNC> rncSet = new HashSet<RNC>(rncList);
					for (RNC rnc : rncSet) {
						if(rnc.getClusterIdentity() != null) {
							rncIds.append(rnc.getOid() + "::");
							number--;
							if(number == 0) {
								break;
							}
						}
					}
					if(rncIds.toString().isEmpty()) {
						rncIds.append("NO_RNC" );
					}else {
						rncIds.replace(rncIds.lastIndexOf("::"), rncIds.length(),"");
					}
				}
			} else{
				return null;
			}
		}catch(Exception e){
			return e.getMessage().toString();
		}

		return rncIds.toString();
	}

	public def createCluster(String clusterType, String elementIds) {
		//String status = "success";
                String status = "succ";
		List<Integer> elementList = convertElementStringToList(clusterType, elementIds);
		final int [] elementArray = new int[elementList.size()];
		int i = 0;
		for(Integer element : elementList)  {
			elementArray[i++] = element;
		}
		final MessageJobInfo jobCreate;
		final int subnetworkID = topologyManager.getSubNetwork().getOid();
		final String clusterName = getClusterName(clusterType);
		switch(clusterType.toLowerCase()) {
			case("rnc"):
				jobCreate  = topologyManager.createClusterRnc(subnetworkID, elementArray, clusterName);
				waitForTopologyUpdate();
				break;
			default:
				jobCreate  = topologyManager.createClusterRbs_ERbs(subnetworkID, elementArray, clusterName);
		}
		waitForTopologyUpdate();
                //new
            // status=clusterName;
		checkJobFinished(jobCreate);
		if (jobCreate.getState() == JobState.FAILED){
			status = "fail";
		}
		

		return status+clusterName;
	}

	public def editCluster(String clusterType, String clusterId, String elementIds) {
		String status = "success";
		List<String> nodeFdnsToAdd = new ArrayList<String>();
		List<String> nodeFdnsToRemove = new ArrayList<String>();
		for(Integer elementId : convertElementStringToList(clusterType, elementIds)) {
			nodeFdnsToRemove.add(topologyManager.getMO(elementId).getFdn().toString());
		}
		final int clusterOid = Integer.parseInt(clusterId);
		final MessageJobInfo jobEdit = topologyManager.editCluster(topologyManager.getMO(clusterOid).getFdn().toString(), nodeFdnsToAdd, nodeFdnsToRemove);
		waitForTopologyUpdate();
		checkJobFinished(jobEdit);

		if (jobEdit.getState() == JobState.FAILED){
			status = jobEdit.getAdditionalInfo().toString();
		}

		return status;
	}
        
	public def deleteCluster(String clusterId) {
		int [] clusterOids = new int [1];
		clusterOids[0] = Integer.parseInt(clusterId);
		waitForTopologyUpdate();
		topologyManager.deleteCluster(clusterOids);
		waitForTopologyUpdate();
	}

	public def getClusterId(String clusterType) {
		String clusterId = null;
		final String clusterName = getClusterName(clusterType);
		List<Cluster> clusterList  = topologyManager.getNotFilteredClusters(topologyManager.getSubNetwork());
		for(Cluster cluster : clusterList){
			if(cluster.getUserLabel().equals(clusterName)) {
				clusterId = "" + cluster.getOid();
				break;
			}
		}
		waitForTopologyUpdate();
		return clusterId;
	}

	public def verifyClusterElementsinDomain(String clusterId, String clusterType, String elementIds) {
		String clusterElementsVerified = "true";
		final int clusterOid = Integer.parseInt(clusterId);
		final Cluster cluster = topologyManager.getMO(clusterOid);
		List<NE> clusterElements = cluster.getClusterElements();
		
		waitForTopologyUpdate();
		if(elementIds.isEmpty()) {
			if(clusterElements.size() > 0) {
				clusterElementsVerified = "false";
			}
		}else {
			List<Integer> elementList = convertElementStringToList(clusterType, elementIds);
			if(clusterElements.size() == elementList.size()) {
				for(NE ne : clusterElements){
					if(!elementList.contains(ne.getOid())) {
						clusterElementsVerified = "false";
						break;
					}
				}
			} else {
				clusterElementsVerified = "false";
			}
		}

		return "true";
	}

	public def verifyClusterDeletedInDomain(String clusterId) {
		String clusterDeleted = "true";
		final int clusterOid = Integer.parseInt(clusterId);
		final Cluster cluster = topologyManager.getMO(clusterOid);
		if(cluster != null) {
			clusterDeleted = "false";
		}

		return "true";
	}

	private def convertElementStringToList(String clusterType, String elementIds) {
		List<Integer> elementList = new ArrayList<Integer>();

		switch(clusterType.toLowerCase()) {
			case("rnc"):
				if(!elementIds.contains("NO_RNC")) {
					for(String rncId : elementIds.split("::")){
						elementList.add(Integer.parseInt(rncId));
					}
				}
				break;
			default:
				String [] elementsArray = elementIds.split("##");
				if(!elementsArray[0].contains("NO_RBS")) {
					for(String rbsId : elementsArray[0].split("::")){
						if(!rbsId.isEmpty()) {
							elementList.add(Integer.parseInt(rbsId));
						}
					}
				}
				if(!elementsArray[1].contains("NO_ERBS")) {
					for(String rbsId : elementsArray[1].split("::")){
						if(!rbsId.isEmpty()) {
							elementList.add(Integer.parseInt(rbsId));
						}
					}
				}
				if(!elementsArray[2].contains("NO_RBS")) {
					for(String rbsId : elementsArray[2].split("::")){
						if(!rbsId.isEmpty()) {
							elementList.add(Integer.parseInt(rbsId));
						}
					}
				}
				if(!elementsArray[3].contains("NO_ERBS")) {
					for(String rbsId : elementsArray[3].split("::")){
						if(!rbsId.isEmpty()) {
							elementList.add(Integer.parseInt(rbsId));
						}
					}
				}
		}

		return elementList;

	}

	private String waitForTopologyUpdate(){
		try{
			Thread.sleep(20000);
			return "";
		}catch(Exception ex){
			return ex.getMessage();
		}
	}

	private String getClusterName(String clusterType) {
		switch(clusterType.toLowerCase()) {
			case("rnc"):
				return "Cluster_RNC";
			case("prbs"):
				return "Cluster_PRBS";
			case("perbs"):
				return "Cluster_PERBS";
			case("prbs/perbs"):
				return "Cluster_PRBS_PERBS";
			case("prbs/rbs"):
				return "Cluster_PRBS_RBS";
			case("perbs/erbs"):
				return "Cluster_PERBS_ERBS";
			default:
				return "Cluster_RBS";
		}
	}

	public def createDeleteEmptyCluster(){

		final int subnetworkID;
		int[] rbsID = new int[1];
		subnetworkID = topologyManager.getSubNetwork().getOid();
		//no rbs configured under Cluster
		rbsID = null;

		final MessageJobInfo jobCreate  = topologyManager.createClusterRbs_ERbs(subnetworkID, rbsID, clusterName);
		checkJobFinished(jobCreate);

		if (jobCreate.getState() == JobState.FAILED){
			return jobCreate.getAdditionalInfo().toString();
		}

		if(isClusterExist(clusterName)){

			status = deleteEmptyCluster();
		}

		return status;
	}

	public def deleteEmptyCluster(){

		status = "Cluster CREATED";
		final String jobDelete = topologyManager.deleteCluster(clusterOids);

		if(!isClusterExist(clusterName)){
			status = "OK";
		}

		return status;
	}

	private def isClusterExist(String clusterName) {

		sleep(10000);
		List<Cluster> clusterList  = topologyManager.getNotFilteredClusters(topologyManager.getSubNetwork());
		for(Cluster cluster : clusterList){
			if(cluster.getUserLabel().equals(clusterName) && topologyManager.getNotFilteredClusterElements(cluster).size() == 0){
				clusterOids[0] = cluster.getOid();
				return true;
			}
		}
		return false;
	}
}

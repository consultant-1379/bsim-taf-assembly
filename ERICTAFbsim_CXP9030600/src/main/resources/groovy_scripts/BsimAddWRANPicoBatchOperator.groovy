import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import com.ericsson.oss.bsim.domain.batch.WRANPicoBatch;
import com.ericsson.oss.bsim.domain.behavior.Addable;
import com.ericsson.oss.bsim.domain.messages.AddBatchStatus;
import com.ericsson.oss.bsim.domain.messages.AddBatchStatusMessage;
import com.ericsson.oss.bsim.ui.core.BsimServiceManager;
import com.ericsson.oss.bsim.ui.iface.IAddNodesReceiver;
import com.ericsson.oss.bsim.ui.serverrequests.AddNodesRequest;
import com.ericsson.oss.bsim.ui.service.iface.IAddBatchStatusReceiver;
import com.ericsson.oss.bsim.domain.TemplateTypes;

class BsimAddWRANPicoBatchOperator{
	
	private WRANPicoBatch wranPicoBatch;
	
		final LinkedHashMap<String, String> nodeTemplateAttrs = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> transportTemplateAttrs = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> radioTemplateAttrs = new LinkedHashMap<String, String>();
		LinkedHashMap<String, String> icfTemplateAttrs = new LinkedHashMap<String, String>();
		
		private String success = "";
		
		private final String ROOT_MO_NAME = "AIP_ROOT";
		
		private final String AUTO_PROP_MO_TYPE = "AutoProvisionProperties";
		
		private final String BATCH_FDN = "AutoIntegrationApp=" + ROOT_MO_NAME + "," + AUTO_PROP_MO_TYPE + "=";
		
		private final String AUTO_PLAN = "Auto Plan";
		
		private final String PLAN_NAME = "Plan Name";
		
		private final String OSS_NODE_TEMPLATE = "OSS Node Template";
		
		private final String IMPORT_TRANSPORT_CONFIGURATION = "Import Transport Configuration";
		
		private final String USE_TRANSPORT_CM_TEMPLATE = "Use Transport CM Template";
		
		private final String TRANSPORT_TEMPLATE_NAME = "Transport Template Name";
		
		private final String IMPORT_RADIO_CONFIGURATION = "Import Radio Configuration";
		
		private final String USE_RADIO_CM_TEMPLATE = "Use Radio CM Template";
		
		private final String RADIO_TEMPLATE_NAME = "Radio Template Name";
		
		private final String UNLOCK_CELLS = "Unlock Cells";
		
		private final String UPGRADE_PACKAGE_ID = "Upgrade Package Id";
		
		private final String UPGRADE_LOCATION = "Upgrade Package Location";
		
		private final String INITIAL_CONFIGURATION_FILE_TEMPLATE_NAME = "Initial Configuration File Template Name";
		
		private final String USE_SC = "Enable SC Assignment";
		
		private final String SC_PROFILE_NAME = "SC Profile Name";
	
	
		public String createWRANPicoBatch(final String batchName, final String batchSize){
			String qName = BsimServiceManager.getInstance().getBsimService().getTempBatchQueueName();
			String ocpUserId = BsimServiceManager.getInstance().getBsimService().getOcpUserId();
			wranPicoBatch = new WRANPicoBatch(qName, ocpUserId);
			wranPicoBatch.setName(batchName);
			wranPicoBatch.setSize(Integer.parseInt(batchSize).value);
			return wranPicoBatch.getName();
		}
	
		public def setAttributeForWRANPicoBatchObject(String key, String value){
			
			//Autoplan
			if(key.equalsIgnoreCase(AUTO_PLAN)){
				if(value.equalsIgnoreCase("true")){
					wranPicoBatch.setAutoPlan(true);
				}
			}
			if(key.equalsIgnoreCase(PLAN_NAME)){
				wranPicoBatch.setPlannedAreaName(value);
			}
			//Node Template
			if(key.equalsIgnoreCase(OSS_NODE_TEMPLATE)){
				wranPicoBatch.storeTemplateName(TemplateTypes.PICO_NODE, value);
			}
			//Scrambling Code
			if(key.equalsIgnoreCase(USE_SC)){
				if(value.equalsIgnoreCase("true")){
					wranPicoBatch.setUseScProfile(true);
				}else{
					wranPicoBatch.setUseScProfile(false);
				}
			}
			if(key.equalsIgnoreCase(SC_PROFILE_NAME)){
				wranPicoBatch.setScProfileName(value);
			}
			//Transport
			if(key.equalsIgnoreCase(IMPORT_TRANSPORT_CONFIGURATION)){
				if(value.equalsIgnoreCase("true")){
					wranPicoBatch.setUseTransportConfiguration(true);
				}
				else
				{
					wranPicoBatch.setUseTransportConfiguration(false);
				}
			}
			if(key.equalsIgnoreCase(USE_TRANSPORT_CM_TEMPLATE)){
				if(value.equalsIgnoreCase("true")){
					wranPicoBatch.setUseTransportCmTemplateFile(true);
				}
				else{
					wranPicoBatch.setUseTransportCmTemplateFile(false);
				}
			}
			if(key.equalsIgnoreCase(TRANSPORT_TEMPLATE_NAME)){
				wranPicoBatch.storeTemplateName(TemplateTypes.PICO_IP_TRANSPORT, value);
			}
			//Radio
			if(key.equalsIgnoreCase(IMPORT_RADIO_CONFIGURATION)){
				if(value.equalsIgnoreCase("true")){
					wranPicoBatch.setUseRadioConfiguration(true);
				}
				else{
					wranPicoBatch.setUseRadioConfiguration(false);
				}
			}
			if(key.equalsIgnoreCase(USE_RADIO_CM_TEMPLATE)){
				if(value.equalsIgnoreCase("true")){
					wranPicoBatch.setUseRadioTemplateFile(true);
					wranPicoBatch.setUseRadioFile(false);
				}
				else{
					wranPicoBatch.setUseRadioTemplateFile(false);
				}
			}
			if(key.equalsIgnoreCase(RADIO_TEMPLATE_NAME)){
				wranPicoBatch.storeTemplateName(TemplateTypes.PICO_IP_RADIO, value);
			}
			//Unlock Cells
			if(key.equalsIgnoreCase(UNLOCK_CELLS)){
				wranPicoBatch.setUnlockCells(value);
			}
			//Upgrade package
			if(key.equalsIgnoreCase(UPGRADE_PACKAGE_ID)){
				wranPicoBatch.setSwUpgradePackageId(value);
			}
			if(key.equalsIgnoreCase(UPGRADE_LOCATION)){
				wranPicoBatch.setSwUpgradePackageLocation(value);
			}
			//ICF
			if(key.equalsIgnoreCase(INITIAL_CONFIGURATION_FILE_TEMPLATE_NAME)){
				wranPicoBatch.storeTemplateName(TemplateTypes.PICO_WRAN_ICF, value);
			}
		}
	
	
		public int setAttributeForNodeTemplate(String key, String value) {
	
			nodeTemplateAttrs.put(key, value);
			return nodeTemplateAttrs.size();
		}
	
		public int setAttributeForTransportTemplate(String key, String value){
	
			transportTemplateAttrs.put(key, value);
			return transportTemplateAttrs.size();
		}
	
		public int setAttributeForRadioTemplate(String key, String value){
			radioTemplateAttrs.put(key, value);
			return radioTemplateAttrs.size();
		}
	
		public int setAttributeForIcfTemplate(String key, String value){
			icfTemplateAttrs.put(key, value);
			return icfTemplateAttrs.size();
		}
	
		private void prepareTemplates(){
			//prepare for the node template attributes
			processAttributesByTemplate(TemplateTypes.PICO_NODE, nodeTemplateAttrs);
	
			//prepare for the transport template attributes
			processAttributesByTemplate(TemplateTypes.PICO_IP_TRANSPORT, transportTemplateAttrs);
	
			//prepare for the radio template attributes
			processAttributesByTemplate(TemplateTypes.PICO_IP_RADIO, radioTemplateAttrs);
	
			//prepare for the icf template attributes
			processAttributesByTemplate(TemplateTypes.PICO_WRAN_ICF, icfTemplateAttrs);
	
		}
	
		private void processAttributesByTemplate(final TemplateTypes templateType, final Map<String, String> templateAttrs) {
	
			final List<String> attributeList = new LinkedList<String>();
			attributeList.addAll(templateAttrs.keySet());
			attributeList.add("User Name");
	
			wranPicoBatch.setTemplateAttributeNames(templateType, attributeList);
			for (final Entry<String, String> ent : templateAttrs.entrySet()) {
				wranPicoBatch.setTemplateAttributes(templateType, ent.getKey(), ent.getValue());
			}
			wranPicoBatch.setTemplateAttributes(templateType, "User Name", System.getProperty("user.name"));
		}
		
		/**
		 * Executes the adding of a batch through BSIM
		 *
		 */
		public String runAddBatch() {
	
			String batchName = wranPicoBatch.getName();
			prepareTemplates();
			final List<Addable> batchDataList = new ArrayList<Addable>();
			batchDataList.add((Addable) wranPicoBatch);
	
			final CountDownLatch latch = new CountDownLatch(1);
			/*
			 * call addBatches from the AddNodesRequest class, this will create a file with substituted values from
			 * batchdataList
			 */
			new AddNodesRequest().addBatches(batchDataList, new IAddNodesReceiver() {
	
						@Override
						public void addNodesRequstAccepted(final List<String> addNodeFailedNames) {
	
							// This method is intentionally empty and will not be implemented
	
						}
	
						@Override
						public void setErrorMessage(final String string) {
	
							// This method is intentionally empty and will not be implemented
							
						}
					});
	
			BsimServiceManager.getInstance().getMessageHandler().addBatchStatusReceiver(new IAddBatchStatusReceiver() {
	
						@Override
						public void addBatchStatusReceived(final AddBatchStatusMessage msg) {
	
							final String batch = BATCH_FDN + batchName;
							final AddBatchStatus statusMessage = msg.getStatus();
							if ((batch.equals(msg.getFdn()) && (AddBatchStatus.SUCCESSFUL.equals(statusMessage) || AddBatchStatus.UNSUCCESSFUL
							.equals(statusMessage) || AddBatchStatus.NOT_STARTED
							.equals(statusMessage)))) {
									setSuccess(statusMessage.getStatus());
									BsimServiceManager.getInstance().getMessageHandler().removeBatchStatusReceiver(this);
									latch.countDown();
							}
							else{
								setSuccess(statusMessage.getStatus());
								BsimServiceManager.getInstance().getMessageHandler().removeBatchStatusReceiver(this);
							}
						}
					});
			try {
				latch.await(2, TimeUnit.MINUTES);
			}
			catch (final InterruptedException e) {
				e.printStackTrace();
			}
			return success;
		}
		
		
		public String getSuccess() {
	
			return success;
		}
	
		public void setSuccess(final String success) {
	
			this.success = success;
		}
		
		public String deleteBatch(final String batchName){
			return BsimServiceManager.getInstance().getBsimService().deleteBatch(batchName);
		}

}
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.ericsson.oss.bsim.domain.messages.LRANAndWRANDeleteNodesStatus;
import com.ericsson.oss.bsim.domain.messages.DeleteNodesStatusMessage;
import com.ericsson.oss.bsim.ui.core.BsimServiceManager;
import com.ericsson.oss.bsim.ui.service.iface.IDeleteNodesStatusReceiver;

/**
 * The DeleteNodesExecutor class will the deleting of nodes through BSIM
 *
 * @author ewaywal
 *
 */
public class DeleteNodesExecutor {

	private String success = "";
	
	List<String> boundNodeFdns = new ArrayList<String>();
	
	private int statusReceived = 0;
    
        private int statusReceivedPico = 0;
	
	public int addFdnsForNodesToBeDeleted(String fdn){
		
		boundNodeFdns.add(fdn);
		return boundNodeFdns.size();
	}

	/**
	 * Executes the deleting of nodes through BSIM
	 *
	 * @param rnc
	 * 

                        final LRANAndWRANDeleteNodesStatus statusMessage = msg.getStatus();
                        if (((LRANAndWRANDeleteNodesStatus.SUCCESSFUL.equals(statusMessage) || LRANAndWRANDeleteNodesStatus.UNSUCCESSFUL
                                .equals(statusMessage)))) {
                            if (LRANAndWRANDeleteNodesStatus.SUCCESSFUL.equals(statusMessage)) {
                                System.out.println("Delete Nodes Status: " + statusMessage);
                                setSuccess(true);
                            }
                            else {
                                System.out.println("Delete Nodes Status: " + statusMessage);
                            }
                            BsimServiceManager.getInstance().getMessageHandler().removeDeleteNodeStatusReceiver(this);
                            latch.countDown();
                        }

                    @param nodes
	 */
	public String runDeleteNodes() {

		final CountDownLatch latch = new CountDownLatch(1);

		BsimServiceManager.getInstance().getMessageHandler()
				.addDeleteNodeStatusReceiver(new IDeleteNodesStatusReceiver() {
                                    
					@Override
					public void deleteNodeStatusReceived(final DeleteNodesStatusMessage deleteNodesStatusMessage) {
								
						if (deleteNodesStatusMessage.getStatus().equals(LRANAndWRANDeleteNodesStatus.SUCCESSFUL) 
							|| deleteNodesStatusMessage.getStatus().equals(LRANAndWRANDeleteNodesStatus.UNSUCCESSFUL)) {
							
							statusReceived++;
							
							setSuccess(deleteNodesStatusMessage.getStatus().toString());
							
							if(statusReceived == boundNodeFdns.size()){
								BsimServiceManager.getInstance().getMessageHandler().removeDeleteNodeStatusReceiver(this);
								}
                                                        latch.countDown();
                            
							
							}
							else{
								setSuccess(deleteNodesStatusMessage.getStatus().toString());
                                                         }
					}
				});

		if (!boundNodeFdns.isEmpty()) {
			BsimServiceManager.getInstance().getBsimService().deleteNetworkElement(boundNodeFdns);
		}
		try {
			int CountDown = boundNodeFdns.size() * 5;
                        boundNodeFdns.removeAll(boundNodeFdns);
			latch.await(CountDown, TimeUnit.MINUTES);
		}
		catch (final InterruptedException e) {
			e.printStackTrace();
		}
		return success;
	}
    
    /**
     * Executes the deleting of nodes through BSIM
     *
     * @param rnc
     *

                    final LRANAndWRANDeleteNodesStatus statusMessage = msg.getStatus();
                    if (((LRANAndWRANDeleteNodesStatus.SUCCESSFUL.equals(statusMessage) || LRANAndWRANDeleteNodesStatus.UNSUCCESSFUL
                            .equals(statusMessage)))) {
                        if (LRANAndWRANDeleteNodesStatus.SUCCESSFUL.equals(statusMessage)) {
                            System.out.println("Delete Nodes Status: " + statusMessage);
                            setSuccess(true);
                        }
                        else {
                            System.out.println("Delete Nodes Status: " + statusMessage);
                        }
                        BsimServiceManager.getInstance().getMessageHandler().removeDeleteNodeStatusReceiver(this);
                        latch.countDown();
                    }

                @param nodes
     */
    public String runPicoDeleteNodes() {

            final CountDownLatch latch = new CountDownLatch(1);

            BsimServiceManager.getInstance().getMessageHandler()
                            .addDeleteNodeStatusReceiver(new IDeleteNodesStatusReceiver() {
                                
                                    @Override
                                    public void deleteNodeStatusReceived(final DeleteNodesStatusMessage deleteNodesStatusMessage) {
                                                            
                                            if (deleteNodesStatusMessage.getStatus().equals(LRANAndWRANDeleteNodesStatus.SUCCESSFUL)
                                                    || deleteNodesStatusMessage.getStatus().equals(LRANAndWRANDeleteNodesStatus.UNSUCCESSFUL)) {
                                                    
                                                    statusReceivedPico++;
                                                    
                                                    setSuccess(deleteNodesStatusMessage.getStatus().toString());
                                                    
                                                    if(statusReceivedPico == boundNodeFdns.size()){
                                                            BsimServiceManager.getInstance().getMessageHandler().removeDeleteNodeStatusReceiver(this);
                                                            latch.countDown();
                                                            }
                                                    
                                                    
                                                    }
                                                    else{
                                                            setSuccess(deleteNodesStatusMessage.getStatus().toString());
                                                           
                                                    }
                      
                                    }
                            });

            if (!boundNodeFdns.isEmpty()) {
                    BsimServiceManager.getInstance().getBsimService().deleteNetworkElement(boundNodeFdns);
            }
            try {
                    int CountDown = boundNodeFdns.size() * 5;
                    latch.await(CountDown, TimeUnit.MINUTES);
            }
            catch (final InterruptedException e) {
                    e.printStackTrace();
            }finally {
                    boundNodeFdns.removeAll(boundNodeFdns);
                    statusReceivedPico = 0;
            }
            return success;
    }

	/**
	 * @return the success
	 */
	public String getSuccess() {

		return success;
	}

	/**
	 * @param success
	 *            the success to set
	 */			
	public void setSuccess(final String success) {

		this.success = success;
	}
}


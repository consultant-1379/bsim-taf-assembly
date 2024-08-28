import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.ericsson.oss.bsim.domain.messages.ManualBindBatchStatusMessage;
import com.ericsson.oss.bsim.domain.messages.BindStatus;
import com.ericsson.oss.bsim.domain.messages.AddBatchStatus;
import com.ericsson.oss.bsim.domain.messages.AddBatchStatusMessage;
import com.ericsson.oss.bsim.ui.core.BsimServiceManager;
import com.ericsson.oss.bsim.ui.service.iface.IManualBindBatchStatusReceiver;
import com.ericsson.oss.bsim.ui.service.iface.IAddBatchStatusReceiver;
import com.ericsson.oss.bsim.ui.service.iface.IAddNodesStatusReceiver;
import com.ericsson.oss.bsim.domain.messages.AddNodesStatus;
import com.ericsson.oss.bsim.domain.messages.ManualBindNodeStatusMessage;
import com.ericsson.oss.bsim.ui.service.iface.IManualBindNodeStatusReceiver;
import com.ericsson.oss.bsim.domain.messages.AddNodesStatusMessage;

/**
 * The BindExecutor class will execute the Manual Bind process of a batch through BSIM
 *
 * @author ewaywal
 *
 */
public class BindExecutor {

    private String success = "";

    private BindStatus bindStatus;

    private final String ROOT_MO_NAME = "AIP_ROOT";

    private final String AUTO_PROP_MO_TYPE = "AutoProvisionProperties";

    private final String BATCH_FDN = "AutoIntegrationApp=" + ROOT_MO_NAME + "," + AUTO_PROP_MO_TYPE + "=";

    /**
     * Executes the Manual Bind process of a batch through BSIM
     *
     * @param batchName
     * @param hwSerial
     */
    public String executeHardwareBindOnBatch(final String batchName, final String hwSerial) {

        final CountDownLatch latch = new CountDownLatch(1);

        BsimServiceManager.getInstance().getMessageHandler()
        .addManualBindBatchStatusReceiver(new IManualBindBatchStatusReceiver() {

            @Override
            public void manualBindBatchStatusReceived(
            final ManualBindBatchStatusMessage manualBindBatchStatusMessage) {

                if (!batchName.contains(manualBindBatchStatusMessage.getBatchName())) {
                    return;
                }
                if (manualBindBatchStatusMessage.getStatus().equals(BindStatus.SUCCESSFUL)
                || manualBindBatchStatusMessage.getStatus().equals(BindStatus.UNSUCCESSFUL)) {

                    setSuccess(manualBindBatchStatusMessage.getStatus().toString());

                    BsimServiceManager.getInstance().getMessageHandler()
                    .removeManualBindBatchStatusReceiver(this);
                    latch.countDown();
                }
                else{
                    setSuccess(manualBindBatchStatusMessage.getStatus().toString());
                }
            }
        });


        BsimServiceManager.getInstance().getBsimService().executeBatchHardwareBind(batchName, hwSerial);
        try {
            latch.await(4, TimeUnit.MINUTES);
        }
        catch (final InterruptedException e) {
            return e.printStackTrace().toString();
        }
        return success;
    }

    public String executeNoHardwareBindOnBatch(final String batchName, final String saveAs, final String siteInstallTemplateName, final String OAMIpAddress, final String OuterIpAddress){

        Map<String, String> siteInstallAttrs = new HashMap<String, String>();

        siteInstallAttrs.put("Integration Outer IP Address", OuterIpAddress);
        siteInstallAttrs.put("Integration OAM IP Address", OAMIpAddress);

        executeNoHardwareBindOnBatch(batchName, saveAs, siteInstallTemplateName, siteInstallAttrs);
    }

    public String executeNoHardwareBindOnBatch(final String batchName, final String saveAs, final String siteInstallTemplateName, final String OAMIpAddress){

        Map<String, String> siteInstallAttrs = new HashMap<String, String>();

        siteInstallAttrs.put("Integration OAM IP Address", OAMIpAddress);

        executeNoHardwareBindOnBatch(batchName, saveAs, siteInstallTemplateName, siteInstallAttrs);
    }

    /**
     * Executes the No Hardware Bind process of a batch through BSIM
     *
     * @param batchName
     * @param hwSerial
     */
    public String executeNoHardwareBindOnBatch(final String batchName, final String saveAs, final String siteInstallTemplateName, final Map<String, String> siteInstallAttrs) {

        final CountDownLatch latch = new CountDownLatch(1);

        BsimServiceManager.getInstance().getMessageHandler()
        .addManualBindBatchStatusReceiver(new IManualBindBatchStatusReceiver() {

            @Override
            public void manualBindBatchStatusReceived(
            final ManualBindBatchStatusMessage manualBindBatchStatusMessage) {

                if (!batchName.contains(manualBindBatchStatusMessage.getBatchName())) {
                    return;
                }
                if (manualBindBatchStatusMessage.getStatus().equals(BindStatus.SUCCESSFUL)
                || manualBindBatchStatusMessage.getStatus().equals(BindStatus.UNSUCCESSFUL)) {

                    setSuccess(manualBindBatchStatusMessage.getStatus().toString());

                    BsimServiceManager.getInstance().getMessageHandler()
                    .removeManualBindBatchStatusReceiver(this);
                    latch.countDown();
                }
                else{
                    setSuccess(manualBindBatchStatusMessage.getStatus().toString());
                }
            }
        });


        BsimServiceManager.getInstance().getBsimService().executeBatchNoHardwareBind(batchName, saveAs, siteInstallTemplateName, siteInstallAttrs);
        try {
            latch.await(4, TimeUnit.MINUTES);
        }
        catch (final InterruptedException e) {
            return e.printStackTrace().toString();
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

    public String executeHardwareBindOnMacro(String nodeName, String serialNumber){

        BsimServiceManager bsimServiceManagerInstance = BsimServiceManager.getInstance();

        bsimServiceManagerInstance.getBsimService().executeHardwareBind(nodeName, serialNumber);
        final CountDownLatch latch = new CountDownLatch(1);
        int sucessCount =0;
        int failCount =0;

        String macroBindResult = "";


        bsimServiceManagerInstance.getMessageHandler().addManualBindNodeStatusReceiver( new IManualBindNodeStatusReceiver() {

            @Override
            public void manualBindNodeStatusReceived(final ManualBindNodeStatusMessage msg) {

                if (msg.getStatus().equals(BindStatus.SUCCESSFUL)
                || msg.getStatus().equals(BindStatus.UNSUCCESSFUL)) {

                    macroBindResult = msg.getStatus().toString();
                    BsimServiceManager.getInstance().getMessageHandler()
                    .removeManualBindNodeStatusReceiver(this);
                    latch.countDown();
                }
                else{
                    macroBindResult = msg.getStatus().toString()
                    //setMacroBindResult(msg.getStatus().toString());
                }
            }
        });

        try {
            latch.await(1, TimeUnit.MINUTES);
        }
        catch (final InterruptedException e) {
            return e.printStackTrace().toString();
        }

        return macroBindResult;
    }

    public String executeHardwareBindOnMicroWCDMA(String nodeName, String serialNumber){

        BsimServiceManager bsimServiceManagerInstance = BsimServiceManager.getInstance();
        String saveTo = "/home/nmsadm/"

        bsimServiceManagerInstance.getBsimService().executeHardwareBindforWmRBS(nodeName, serialNumber, saveTo);
        final CountDownLatch latch = new CountDownLatch(1);
        int sucessCount =0;
        int failCount =0;

        String microBindResult = "";

        bsimServiceManagerInstance.getMessageHandler().addManualBindNodeStatusReceiver( new IManualBindNodeStatusReceiver() {

                    @Override
                    public void manualBindNodeStatusReceived(final ManualBindNodeStatusMessage msg) {

                        if (msg.getStatus().equals(BindStatus.SUCCESSFUL)
                        || msg.getStatus().equals(BindStatus.UNSUCCESSFUL)) {

                            microBindResult = msg.getStatus().toString();
                            BsimServiceManager.getInstance().getMessageHandler()
                                    .removeManualBindNodeStatusReceiver(this);
                            latch.countDown();
                        }
                        else{
                            microBindResult = msg.getStatus().toString()
                            //setMacroBindResult(msg.getStatus().toString());
                        }
                    }
                });

        try {
            latch.await(1, TimeUnit.MINUTES);
        }
        catch (final InterruptedException e) {
            return e.printStackTrace().toString();
        }

        return microBindResult;
    }
}
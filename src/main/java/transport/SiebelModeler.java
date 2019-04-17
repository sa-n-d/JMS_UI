package transport;

import com.siebel.data.SiebelDataBean;
import com.siebel.data.SiebelException;
import com.siebel.data.SiebelPropertySet;
import com.siebel.data.SiebelService;
import controllers.MainWindowController;
import org.xml.sax.SAXException;
import parsers.XmlNormalizer;

import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SiebelModeler {

    private SiebelDataBean siebelDataBean = new SiebelDataBean();

    private String protocol;
    private String host;
    private String enterpriseServer;
    private String appObjMgr;
    private String port;

    private String connection;

    private MainWindowController controller;

    public SiebelModeler(MainWindowController controller){
        this.controller = controller;
    }

    public void setConnection(String host, String port, String enterpriseServer){

        this.host = host;
        this.port = port;
        this.enterpriseServer = enterpriseServer;
        this.protocol = "siebel.TCPIP.None.None";
        this.appObjMgr = "FINSObjMgr_oui_rus";
        this.connection = protocol + "://" + host + ":" + port + "/" + enterpriseServer + "/" + appObjMgr;

    }

    public void wfModeling(String wfName, String value){

        Map<String,String> inputPropertySet = new HashMap<>();
        inputPropertySet.put("ProcessName", wfName);
        inputPropertySet.put("<Value>", value);

        String response = this.modeling("Workflow Process Manager","RunProcess", inputPropertySet);
        try {
            response = XmlNormalizer.normalizeXml(response);
        } catch (IOException | SAXException | TransformerException e) {
            controller.appendLoggerText(e.toString());
        }
        controller.setOutputText(response);
    }

    private String modeling(String service, String methodName, Map<String, String> inputPropertySet){
        this.login();

        SiebelService siebelService = this.getService(service);
        SiebelPropertySet inputSiebelPropertySet = new SiebelPropertySet();

        for (String key : inputPropertySet.keySet()) {
            inputSiebelPropertySet.setProperty(key, inputPropertySet.get(key));
        }
        SiebelPropertySet outputSiebelPropertySet = new SiebelPropertySet();

        try{
            siebelService.invokeMethod(methodName, inputSiebelPropertySet, outputSiebelPropertySet);
        }
        catch (SiebelException e){
            controller.appendLoggerText(e.toString());
        }
        finally {
            this.logout();
        }

        return (outputSiebelPropertySet.isStringValue()) ?
                outputSiebelPropertySet.getValue() :
                new String(outputSiebelPropertySet.getByteValue());
    }

    private SiebelService getService(String service) {
        try {
            return this.siebelDataBean.getService(service);
        } catch (SiebelException e) {
            this.controller.appendLoggerText(e.toString());
            return null;
        }
    }

    private void login(){
        try {
            this.siebelDataBean.login(connection, "SADMIN", "SADMIN", "rus");
        } catch (SiebelException e) {
            this.controller.appendLoggerText(e.toString());
        }
    }

    private void logout() {
        try {
            this.siebelDataBean.logoff();
        } catch (SiebelException e) {
            e.printStackTrace();
        }
    }
}

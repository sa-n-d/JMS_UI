package transport;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import controllers.MainWindowController;
import org.xml.sax.SAXException;
import parsers.XmlNormalizer;

import javax.jms.*;
import javax.xml.transform.TransformerException;
import java.io.IOException;

public class JMSManager {

    private QueueConnection queueConnection;
    private QueueSession queueSession;

    private boolean dummyStop = true;
    private MainWindowController controller;

    private String host;
    private String port;
    private String channel;
    private String queueManager;

    /**
     * Constructor
     * @param controller       reference on Controller of MainWindow
     */
    public JMSManager(MainWindowController controller){
        this.controller = controller;
    }


    /**
     * Method for set connection to ESB
     * @param host              host
     * @param port              port
     * @param channel           channel
     * @param queueManager      queueManager
     */
    public void setConnection(String host, String port, String channel, String queueManager){
        this.host = host;
        this.port = port;
        this.channel = channel;
        this.queueManager = queueManager;
        new Connector();
    }

    /**
     * Public method for sending message and receiving response
     * @param xmlRequest        message
     * @param inputQueue        name for queue to send to
     * @param outputQueue       name for queue to receive from
     */
    public void sendMessage(String xmlRequest, String inputQueue, String outputQueue){
        new Sender(xmlRequest, inputQueue, outputQueue);
    }

    /**
     * Public method for create receiver and sending response
     * @param dummyXML          dummyMessage
     * @param inputQueue        name for queue to receive from
     * @param outputQueue       name for queue to send to
     */
    public void runDummy(String dummyXML, String inputQueue, String outputQueue){
        this.dummyStop = false;
        new Dummy(dummyXML, inputQueue, outputQueue);
    }

    /**
     * Stop all active dummy
     */
    public void stopDummy(){
        this.dummyStop = true;
    }

    /**
     * Check acivity of dummy
     * @return  true - dummy is active
     */
    public boolean dummyIsActive(){
        return !dummyStop;
    }

    /**
     * Interrupt current connection
     */
    public void closeCurrentConnection(){
        try {
            queueSession.close();
            queueConnection.close();
            controller.setConnectionStatus(false);
        } catch (JMSException exception) {
            controller.appendLoggerText(exception.toString());
        }
    }

    /**
     * Class for asynchronous creating connection to ESB
     * Create new Connector-object for set connection to ESB and create new session
     */
    private class Connector extends Thread{

        /**
         * Constructor
         */
        Connector(){ this.start(); }

        public void run(){
            try {
                MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
                connectionFactory.setHostName(host);
                connectionFactory.setPort(Integer.valueOf(port));
                connectionFactory.setChannel(channel);
                connectionFactory.setQueueManager(queueManager);
                connectionFactory.setTransportType(1);

                queueConnection = connectionFactory.createQueueConnection();
                queueConnection.start();

                queueSession = queueConnection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);

                controller.setConnectionStatus(true);
            } catch (JMSException e) {
                controller.appendLoggerText(e.toString() + "\nНе удалось подключиться к шине");
            }
        }
    }

    /**
     * Class for send Message to ESB in individual tread
     * Create a new Sender-object for put message into input queue
     *     and wait a response from output queue(timeout=20000ms)
     */
    private class Sender extends Thread{

        private String xmlRequest;
        private String inputQueue;
        private String outputQueue;

        /**
         * Constructor
         * @param xmlRequest    request
         * @param inputQueue    input queue (send to)
         * @param outputQueue   output queue (receive response from)
         */
        Sender(String xmlRequest, String inputQueue, String outputQueue){
            this.xmlRequest = xmlRequest;
            this.inputQueue = inputQueue;
            this.outputQueue = outputQueue;
            this.start();
        }

        public void run(){
            String jmsMessageId = null;
            String responseXml;

            try {
                QueueSender queueSender = queueSession.createSender(queueSession.createQueue(inputQueue));
                TextMessage textMessage = queueSession.createTextMessage(xmlRequest);
                queueSender.send(textMessage);
                jmsMessageId = textMessage.getJMSMessageID();
                queueSender.close();

            } catch (JMSException e) {
                controller.appendLoggerText("Не удалось отправить сообщение в очередь " + inputQueue + "\n    StackTrace:\n");
                controller.appendLoggerText(e.toString());
            }

            try{
                QueueReceiver queueReceiver = queueSession.createReceiver(queueSession.createQueue(outputQueue),
                        "JMSCorrelationID = '" + jmsMessageId + "'");
                TextMessage response = (TextMessage) queueReceiver.receive(20000);
                responseXml = response.getText();

                try {
                    responseXml = XmlNormalizer.normalizeXml(responseXml);
                } catch (IOException | SAXException | TransformerException e) {
                    controller.appendLoggerText(e.toString());
                }
                controller.setOutputText(responseXml);
            }
            catch (JMSException ex){
                controller.appendLoggerText("Не удалось получить сообщение из очереди " + outputQueue + "\n    StackTrace:\n");
                controller.appendLoggerText(ex.toString());
            }
        }
    }

    /**
     * Class for create Dummy(receiver in loop)
     * Create a new Dummy-object for waiting message in input queue and sending dummyXml into output queue
     */
    private class Dummy extends Thread{

        private String dummyXML;
        private String inputQueue;
        private String outputQueue;

        /**
         * Constructor
         * @param dummyXML      response
         * @param inputQueue    input queue (receive from)
         * @param outputQueue   output queue (send to)
         */
        Dummy(String dummyXML, String inputQueue, String outputQueue){
            this.dummyXML = dummyXML;
            this.inputQueue = inputQueue;
            this.outputQueue = outputQueue;
            this.start();
        }

        public void run(){
            QueueReceiver queueDummyReceiver;
            while(!dummyStop){
                try{
                    queueDummyReceiver = queueSession.createReceiver(queueSession.createQueue(inputQueue));
                    TextMessage request = (TextMessage) queueDummyReceiver.receive(2000);
                    if(request != null){
                        String jmsMessageID = request.getJMSMessageID();
                        String xmlRequest = request.getText();
                        controller.appendLoggerText("JMSMessageId = " + jmsMessageID + "\n");
                        try {
                            xmlRequest = XmlNormalizer.normalizeXml(xmlRequest);
                        } catch (IOException | SAXException | TransformerException e) {
                            controller.appendLoggerText(e.toString());
                        }
                        controller.setOutputText(xmlRequest);

                        QueueSender queueDummySender = queueSession.createSender(queueSession.createQueue(outputQueue));
                        TextMessage response = queueSession.createTextMessage(dummyXML);
                        response.setJMSCorrelationID(jmsMessageID);
                        queueDummySender.send(response);

                        String jmsMessageId = response.getJMSMessageID();
                        controller.appendLoggerText("Ответное сообщение успешно отправлено!\nJMSMessageId = " + jmsMessageId + "\n");
                        queueDummySender.close();
                    }
                }
                catch (JMSException ex){
                    controller.appendLoggerText("Возникла ошибка ресивера\n");
                    controller.appendLoggerText(ex.toString());
                }
            }
        }
    }
}

package jms;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import javafx.scene.control.TextArea;

import javax.jms.*;

public class JMSManager {

    private QueueConnection queueConnection;    // подключение к шине
    private QueueSession queueSession;          // сессия
    private QueueSender queueSender;            // сендер сообщений во входящую очередь
    private QueueReceiver queueReceiver;        // ресивер сообщений исходящей очереди
    private QueueSender queueDummySender;
    private QueueReceiver queueDummyReceiver;

    private boolean connectionIsActive;
    private boolean dummyStop = true;
    private TextArea outTextArea;
    private TextArea loggerTextArea;

    /**
     * Constructor
     * @param outTextArea       reference on textArea for print receiving message
     * @param loggerTextArea    reference on textArea for print errors and other information
     */
    public JMSManager(TextArea outTextArea, TextArea loggerTextArea){
        this.outTextArea = outTextArea;
        this.loggerTextArea = loggerTextArea;
    }

    /**
     * Method for set connection to ESB
     * @param host              host
     * @param port              port
     * @param channel           channel
     * @param queueManager      queueManager
     * @return                  success flag
     */
    public boolean setConnection(String host, String port, String channel, String queueManager){

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
            connectionIsActive = true;
            return true;

        } catch (JMSException e) {
            System.out.println("Ошибка подлючения к менеджеру очередей");
            loggerTextArea.setText(e.toString());
            return false;
        }
    }

    public void sendMessage(String xmlRequest, String inputQueue, String outputQueue){
        new Sender(xmlRequest, inputQueue, outputQueue);
    }

    public void runDummy(String dummyXML, String inputQueue, String outputQueue){
        this.dummyStop = false;
        new Dummy(dummyXML, inputQueue, outputQueue);
    }

    public void stopDummy(){
        this.dummyStop = true;
    }

    public boolean dummyIsRunning(){
        return !dummyStop;
    }

    public void closeCurrentConnection(){
        try {
            queueSession.close();
            queueConnection.close();
        } catch (JMSException e) {
            e.printStackTrace();
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
                queueSender = queueSession.createSender(queueSession.createQueue(inputQueue));
                TextMessage textMessage = queueSession.createTextMessage(xmlRequest);
                queueSender.send(textMessage);
                jmsMessageId = textMessage.getJMSMessageID();
                queueSender.close();

            } catch (JMSException e) {
                loggerTextArea.appendText("Не удалось отправить сообщение в очередь " + inputQueue + "\n    StackTrace:\n");
                loggerTextArea.appendText(e.toString());
            }

            try{
                queueReceiver = queueSession.createReceiver(queueSession.createQueue(outputQueue),
                        "JMSCorrelationID = '" + jmsMessageId + "'");
                TextMessage response = (TextMessage) queueReceiver.receive(20000);
                responseXml = response.getText();
                outTextArea.setText(responseXml);
            }
            catch (JMSException ex){
                System.out.println("Не удалось получить сообщение из очереди " + outputQueue);
                loggerTextArea.appendText("Не удалось получить сообщение из очереди " + outputQueue + "\n    StackTrace:\n");
                loggerTextArea.appendText(ex.toString());
                outTextArea.setText("Не удалось получить сообщение из ответной очереди");
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
            while(!dummyStop){
                try{
                    queueDummyReceiver = queueSession.createReceiver(queueSession.createQueue(inputQueue));
                    TextMessage request = (TextMessage) queueDummyReceiver.receive(2000);
                    if(request != null){
                        String jmsMessageID = request.getJMSMessageID();
                        String xmlRequest = request.getText();
                        loggerTextArea.appendText("JMSMessageId = " + jmsMessageID + "\n");
                        outTextArea.setText(xmlRequest);

                        queueDummySender = queueSession.createSender(queueSession.createQueue(outputQueue));
                        TextMessage response = queueSession.createTextMessage(dummyXML);
                        response.setJMSCorrelationID(jmsMessageID);
                        queueDummySender.send(response);

                        String jmsMessageId = response.getJMSMessageID();
                        loggerTextArea.appendText("Ответное сообщение успешно отправлено!\nJMSMessageId = " + jmsMessageId + "\n");
                        queueDummySender.close();
                    }
                }
                catch (JMSException ex){
                    loggerTextArea.appendText("Возникла ошибка ресивера\n");
                    loggerTextArea.appendText(ex.toString());
                    ex.printStackTrace();
                }
            }
        }
    }
}

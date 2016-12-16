package pt.gov.dgrm.smsgatebox.rabbit;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang.SerializationUtils;
import pt.gov.dgrm.smsgatebox.models.SMSNotification;
import pt.gov.dgrm.smsgatebox.models.SMSReceiver;
import pt.gov.dgrm.smsgatebox.utils.CommonConfig;

/**
 * Responsável pela comunicação com o RabbitMQ
 * @author Altran
 */
public class Utils {
    
    protected static Connection connection = null;
    private static final Properties props = CommonConfig.init();
    
    /**
     * Vai buscar ao RabbitMQ as mensagens recebidas entre duas datas
     *
     * @param dtini data de inicio
     * @param dtfim data de fim
     * @return HTTP Status-Code as response.
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     * @throws java.lang.InterruptedException
     */ 
    public static List<SMSReceiver> getSMSReceived(Date dtini, Date dtfim) throws IOException, TimeoutException, InterruptedException {
        List<SMSReceiver> myList = new ArrayList<>();
        SMSReceiver received = null;
        
        Channel channel = Utils.getChannel();

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(props.getProperty("RECEIVED_QUEUE"), false, consumer);
        
        long timeout = 1;
        
        boolean flag = true;
        while(flag) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeout);
            if (delivery == null || !channel.isOpen()){
                flag=false;
            } else {
                if(delivery.getProperties().getTimestamp().after(dtini) && delivery.getProperties().getTimestamp().before(dtfim)) {
                    received = (SMSReceiver) SerializationUtils.deserialize(delivery.getBody());
                    myList.add(received);
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println(" [x] Received SMS fetch from Rabbit ");
                } else {
                    flag=false;
                }
            }
        }
        closeChannel(channel);
        return myList;
    }
    
    /**
     * Vai buscar ao RabbitMQ as notificações de entrega de uma SMS
     *
     * @param uuid identificador único da mensagem
     * @return HTTP Status-Code as response.
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     * @throws java.lang.InterruptedException
     */ 
    public static SMSNotification getNotification(String uuid) throws IOException, TimeoutException, InterruptedException {
        
        SMSNotification notif = null;
        
        Channel channel = Utils.getChannel();

        QueueingConsumer consumer = new QueueingConsumer(channel);
        channel.basicConsume(props.getProperty("NOTIFICATION_QUEUE"), false, consumer);
        
        long timeout = 1;
        boolean flag = true;
        while(flag) {
            QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeout);
            if (delivery == null || !channel.isOpen()){
                flag=false;
            } else {
                if(delivery.getProperties().getMessageId().equals(uuid)) {
                    notif = (SMSNotification) SerializationUtils.deserialize(delivery.getBody());    
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                    System.out.println(" [x] Notification fetch from Rabbit ");
                    break;
                } 
            }
        }
        closeChannel(channel);
        return notif;
    }
    
    /**
     * Abre um canal para o RabbitMQ utilizando uma ligação já existente
     *
     * @return channel que foi criado
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */ 
    public static Channel getChannel() throws IOException, TimeoutException {
        connection = getConnection();
        Channel channel = connection.createChannel();
        return channel;
    }
    
    /**
     * Cria uma ligação para o RabbitMQ
     *
     * @return connection que foi criada
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */     
    public static Connection getConnection() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        if (connection==null || !connection.isOpen()) connection = factory.newConnection();
        return connection;
    }

    /**
     * Coloca no RabbitMQ as notificações de entrega de uma SMS
     *
     * @param sms recebida
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */ 
    public static void sendToNotifications(SMSNotification sms) throws IOException, TimeoutException{
        
       connection = getConnection();
       Channel channel = getChannel();
       channel.queueDeclare(props.getProperty("NOTIFICATION_QUEUE"), true, false, false, null);
       
       byte[] data = SerializationUtils.serialize(sms);
       AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
       propsBuilder.deliveryMode(2);
       propsBuilder.messageId(sms.getUuid());
       
       channel.basicPublish("", props.getProperty("NOTIFICATION_QUEUE"), propsBuilder.build(), data);
       System.out.println(" [x] Notification Sended to Rabbit ");

       closeChannel(channel);
       closeConnection(connection);
    }
    
     /**
     * Coloca no RabbitMQ as SMS recebidas
     *
     * @param sms recebida
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */ 
    public static void sendToReceived(SMSReceiver sms) throws IOException, TimeoutException{
        
        connection = getConnection();
        Channel channel = getChannel();
        channel.queueDeclare(props.getProperty("RECEIVED_QUEUE"), true, false, false, null);

        byte[] data = SerializationUtils.serialize(sms);
        AMQP.BasicProperties.Builder propsBuilder = new AMQP.BasicProperties.Builder();
        propsBuilder.deliveryMode(2);
        propsBuilder.messageId(sms.getUuid());
        propsBuilder.timestamp(sms.getTime());
        
        channel.basicPublish("", props.getProperty("RECEIVED_QUEUE"), propsBuilder.build(), data);
        System.out.println(" [x] Received SMS Sended to Rabbit ");

        closeChannel(channel);
        closeConnection(connection);
    }
    
    /**
     * Fecha um canal do RabbitMQ
     *
     * @param channel a fechar
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */ 
    public static void closeChannel(Channel channel) throws IOException, TimeoutException {
        channel.close();
    }
    
    /**
     * Fecha uma ligação do RabbitMQ
     *
     * @param conn a fechar
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     */   
    public static void closeConnection(Connection conn) throws IOException, TimeoutException {
        conn.close();
    }
    
}

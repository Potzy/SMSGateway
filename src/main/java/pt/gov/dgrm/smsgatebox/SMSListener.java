package pt.gov.dgrm.smsgatebox;

import java.io.BufferedReader;
import javax.ws.rs.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.HttpStatus;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import static javax.ws.rs.core.HttpHeaders.USER_AGENT;
import javax.ws.rs.core.MediaType;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import javax.ws.rs.core.Response;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import pt.gov.dgrm.smsgatebox.models.*;
import pt.gov.dgrm.smsgatebox.rabbit.Utils;
import pt.gov.dgrm.smsgatebox.utils.CommonConfig;

/**
 * Endpoint REST
 * @author Altran
 */
@Path("/")
public class SMSListener {
    
    private static final SimpleDateFormat WS_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat DT_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    private static final SimpleDateFormat NOTIF_DATE_FORMAT          = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    private static final Properties props = CommonConfig.init();
    
    /**
     * Trata os pedidos POST das mensagens a receber
     *
     * @param user utilizador do endpoint
     * @param pass password do user do endpoint
     * @param uuid identificador único
     * @param gateway gateway usar
     * @param recipient número de telefone de destino (MEO)
     * @param sender número de telemóvel de origem 
     * @param timestamp data/hora receção
     * @param message texto recebido
     * @return HTTP Status-Code as response.
     * @throws java.io.IOException
     * @throws java.util.concurrent.TimeoutException
     * @throws java.text.ParseException
     */
    @POST
    @Path("receive")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response receiveSMS(@FormParam("login") String user,
                               @FormParam("password") String pass,
                               @FormParam("uniqueId") String uuid,
                               @FormParam("gateway") String gateway,
                               @FormParam("recipient") String recipient,
                               @FormParam("sender") String sender,
                               @FormParam("smscTimestamp") String timestamp,
                               @FormParam("message") String message)  throws IOException, TimeoutException, ParseException {

        try {            
            //Verifica nullos, retorna HTTP 400 se não encontrar um atributo
            if (user==null || user.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: login").build();
            }
            if (pass==null || pass.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: password").build();
            }
            if (uuid==null || uuid.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: uniqueId").build();
            }
            if (gateway==null || gateway.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: gateway").build();
            }
            if (recipient==null || recipient.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: recipient").build();
            }
            if (sender==null || sender.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: sender").build();
            }
            if (timestamp==null || timestamp.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: smscTimestamp").build();
            }
            if (message==null || message.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: message").build();
            }

            Date time = new Date (Long.parseLong(timestamp) * 1000); // * 1000 porque o java espera milisegundos
            //Format format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            //Inicializar a classe
            SMSReceiver receiv = new SMSReceiver(user, pass, uuid, gateway, recipient, sender, time, message);

            if (receiv.getUsername().equalsIgnoreCase(props.getProperty("LOGIN")) && receiv.getPassword().equalsIgnoreCase(props.getProperty("PASSWORD"))){

                Utils.sendToReceived(receiv);

                return Response.ok().status(HttpStatus.SC_OK).type(MediaType.APPLICATION_FORM_URLENCODED).entity("SUCCESS").build();
            }else {
                return Response.ok().status(HttpStatus.SC_FORBIDDEN).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Autenticação Inválida").build();
            }
        } catch  (IOException | TimeoutException e) {
            System.out.println("ERROR: " + e + " at: " + this.getClass().getName());
        }
        return Response.ok().status(HttpStatus.SC_INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Error! Please check logs for details").build();
    } 
    
    /**
     * Trata os pedidos POST para as notificações de entrega das mensagens enviadas
     * 
     * @param user utilizador do endpoint
     * @param pass password do user do endpoint
     * @param guid identificador único do grupo
     * @param status estado da mensagem
     * @param uniqueId identificador único da SMS
     * @param entregue data/hora de entrega
     * @return HTTP Status-Code as response.
     */
    @POST
    @Path("notify/deliver_report")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response getSMSNotification(@FormParam("login") String user, 
                                       @FormParam("password") String pass,
                                       @FormParam("guid") String guid,
                                       @FormParam("uuid") String uniqueId,
                                       @FormParam("status") String status,
                                       @FormParam("deliveredAt") String entregue) {
        
        try {
            //Verifica nullos, retorna HTTP 400 se não encontrar um atributo
            if (user==null || user.equals("")){   
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: login").build();
            }
            if (pass==null || pass.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: password").build();
            }
            if (uniqueId==null || uniqueId.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: uniqueId").build();
            }
            if (status==null || status.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: status").build();
            }
            if (entregue==null || entregue.equals("")){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("MISSING ATTRIBUTE: deliveredAt").build();
            }

            //Obter os valores presentes no header
            Date deliveredAt = NOTIF_DATE_FORMAT.parse(entregue);


            //Inicializar a classe
            SMSNotification notif = new SMSNotification(user, pass, uniqueId, status, deliveredAt);


            if (notif.getUsername().equalsIgnoreCase(props.getProperty("LOGIN")) && notif.getPassword().equalsIgnoreCase(props.getProperty("PASSWORD"))){

                Utils.sendToNotifications(notif);

                return Response.ok().status(HttpStatus.SC_OK).type(MediaType.APPLICATION_FORM_URLENCODED).entity("SUCCESS").build();
            }else {
                return Response.ok().status(HttpStatus.SC_FORBIDDEN).type(MediaType.APPLICATION_FORM_URLENCODED).entity("INVALID AUTENTICATION").build();
            }
        } catch ( IOException | TimeoutException | ParseException e) {
             System.out.println("ERROR: " + e + " at: " + this.getClass().getName());
        }
        return Response.ok().status(HttpStatus.SC_INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Error! Please check the logs for details").build();
    }   
    
    /**
     * Trata os pedidos POST (a gatebox invoca este método) para enviar uma SMS (fazendo uso do método interno restCall)
     *
     * @param requestBody - XML que vem da gatebox, contém as informações necessárias ao pedido
     * @return HTTP Status-Code e a sua resposta associada.
     */
    @POST
    @Path("send")
    public Response sendSMS(InputStream requestBody) {
        try {
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(requestBody));
            StringBuilder out = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }
    
            String result = restCall(out.toString());
     
            //Tratar erros HTTP mais comuns...
            switch (result) {                
                case "400": return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                case "401": return Response.ok().status(HttpStatus.SC_UNAUTHORIZED).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                case "403": return Response.ok().status(HttpStatus.SC_FORBIDDEN).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                case "404": return Response.ok().status(HttpStatus.SC_NOT_FOUND).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                case "500": return Response.ok().status(HttpStatus.SC_INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                default: return Response.ok().status(HttpStatus.SC_OK).type(MediaType.APPLICATION_FORM_URLENCODED).entity(result).build();
            }  
        } catch (IOException e) {
             System.out.println("ERROR: " + e + " at: (internal) sendSMS");
        }
        return null;
    } 
    
    /**
     * Trata os pedidos GET (a gatebox invoca este método) para ir buscar todas as mensagens recebidas entre duas datas
     *
     * @param headers (dtini, dtfim)
     * @return HTTP Status-Code e a sua resposta associada.
     */
    @GET
    @Path("get/received")
    public Response getIncomingSMS(@Context HttpHeaders headers) {
        try {
            
            if (headers.getRequestHeader("dtini")==null || headers.getRequestHeader("dtini").isEmpty()){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Missing Header Attribute: Data Início").build();
            }

            if (headers.getRequestHeader("dtfim")==null || headers.getRequestHeader("dtfim").isEmpty()){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Missing Header Attribute: Data Fim").build();
            }

            String strInicial = headers.getRequestHeader("dtini").get(0);
            String strFinal = headers.getRequestHeader("dtfim").get(0);

            Date dtini = WS_DATE_FORMAT.parse(strInicial);
            Date dtfim = WS_DATE_FORMAT.parse(strFinal);

            Timestamp tsini = new java.sql.Timestamp(dtini.getTime());
            Timestamp tsfin = new java.sql.Timestamp(dtfim.getTime());
            
            //Date dtini= new Date(Long.parseLong(headers.getRequestHeader("dtini").get(0))* 1000);
            //Date dtfim=new Date(Long.parseLong(headers.getRequestHeader("dtfim").get(0))* 1000);

            List<SMSReceiver> receivedSMS = Utils.getSMSReceived(tsini, tsfin);

            SMSReceiver mySMS = new SMSReceiver(); 

            if (receivedSMS!=null && receivedSMS.size()>0){
                StringBuilder sb = new StringBuilder();
                sb.append("<getClientDirectResponsesResponse>\n");
                sb.append("\t<clientDirectResponses>\n");
                for (int i = 0; i < receivedSMS.size(); i++) {
                   mySMS = receivedSMS.get(i);
                   String date = DT_FORMAT.format(mySMS.getTime());
                   String myString = "\t\t<ClientDirectResponses>\n"+
                                        "\t\t\t<text>" + mySMS.getMessage()+"</text>\n" +
                                        "\t\t\t<date>" + date +"</date>\n" +
                                        "\t\t\t<originator>" + mySMS.getRecipient()+"</originator>\n"+
                                    "\t\t</ClientDirectResponses>\n";
                   sb.append(myString);
                }
                sb.append("\t</clientDirectResponses>\n");
                sb.append("\t<nextPage>STOP</nextPage>\n");
                sb.append("</getClientDirectResponsesResponse>");

                return Response.ok().status(HttpStatus.SC_OK).type(MediaType.APPLICATION_FORM_URLENCODED).entity(sb.toString()).build();

            }else {   
                return Response.ok().status(HttpStatus.SC_NOT_FOUND).type(MediaType.APPLICATION_FORM_URLENCODED).entity("<getClientDirectResponsesResponse/>").build();
            }
        } catch (NumberFormatException | IOException | TimeoutException | InterruptedException | ParseException e) {
             System.out.println("ERROR: " + e + " at: " + this.getClass().getName());
        }
        return Response.ok().status(HttpStatus.SC_INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Error! Please check the logs for details").build();
    } 
       
    /**
     * Trata os pedidos GET que verificam o estado de uma mensagem, primeiro vai ao rabbit, se não encontrar invoca diretamente a AMA
     *
     * @param uid
     * @return HTTP Status-Code as response.
     */
    @GET
    @Path("get/status/{id}")
    public Response checkSMS(@PathParam("id") String uid) {
        try {
            
            if (uid==null || uid==""){
                return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Missing Header Attribute: uuid").build();
            }

            SMSNotification notif = Utils.getNotification(uid);

            if (notif!=null){

                StringBuilder sb = new StringBuilder();
                String myString = "\t<message>\n"+
                                        "\t\t<submissionId>" + notif.getUuid()+"</submissionId>\n" +
                                        "\t\t<endDate>" + notif.getDeliveredAt()+"</endDate>\n" +
                                        "\t\t<state>" + notif.getStatus()+"</state>\n"+
                                    "\t</message>\n";
                sb.append(myString);

                return Response.ok().status(HttpStatus.SC_OK).type(MediaType.APPLICATION_FORM_URLENCODED).entity(sb.toString()).build();
                
            }else {
                String result = getSMSbyID(uid);
                switch (result) {
                    case "400": return Response.ok().status(HttpStatus.SC_BAD_REQUEST).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                    case "401": return Response.ok().status(HttpStatus.SC_UNAUTHORIZED).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                    case "403": return Response.ok().status(HttpStatus.SC_FORBIDDEN).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                    case "404": return Response.ok().status(HttpStatus.SC_NOT_FOUND).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                    case "500": return Response.ok().status(HttpStatus.SC_INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_FORM_URLENCODED).entity("REST Error - Status Code: " + result).build();
                    default: return Response.accepted().status(HttpStatus.SC_OK).type(MediaType.APPLICATION_FORM_URLENCODED).entity(result).build();
                }
            }
            
        } catch (IOException | TimeoutException | InterruptedException e) {
           System.out.println("ERROR: " + e + " at: " + this.getClass().getName());
        }
        return Response.ok().status(HttpStatus.SC_INTERNAL_SERVER_ERROR).type(MediaType.APPLICATION_FORM_URLENCODED).entity("Error! Please check logs for details").build();
    } 
    
    /**
     * Método interno utilizado pelo getSMS, invoca o endpoint da AMA para determinar o estado de uma SMS (se foi entregue ou não).
     *
     * @param uid - Identificador único da mensagem
     * @return HTTP Status-Code as response.
     */
    protected static String getSMSbyID(String uid) {
        try {

            HttpClient client = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet(props.getProperty("AMACheckSMS")+uid);
            //HttpEntity e = new StringEntity();
            //get.setEntity(e);
            // add header
            get.setHeader("User-Agent", USER_AGENT);
            //make connection

            HttpResponse response = client.execute(get);
            int httpCode = response.getStatusLine().getStatusCode();

            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                    result.append(line);
            }
            if (httpCode == HttpStatus.SC_OK)   return result.toString();
            else return String.valueOf(httpCode);
        } catch (IOException | UnsupportedOperationException e) {
            System.out.println("ERROR: " + e + " at: (internal) getSMSbyID");
        }
        return String.valueOf(HttpStatus.SC_INTERNAL_SERVER_ERROR);
    }
        
    /**
     * Método interno utilizado pelo sendSMS, invoca o endpoint da AMA para enviar uma SMS
     *
     * @param xml - XML que contém as informações necessárias ao pedido (está definido na gatebox)
     * @return HTTP Status-Code e a resposta associada.
     */
    protected static String restCall(String xml){
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpPost post = new HttpPost(props.getProperty("AMASendSMS"));
            HttpEntity e = new StringEntity(xml);
            post.setEntity(e);
            // add header
            post.setHeader("User-Agent", USER_AGENT);
            post.setHeader("Content-Type", TEXT_XML);
            //make connection

            HttpResponse response = client.execute(post);
            int httpCode = response.getStatusLine().getStatusCode();

            
            
            BufferedReader rd = new BufferedReader(
                    new InputStreamReader(response.getEntity().getContent()));

            StringBuilder result = new StringBuilder();
            String line = "";
            while ((line = rd.readLine()) != null) {
                    result.append(line);
            }

            if (httpCode == HttpStatus.SC_OK)   return result.toString();
            else return String.valueOf(httpCode);
        } catch (IOException | UnsupportedOperationException e) {
            System.out.println("ERROR: " + e + " at: (internal) restCall");
        }
        return null;
    }
    
}
package pt.gov.dgrm.smsgatebox.models;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Representa a SMS recebida (operação SMS)
 * @author Altran
 */
public class SMSReceiver implements Serializable {

    public SMSReceiver() {

    }
       
    
    public SMSReceiver(String username, String password, String uuid, String gateway, String recipient, String sender, Date time, String message) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.gateway = gateway;
        this.recipient = recipient;
        this.sender = sender;
        this.time = time;
        this.message = message;
    }
       
    @NotNull
    @Size(max = 160)
    private String username;
    
    @NotNull
    @Size(max = 160)
    private String password;

    @NotNull
    @Size(max = 160)
    private String uuid;
    
    @NotNull
    @Size(max = 160)
    private String gateway;
    
    @NotNull
    @Size(max = 160)
    private String recipient;
 
    @NotNull
    @Size(max = 160)
    private String sender;
     
    @NotNull
    @Size(max = 160)
    private Date time;

    @NotNull
    @Size(max = 160)
    private String message;
    
    /**
    * @return the username
    */  
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */  
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the uuid
     */  
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the gateway
     */  
    public String getGateway() {
        return gateway;
    }

    /**
     * @param gateway the gateway to set
     */
    public void setGateway(String gateway) {
        this.gateway = gateway;
    }

    /**
     * @return the recipient
     */  
    public String getRecipient() {
        return recipient;
    }

    /**
     * @param recipient the recipient to set
     */
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    /**
     * @return the sender
     */  
    public String getSender() {
        return sender;
    }

    /**
     * @param sender the sender to set
     */
    public void setSender(String sender) {
        this.sender = sender;
    }

    /**
     * @return the time
     */  
    public Date getTime() {
        return time;
    }

    /**
     * @param time the time to set
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @return the message
     */  
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }
    
}

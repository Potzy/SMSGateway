package pt.gov.dgrm.smsgatebox.models;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Representa a SMS enviada 
 * @author Altran
 */

public class SMSSender {
    
 
    public SMSSender(String username, String password, String recipient, String sender, String message) {
        this.username = username;
        this.password = password;
        this.recipient = recipient;
        this.sender = sender;
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
    private String recipient;
 
    @NotNull
    @Size(max = 160)
    private String sender;
     
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

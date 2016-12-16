package pt.gov.dgrm.smsgatebox.models;

import java.io.Serializable;
import java.util.Date;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * Representa a SMS recebida (notificação de entrega)
 * @author Altran
 */
public class SMSNotification implements Serializable {
           
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
    private String status;
    
    @NotNull
    @Size(max = 160)
    private Date deliveredAt;

    public SMSNotification(String username, String password, String uuid, String status, Date deliveredAt) {
        this.username = username;
        this.password = password;
        this.uuid = uuid;
        this.status = status;
        this.deliveredAt = deliveredAt;
    }
       
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
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the deliveredAt
     */
    public Date getDeliveredAt() {
        return deliveredAt;
    }

    /**
     * @param deliveredAt the deliveredAt to set
     */
    public void setDeliveredAt(Date deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}

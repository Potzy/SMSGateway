package pt.gov.dgrm.smsgatebox.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Responsável por inicializar o ficheiro de configurações
 * @author Altran
 */
public class CommonConfig {
  
    private static final String fileName = "config.properties";

    public static Properties init(){
        
        Properties properties = new Properties();
	InputStream in = null;
                
	try {
                in = CommonConfig.class.getClassLoader().getResourceAsStream(fileName);
                properties.load(in);
                in.close();

	} catch (IOException ex) {
            System.out.println("ERROR: " + ex + " at: " + CommonConfig.class.getName()); 
        }
        return properties;
    }
}

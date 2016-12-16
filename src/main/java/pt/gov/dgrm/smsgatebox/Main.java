package pt.gov.dgrm.smsgatebox;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.glassfish.jersey.server.ServerProperties;
import pt.gov.dgrm.smsgatebox.utils.CommonConfig;

/**
 * Responsável por iniciar o servidor HTTP
 * @author Altran
 */
@ApplicationPath("sms")
public class Main extends Application{ 
    
       
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://localhost:8080/";    
    private static final Properties props = CommonConfig.init();
    
    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // create a resource config that scans for JAX-RS resources and providers
        final ResourceConfig rc = new ResourceConfig().packages("pt.gov.dgrm.gatebox").
                property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true).register(CORSResponseFilter.class);
        
        // create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method.
     * @param args 
     * @throws IOException
     * @throws java.util.concurrent.TimeoutException
     */
    public static void main(String[] args) throws IOException, TimeoutException {
        final HttpServer server = startServer();
        
        System.out.println(String.format("JAX-WS Online: %sapplication.wadl\n", BASE_URI));

        System.in.read();
        server.stop();
        
       
  }
   
}



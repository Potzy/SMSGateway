package pt.gov.dgrm.smsgatebox;

import java.io.IOException;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;

/**
 * CORSResponseFilter
 * @author Altran
 */

public class CORSResponseFilter implements ContainerResponseFilter {

        /*
	 * (non-Javadoc)
	 * 
	 * @see
	 * javax.ws.rs.container.ContainerResponseFilter#filter(javax.ws.rs.container
	 * .ContainerRequestContext, javax.ws.rs.container.ContainerResponseContext)
	 */
        @Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {

		MultivaluedMap<String, Object> headers = responseContext.getHeaders();
            
		headers.add("Access-Control-Allow-Origin", "*");		
		headers.add("Access-Control-Allow-Methods", "*");			
		headers.add("Access-Control-Allow-Headers:", "Origin, Content-Type, X-Auth-Token , Authorization");
	}

}
package pt.gov.dgrm.smsgatebox;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Captura a exceção através de mapeamento no envio um pedido REST
 * @author Altran
 */
@Provider
public class DebugExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        return Response.serverError().entity(exception.getMessage()).build();
    } 
}
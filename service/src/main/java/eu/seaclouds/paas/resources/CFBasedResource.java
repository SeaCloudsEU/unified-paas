package eu.seaclouds.paas.resources;

import java.util.List;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.seaclouds.paas.Credentials;
import eu.seaclouds.paas.Module;
import eu.seaclouds.paas.PaasClient;
import eu.seaclouds.paas.PaasSession;
import eu.seaclouds.paas.ServiceApp;


public abstract class CFBasedResource extends PaaSResource
{
	
	
	private static Logger log = LoggerFactory.getLogger(CFBasedResource.class);
	

	/**
	 * 
	 * @param client
	 */
	public CFBasedResource(PaasClient client)
	{
		super(client);
	}

	
	@PUT
	@Path("/applications/{name}/bind/{service}")
	@Override
	public Response bindApplication(@PathParam("name") String name, @PathParam("service") String service, @Context HttpHeaders headers)
	{
		log.info("bindApplication({}, {})", name, service);
		Credentials credentials = extractCredentials(headers);
		if (credentials == null) {
			// Error Response
			return generateCredentialsErrorJSONResponse("PUT /applications/" + name + "/bind/" + service);
		}
		
		PaasSession session = client.getSession(credentials);
		
		Module m = session.getModule(name);
		
		// cloud foundry ... cleardb:mycleardb:spark
		String[] servValues = service.split(":");
		
		if (servValues.length == 3)
		{
			ServiceApp serviceapp = new ServiceApp(servValues[0]);
			serviceapp.setServiceInstanceName(servValues[1]);
			serviceapp.setServicePlan(servValues[2]);
	    	
	        session.bindToService(m, serviceapp);
		}
		else
		{
			// Response
		    return generateJSONResponse(Response.Status.BAD_REQUEST, OperationResult.ERROR,
									    "PUT /applications/" + name + "/bind/" + service, 
									    "Credentials not found in request headers / lenght != 3");
		}

		// Response
	    return generateJSONResponse(Response.Status.OK, OperationResult.OK,
								    "PUT /applications/" + name + "/bind/" + service, 
								    "service " + service + " binded to app: " + name);
	}
	
	
	@PUT
	@Path("/applications/{name}/unbind/{service}")
	@Override
	public Response unbindApplication(@PathParam("name") String name, @PathParam("service") String service, @Context HttpHeaders headers)
	{
		log.info("unbindApplication({}, {})", name, service);
		Credentials credentials = extractCredentials(headers);
		if (credentials == null) {
			// Error Response
			return generateCredentialsErrorJSONResponse("PUT /applications/" + name + "/unbind/" + service);
		}
		
		PaasSession session = client.getSession(credentials);
		
		Module m = session.getModule(name);
		
		// cloud foundry ... cleardb:mycleardb:spark
		String[] servValues = service.split(":");
		
		if (servValues.length == 3)
		{
			ServiceApp serviceapp = new ServiceApp(servValues[0]);
			serviceapp.setServiceInstanceName(servValues[1]);
			serviceapp.setServicePlan(servValues[2]);
	    	
	        session.unbindFromService(m, serviceapp);
		}
		
		// Response
	    return generateJSONResponse(Response.Status.OK, OperationResult.OK,
								    "PUT /applications/" + name + "/unbind/" + service, 
								    "service " + service + " unbinded from app: " + name);
	}


	@Override
	protected Credentials extractCredentials(HttpHeaders headers)
	{
		Credentials credentials = null;
		
		log.debug("Checking credentials [CloudFoundry] ...");

		List<String> crs = headers.getRequestHeader("credentials");
		if (crs != null && !crs.isEmpty() && crs.size()==6)
		{
			credentials = new Credentials.ApiUserPasswordOrgSpaceCredentials(
					crs.get(0), crs.get(1), crs.get(2), crs.get(3), crs.get(4), Boolean.valueOf(crs.get(5)));
		}

		return credentials;
	}
	

}

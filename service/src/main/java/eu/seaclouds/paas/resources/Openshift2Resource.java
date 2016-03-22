package eu.seaclouds.paas.resources;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openshift.client.cartridge.IStandaloneCartridge;
import eu.seaclouds.paas.Credentials;
import eu.seaclouds.paas.Module;
import eu.seaclouds.paas.PaasClient;
import eu.seaclouds.paas.PaasSession;
import eu.seaclouds.paas.ServiceApp;
import eu.seaclouds.paas.data.Application;
import eu.seaclouds.paas.openshift2.DeployParameters;


@Path("/openshift2")
public class Openshift2Resource extends PaaSResource
{
	
	
	private static Logger log = LoggerFactory.getLogger(Openshift2Resource.class);

	
	/**
	 * 
	 * Constructor
	 * @param client
	 */
	public Openshift2Resource(PaasClient client)
	{
		super(client);
	}
	
	
	@POST
	@Path("/applications")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.APPLICATION_JSON)
	@Override
	public Response createApplication(@Context HttpHeaders headers, FormDataMultiPart form)
	{
		throw new WebApplicationException("method not implemented", Response.Status.BAD_REQUEST);
	}

	
	@POST
	@Path("/applications/git")
	@Produces(MediaType.APPLICATION_JSON)
	public Response createApplication2(@Context HttpHeaders headers, @FormParam("appName") String appname, 
			@FormParam("appGitUrl") String appGitUrl)
	{
		try
		{
			Credentials credentials = extractCredentials(headers);
			if (credentials == null) {
				// Error Response
				return generateCredentialsErrorJSONResponse("POST /applications");
			}
			
			log.info("createApplication({})", appname);
			
			PaasSession session = client.getSession(credentials);

			Application result;
			try {
				Module m = session.deploy(appname, new DeployParameters(appGitUrl, IStandaloneCartridge.NAME_JBOSSEWS));
				result = new Application(m.getName(), new URL(m.getUrl()));
			}
			catch (IOException e)
			{
				throw new WebApplicationException(e);
			}
			
			// Response
		    return generateJSONResponse(Response.Status.OK, OperationResult.OK,
									    "POST /applications/",
									    "application " + result.getName() + " created / deployed: " + result.getUrl());
		}
		catch (Exception e)
		{
			// Response
		    return generateJSONResponse(Response.Status.INTERNAL_SERVER_ERROR, OperationResult.ERROR,
									    "POST /applications/",
									    "application not created / deployed: " + e.getMessage());
		}
	}
	
	
	@PUT
	@Path("/applications/{name}/bind/{service}")
	@Override
	public Response bindApplication(String name, String service, HttpHeaders headers)
	{
		log.info("bindApplication({}, {})", name, service);
		Credentials credentials = extractCredentials(headers);
		if (credentials == null) {
			// Error Response
			return generateCredentialsErrorJSONResponse("PUT /applications/" + name + "/bind/" + service);
		}
		
		PaasSession session = client.getSession(credentials);
		
		Module m = session.getModule(name);
		// openshift ... mysql-5.5
        session.bindToService(m, new ServiceApp(service));

		// Response
	    return generateJSONResponse(Response.Status.OK, OperationResult.OK,
								    "PUT /applications/" + name + "/bind/" + service, 
								    "service " + service + " binded to app: " + name);
	}
	

	@PUT
	@Path("/applications/{name}/unbind/{service}")
	@Override
	public Response unbindApplication(String name, String service, HttpHeaders headers)
	{
		log.info("unbindApplication({}, {})", name, service);
		Credentials credentials = extractCredentials(headers);
		if (credentials == null) {
			// Error Response
			return generateCredentialsErrorJSONResponse("PUT /applications/" + name + "/unbind/" + service);
		}
		
		PaasSession session = client.getSession(credentials);
		
		Module m = session.getModule(name);
		// openshift ... mysql-5.5
        session.unbindFromService(m, new ServiceApp(service));

		// Response
	    return generateJSONResponse(Response.Status.OK, OperationResult.OK,
								    "PUT /applications/" + name + "/unbind/" + service, 
								    "service " + service + " unbinded from app: " + name);
	}

	
	@Override
	protected Credentials extractCredentials(HttpHeaders headers)
	{
		Credentials credentials = null;
		
		log.debug("Checking credentials [Openshift2] ...");

		List<String> crs = headers.getRequestHeader("credentials");
		if (crs != null && !crs.isEmpty() && crs.size()==2)
		{
			credentials = new Credentials.UserPasswordCredentials(crs.get(0), crs.get(1));
		}

		return credentials;
	}

	
}

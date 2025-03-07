
package io.openliberty.sample.system;

import javax.inject.Inject;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/map")
public class MapResource {

	@Inject
	@org.eclipse.microprofile.config.inject.ConfigProperty(name = "MY_POD_NAME",defaultValue = "null_pod")
	private String pod_name;

	@Inject
	MapManager mapManager;

	@GET
	@Path("/get")
	@Produces(MediaType.TEXT_PLAIN)
	public String getValue(@QueryParam("key") String key) {
		String val = mapManager.get(key);
		return val + " from " + pod_name + "\n";
	}

	@GET
	@Path("/put")
	@Produces(MediaType.TEXT_PLAIN)
	public void putValue(@QueryParam("key") String key, @QueryParam("value") String value) {
		mapManager.put(key,value);
	}


}

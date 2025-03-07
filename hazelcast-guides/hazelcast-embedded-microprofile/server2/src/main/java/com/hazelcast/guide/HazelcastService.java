package com.hazelcast.guide;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

@RequestScoped
@Path("/map")
public class HazelcastService {

    @Inject
    HazelcastManager hazelcastManager;

    @GET
    @Path("/get")
    @Produces(MediaType.TEXT_PLAIN)
    public String getValue(@QueryParam("key") Integer key) {
        return hazelcastManager.get(key);
    }

    @GET
    @Path("/list")
    @Produces(MediaType.TEXT_PLAIN)
    public String list() {
        return hazelcastManager.list();
    }

    @PUT
    @Path("/put")
    @Produces(MediaType.TEXT_PLAIN)
    public String putValue(@QueryParam("key") Integer key, @QueryParam("value") String value) {
        return hazelcastManager.put(key,value);
    }

}

package com.socialcomputing.labs.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/labs")
public class LabsRest {
    private static final Logger LOG = LoggerFactory.getLogger(LabsRest.class);

    @GET
    @Produces("text/plain")
    public String sayHello() {
        LOG.debug("hello world service called");
        return "Hello World";
    }
}

package org.acme;
import java.io.IOException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import org.jboss.resteasy.reactive.RestResponse;
import org.json.JSONObject;

@Path("/words")
public class WordResource {

    @Inject WordService wordService;

    @Path("/check/{word}")
    @GET
    public RestResponse<JSONObject> check(@PathParam String word) throws IOException {
        return wordService.check(word);
    }

    @Path("/solution")
    @GET
    public RestResponse<JSONObject> solution() throws IOException {
        return wordService.solution();
    }
}
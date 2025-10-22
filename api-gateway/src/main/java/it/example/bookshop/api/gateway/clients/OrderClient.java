package it.example.bookshop.api.gateway.clients;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "ord")
@Path("/orders")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface OrderClient {

    @GET
    List<Map<String, Object>> list(@QueryParam("userId") String userId, @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size);

    @GET
    @Path("/{id}")
    String get(@PathParam("id") Long id);

    @POST
    Map<String, Object> create(Map<String, Object> createOrderRequest);

    @POST
    @Path("/{id}/cancel")
    String cancel(@PathParam("id") Long id);

    @POST
    @Path("/{id}/mark-paid")
    String markPaid(@PathParam("id") Long id);

    @POST
    @Path("/{id}/fulfill")
    String fulfill(@PathParam("id") Long id);
}

package it.example.bookshop.api.gateway.clients;

import java.util.List;
import it.example.bookshop.common.dto.AdjustRequest;
import it.example.bookshop.common.dto.InventoryItem;
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

@RegisterRestClient(configKey = "inv")
@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface InventoryClient {

    @GET
    List<InventoryItem> list(@QueryParam("page") @DefaultValue("0") int page,
                             @QueryParam("size") @DefaultValue("50") int size);

    @GET
    @Path("/{isbn}")
    InventoryItem get(@PathParam("isbn") String isbnCode);

    @POST
    @Path("/{isbn}/adjust")
    InventoryItem adjust(@PathParam("isbn") String isbnCode, AdjustRequest req);
}
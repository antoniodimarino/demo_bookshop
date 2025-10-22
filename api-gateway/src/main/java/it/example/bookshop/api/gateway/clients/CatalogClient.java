package it.example.bookshop.api.gateway.clients;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import it.example.bookshop.common.dto.BookUpsert;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "cat")
@Path("/catalog")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface CatalogClient {

    @GET
    @Path("/books")
    List<BookUpsert> list(@QueryParam("search") String search, @QueryParam("page") int p, @QueryParam("size") int s);

    @GET
    @Path("/books/{isbn}")
    BookUpsert get(@PathParam("isbn") String isbn);

    @POST
    @Path("/books")
    BookUpsert create(BookUpsert upsert);
}
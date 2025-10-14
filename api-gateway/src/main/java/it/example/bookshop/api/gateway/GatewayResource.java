package it.example.bookshop.api.gateway;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.inject.Inject;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.PathParam;
import java.util.List;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import it.example.bookshop.api.gateway.clients.CatalogClient;

import it.example.bookshop.common.dto.BookUpsert;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GatewayResource {

    @Inject
    @RestClient
    CatalogClient catalog;

    // === Endpoint Pubblici (Catalogo) ===
    @GET
    @Path("/books")
    List<BookUpsert> list(@QueryParam("search") String search, @QueryParam("page") int p, @QueryParam("size") int s) {
        return catalog.list(search, p, s);
    }

    @GET
    @Path("/books/{isbn}")
    public BookUpsert book(@PathParam("isbn") String isbn) {
        return catalog.get(isbn);
    }

    // === Endpoint solo per Amministratori ===
    @POST
    @Path("/books")
    public BookUpsert createBook(BookUpsert req) {
        return catalog.create(req);
    }
}
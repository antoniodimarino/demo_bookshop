package it.example.bookshop.catalog.service.isbn;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(configKey = "isbn-client")
@Path("/isbn")
@Produces(MediaType.APPLICATION_JSON)
public interface IsbnClient {

    @GET
    @Path("/validate/{isbn}")
    ValidationResult validate(@PathParam("isbn") String isbn);

    record ValidationResult(String isbn, boolean valid) {

    }
}

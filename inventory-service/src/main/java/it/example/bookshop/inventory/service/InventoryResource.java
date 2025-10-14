package it.example.bookshop.inventory.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/inventory")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class InventoryResource {

    @Inject
    @RestClient
    IsbnClient isbn;

    /* 
    @GET
    public String ping() {
        return "{\"service\":\"inventory-service\",\"status\":\"ok\"}";
    }
    */

    @GET
    @Path("")
    public java.util.List<InventoryItem> list(@QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {
        return InventoryItem.findAll().page(page, size).list();
    }

    @PUT
    @Transactional
    @Path("/{isbn}")
    public InventoryItem upsert(@PathParam("isbn") String isbnCode, InventoryItem body) {
        InventoryItem item = InventoryItem.find("isbn", isbnCode).firstResult();
        if (item == null) {
            item = new InventoryItem();
            item.isbn = isbnCode;
            item.location = body.location;
            item.quantity = Math.max(0, body.quantity);
        } else {
            if (body.location != null) {
                item.location = body.location;

            }
            item.quantity = Math.max(0, body.quantity);
        }
        item.persist();
        return item;
    }

    @GET
    @Path("/{isbn}")
    public InventoryItem get(@PathParam("isbn") String isbnCode) {
        InventoryItem item = InventoryItem.find("isbn", isbnCode).firstResult();
        if (item == null) {
            throw new NotFoundException();
        }
        return item;
    }

    public record AdjustRequest(int delta, String location) {

    }

    @POST
    @Transactional
    @Path("/{isbn}/adjust")
    public InventoryItem adjust(@PathParam("isbn") String isbnCode, AdjustRequest req) {
        if (!isbn.validate(isbnCode).valid()) {
            throw new BadRequestException("ISBN non valido");
        }
        InventoryItem item = InventoryItem.find("isbn", isbnCode).firstResult();
        if (req == null) {
            throw new BadRequestException("Body mancante");
        }
        if (req.delta() == 0) {
            throw new BadRequestException("Delta deve essere diverso da 0");
        }
        if (item == null && (req.location() == null || req.location().isBlank())) {
            throw new BadRequestException("Location obbligatoria alla creazione");
        }
        if (item == null) {
            item = new InventoryItem();
            item.isbn = isbnCode;
            item.location = req.location();
            item.quantity = 0;
        } else if (req.location() != null && !req.location().isBlank()) {
            item.location = req.location();
        }
        item.quantity += req.delta();
        if (item.quantity < 0) {
            throw new BadRequestException("Quantità negativa");
        }
        item.persist();
        return item;
    }
}

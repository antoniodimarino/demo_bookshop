package it.example.bookshop.api.gateway;

import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import it.example.bookshop.api.gateway.clients.CatalogClient;
import it.example.bookshop.api.gateway.clients.OrderClient;
import it.example.bookshop.api.gateway.clients.PaymentClient;
import it.example.bookshop.api.gateway.clients.UserClient;
import it.example.bookshop.api.gateway.clients.InventoryClient;
import it.example.bookshop.common.dto.AuthRequest;
import it.example.bookshop.common.dto.AuthResponse;
import it.example.bookshop.common.dto.BookUpsert;
import it.example.bookshop.common.dto.RegisterRequest;
import it.example.bookshop.common.dto.UserDTO;
import it.example.bookshop.common.dto.InventoryItem;
import it.example.bookshop.common.dto.AdjustRequest;
import org.eclipse.microprofile.jwt.JsonWebToken;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GatewayResource {

    @Inject
    @RestClient
    CatalogClient catalog;
    @Inject
    @RestClient
    OrderClient orders;
    @Inject
    @RestClient
    PaymentClient payments;
    @Inject
    @RestClient
    UserClient users;
    @Inject
    @RestClient
    InventoryClient inventory;
    @Inject
    JsonWebToken jwt;

    // === Endpoint Pubblici (Catalogo) ===
    @GET
    @Path("/books")
    @PermitAll // Chiunque può vedere i libri
    public List<BookUpsert> books(@QueryParam("search") String search, @QueryParam("page") @DefaultValue("0") int p, @QueryParam("size") @DefaultValue("20") int s) {
        return catalog.list(search, p, s);
    }

    @GET
    @Path("/books/{isbn}")
    @PermitAll // Chiunque può vedere il dettaglio di un libro
    public BookUpsert book(@PathParam("isbn") String isbn) {
        return catalog.get(isbn);
    }

    // === Endpoint di Autenticazione (Pubblici) ===
    @POST
    @Path("/users/register")
    @PermitAll // Chiunque può registrarsi
    public Response register(RegisterRequest req) {
        // Inoltriamo la richiesta e la risposta così com'è
        return users.register(req);
    }

    @POST
    @Path("/users/login")
    @PermitAll // Chiunque può fare il login
    public AuthResponse login(AuthRequest req) {
        return users.login(req);
    }

    // === Endpoint Protetti per Utenti Autenticati ===
    @GET
    @Path("/users/me")
    @RolesAllowed({"CUSTOMER", "ADMIN"}) // Solo utenti autenticati
    public UserDTO getMe(@HeaderParam("Authorization") String authorizationHeader) {
        return users.getMe(authorizationHeader);
    }

    @POST
    @Path("/orders")
    @RolesAllowed({"CUSTOMER", "ADMIN"})
    public Map<String, Object> createOrder(Map<String, Object> req) {
        // SICUREZZA: Prendi l'ID utente dal token, non dal body della richiesta
        String jwtUserId = jwt.getClaim("userId").toString();
        
        // Sovrascrivi l'userId nel payload per garantire che l'utente crei l'ordine per se stesso
        req.put("userId", jwtUserId);
        
        return orders.create(req);
    }

    @GET
    @Path("/orders/me")
    @RolesAllowed({"CUSTOMER", "ADMIN"}) // Solo utenti autenticati
    public List<Map<String, Object>> listMyOrders(
            @QueryParam("page") @DefaultValue("0") int p,
            @QueryParam("size") @DefaultValue("20") int s) {
        
        // Estrai l'ID utente (come stringa) dal claim "userId" del token
        String userId = jwt.getClaim("userId").toString();
        
        // Chiama l'order-service passando l'userId
        return orders.list(userId, p, s);
    }

    @POST
    @Path("/payments")
    @RolesAllowed({"CUSTOMER", "ADMIN"}) // Solo utenti autenticati possono pagare
    public Map<String, Object> pay(Map<String, Object> req) {
        return payments.pay(req);
    }

    // === Endpoint solo per Amministratori ===
    @POST
    @Path("/books")
    @RolesAllowed("ADMIN") // Solo ADMIN può aggiungere libri
    public BookUpsert createBook(BookUpsert req) {
        return catalog.create(req);
    }

    @GET
    @Path("/orders")
    @RolesAllowed("ADMIN") // Solo ADMIN può vedere tutti gli ordini
    public List<Map<String, Object>> listOrders(@QueryParam("page") @DefaultValue("0") int p, @QueryParam("size") @DefaultValue("20") int s, @HeaderParam("Authorization") String authorizationHeader) {
        // NOTA: andrebbe implementato un filtro per utente se anche i CUSTOMER potessero accedere
        return orders.list(null, p, s);
    }

    @GET
    @Path("/users")
    @RolesAllowed("ADMIN")
    public List<UserDTO> listUsers(@QueryParam("page") @DefaultValue("0") int p, @QueryParam("size") @DefaultValue("20") int s, @HeaderParam("Authorization") String authorizationHeader) {
        return users.list(p, s, authorizationHeader);
    }

    @GET
    @Path("/inventory")
    @RolesAllowed("ADMIN")
    public List<InventoryItem> listInventory(
            @QueryParam("page") @DefaultValue("0") int p, 
            @QueryParam("size") @DefaultValue("50") int s) {
        return inventory.list(p, s);
    }

    @POST
    @Path("/inventory/{isbn}/adjust")
    @RolesAllowed("ADMIN")
    public Map<String, Object> adjustInventory(
            @PathParam("isbn") String isbn, 
            Map<String, Object> req) {
        
        // Estrai e valida i dati dalla mappa in arrivo dal frontend
        Integer delta = (Integer) req.get("delta");
        String location = (String) req.get("location");
        if (delta == null || delta == 0) {
            throw new jakarta.ws.rs.BadRequestException("Il campo 'delta' è obbligatorio e non può essere 0");
        }

        var clientReq = new AdjustRequest(delta, location);
        var item = inventory.adjust(isbn, clientReq);

        return Map.of( 
            "isbn", item.isbn(), 
            "quantity", item.quantity(), 
            "location", item.location()
        );
    }
}

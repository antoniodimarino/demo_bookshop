package it.example.bookshop.order.service;

import java.net.URI;
import java.util.List;

import org.jboss.logging.Logger;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import it.example.bookshop.order.service.dto.CreateOrderRequest;
import it.example.bookshop.order.service.dto.OrderResponse;
import it.example.bookshop.order.service.events.ReleaseStockPayload;
import it.example.bookshop.order.service.events.ReserveStockPayload;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/orders")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OrderResource {

    private static final Logger LOG = Logger.getLogger(OrderResource.class);
    @Inject StockEventProducer stockEvents;
    @Inject Event<ReserveStockPayload> reserveStockEvent;
    @Inject Event<ReleaseStockPayload> releaseStockEvent;

    @GET
    public List<OrderResponse> list(@QueryParam("userId") String userId, @QueryParam("page") @DefaultValue("0") int page,
                                    @QueryParam("size") @DefaultValue("20") int size) {
        PanacheQuery<CustomerOrder> query;
        if (userId != null && !userId.isBlank()) {
            // Se userId è fornito, filtra per utente
            query = CustomerOrder.<CustomerOrder>find("userId = ?1", userId).page(page, size);
        } else {
            // Altrimenti, restituisci tutti gli ordini (per Admin)
            query = CustomerOrder.<CustomerOrder>findAll().page(page, size);
        }
        return query.list()
                .stream()
                .map(o -> OrderResponse.fromEntity((CustomerOrder) o))
                .toList();
    }

    @GET
    @Path("/{id}")
    public OrderResponse get(@PathParam("id") Long id) {
        CustomerOrder o = CustomerOrder.findById(id);
        if (o == null) throw new NotFoundException();
        return OrderResponse.fromEntity(o);
    }

    @POST
    @Transactional
    public Response create(CreateOrderRequest req, @Context UriInfo uriInfo) {
        
        if (req == null) throw new BadRequestException("Body mancante");
        if (req.userId() == null || req.userId().isBlank()) throw new BadRequestException("userId obbligatorio");
        if (req.items() == null || req.items().isEmpty()) throw new BadRequestException("items obbligatorio e non vuoto");

        CustomerOrder order = new CustomerOrder();
        order.userId = req.userId();

        long total = 0L;
        List<CreateOrderRequest.Item> items = req.items();
        for (CreateOrderRequest.Item it : items) {
            if (it == null || it.isbn() == null || it.isbn().isBlank())
                throw new BadRequestException("isbn obbligatorio");
            if (it.quantity() <= 0)
                throw new BadRequestException("quantity deve essere > 0");
            OrderItem oi = new OrderItem();
            oi.isbn = it.isbn();
            oi.quantity = it.quantity();
            oi.unitPriceCents = it.unitPriceCents();
            order.addItem(oi);
            if (oi.unitPriceCents != null) total += oi.unitPriceCents * oi.quantity;
        }
        order.totalCents = (total > 0 ? total : null);
        order.persist();
        
        var reserve = new it.example.bookshop.order.service.events.ReserveStockEvent(
                            order.id,
                            order.userId,
                            order.items.stream()
                                .map(i -> new it.example.bookshop.order.service.events.ReserveStockEvent.Item(
                                    i.isbn, i.quantity, null /* location opzionale */
                                ))
                                .toList()
        );
        // stockEvents.publishReserve(reserve);
        reserveStockEvent.fire(new ReserveStockPayload(reserve));

        URI location = uriInfo.getAbsolutePathBuilder().path(String.valueOf(order.id)).build();
        return Response.created(location).entity(OrderResponse.fromEntity(order)).build();
    }

    @POST
    @Path("/{id}/cancel")
    @Transactional
    public OrderResponse cancel(@PathParam("id") Long id) {
        CustomerOrder o = CustomerOrder.findById(id);
        if (o == null) throw new NotFoundException();
        if (o.status == OrderStatus.FULFILLED) {
            throw new ClientErrorException("Ordine già evaso", 409);
        }
        
        o.status = OrderStatus.CANCELLED;

        var release = new it.example.bookshop.order.service.events.ReleaseStockEvent(
                            o.id,
                            o.userId,
                            o.items.stream()
                                .map(i -> new it.example.bookshop.order.service.events.ReleaseStockEvent.Item(
                                    i.isbn, i.quantity, null
                                ))
                                .toList()
        );
        // stockEvents.publishRelease(release);
        releaseStockEvent.fire(new ReleaseStockPayload(release));
        return OrderResponse.fromEntity(o);
    }

    @POST
    @Path("/{id}/mark-paid")
    @Transactional
    public OrderResponse markPaid(@PathParam("id") Long id) {
        CustomerOrder o = CustomerOrder.findById(id);
        if (o == null) throw new NotFoundException();
        if (o.status != OrderStatus.NEW) {
            throw new ClientErrorException("Solo gli ordini NEW possono essere marcati come PAID", 409);
        }
        o.status = OrderStatus.PAID;
        return OrderResponse.fromEntity(o);
    }

    @POST
    @Path("/{id}/fulfill")
    @Transactional
    public OrderResponse fulfill(@PathParam("id") Long id) {
        CustomerOrder o = CustomerOrder.findById(id);
        if (o == null) throw new NotFoundException();
        if (o.status != OrderStatus.PAID) {
            throw new ClientErrorException("Solo gli ordini PAID possono essere evasi", 409);
        }
        o.status = OrderStatus.FULFILLED;
        return OrderResponse.fromEntity(o);
    }
}
package it.example.bookshop.payment.service;

import java.util.Map;
import java.util.UUID;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import it.example.bookshop.payment.service.dto.PaymentRequest;
import it.example.bookshop.payment.service.dto.PaymentResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/payments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PaymentResource {

    @Inject
    @Channel("payment-confirmed")
    Emitter<String> confirmed;
    private static final ObjectMapper M = new ObjectMapper();

    @POST
    public PaymentResponse pay(PaymentRequest req) {
        if (req == null || req.orderId() == null || req.amountCents() == null) {
            throw new BadRequestException("Dati pagamento mancanti");
        }
// TODO integrare provider esterno (PayPal/Stripe). Qui mockiamo success.
        String txId = UUID.randomUUID().toString();
        try {
            confirmed.send(M.writeValueAsString(Map.of(
                    "orderId", req.orderId(), "amountCents", req.amountCents(), "method", req.method(), "txId", txId)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new PaymentResponse("CONFIRMED", txId);
    }

    @GET
    public Response ping() {
        return Response.ok(Map.of("status", "ok")).build();
    }
}

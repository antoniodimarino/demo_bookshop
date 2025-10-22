package it.example.bookshop.order.service;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.example.bookshop.order.service.events.ReleaseStockPayload;
import it.example.bookshop.order.service.events.ReserveStockPayload;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class StockEventProducer {

    @Inject @Channel("reserve-stock")
    Emitter<String> reserveEmitter;

    @Inject @Channel("release-stock")
    Emitter<String> releaseEmitter;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void onReserveStock(@Observes(during = TransactionPhase.AFTER_SUCCESS) ReserveStockPayload payload) {
        reserveEmitter.send(asJson(payload.event()));
    }

    public void onReleaseStock(@Observes(during = TransactionPhase.AFTER_SUCCESS) ReleaseStockPayload payload) {
        releaseEmitter.send(asJson(payload.event()));
    }
    /*
    public void publishReserve(ReserveStockEvent evt) {
        reserveEmitter.send(asJson(evt));
    }
    public void publishRelease(ReleaseStockEvent evt) {
        releaseEmitter.send(asJson(evt));
    }
    */
    private String asJson(Object o) {
        try { return MAPPER.writeValueAsString(o); }
        catch (JsonProcessingException e) { throw new RuntimeException(e); }
    }
}
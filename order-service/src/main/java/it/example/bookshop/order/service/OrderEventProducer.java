package it.example.bookshop.order.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import it.example.bookshop.order.service.events.OrderCreated;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderEventProducer {

    @Inject
    @Channel("order-created")
    Emitter<String> emitter;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public void publish(OrderCreated evt) {
        try {
            emitter.send(MAPPER.writeValueAsString(evt));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot serialize OrderCreated", e);
        }
    }
}
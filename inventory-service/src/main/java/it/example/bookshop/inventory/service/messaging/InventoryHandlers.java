package it.example.bookshop.inventory.service.messaging;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.reactive.messaging.annotations.Blocking;
import it.example.bookshop.inventory.service.InventoryItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;

@ApplicationScoped
public class InventoryHandlers {

    private static final ObjectMapper M = new ObjectMapper();
    
    // Add @JsonIgnoreProperties(ignoreUnknown = true) if you only care about isbn
    record BookCreatedEvent(String isbn, String title, java.util.List<String> authors) {}

    record ReserveStockEvent(Long orderId, String userId, List<Item> items) {

        record Item(String isbn, int quantity, String location) {

        }
    }

    record ReleaseStockEvent(Long orderId, String userId, List<Item> items) {

        record Item(String isbn, int quantity, String location) {

        }
    }

    @Incoming("book-created")
    @Blocking
    @Transactional
    public void onBookCreated(String payload) throws Exception {
        var evt = M.readValue(payload, BookCreatedEvent.class);
        if (evt.isbn() != null && InventoryItem.find("isbn", evt.isbn()).firstResult() == null) {
            InventoryItem item = new InventoryItem();
            item.isbn = evt.isbn();
            item.quantity = 0; // Default quantity
            item.location = "default"; // Default location
            item.persist();
        }
    }

    @Incoming("reserve-stock")
    @Blocking
    @Transactional
    public void onReserve(String payload) throws Exception {
        var evt = M.readValue(payload, ReserveStockEvent.class);
        for (var it : evt.items()) {
            InventoryItem item = InventoryItem.find("isbn", it.isbn()).firstResult();
            if (item == null || item.quantity < it.quantity()) {
                return;
            }
            item.quantity -= it.quantity();
        }
    }

    @Incoming("release-stock")
    @Blocking
    @Transactional
    public void onRelease(String payload) throws Exception {
        var evt = M.readValue(payload, ReleaseStockEvent.class);
        for (var it : evt.items()) {
            InventoryItem item = InventoryItem.find("isbn", it.isbn()).firstResult();
            if (item == null) {
                item = new InventoryItem();
                item.isbn = it.isbn();
                item.location = it.location();
                item.quantity = 0;
            }
            item.quantity += it.quantity();
            item.persist();
        }
    }
}

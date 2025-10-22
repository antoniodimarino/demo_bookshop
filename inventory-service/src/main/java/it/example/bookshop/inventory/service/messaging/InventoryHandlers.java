package it.example.bookshop.inventory.service.messaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.reactive.messaging.annotations.Blocking;
import it.example.bookshop.inventory.service.InventoryItem;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class InventoryHandlers {

    private static final ObjectMapper M = new ObjectMapper();

    @Inject
    @Channel("stock-reservation-failed")
    Emitter<String> reservationFailedEmitter;
    
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

    record StockReservationFailed(Long orderId, String reason, List<String> failedIsbns) {}

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

        List<String> failedIsbns = new ArrayList<>();
        String reason = "INSUFFICIENT_STOCK"; // Ragione di default
        Map<String, Integer> requiredStock = new HashMap<>();
        Map<String, InventoryItem> foundItems = new HashMap<>();

        for (var it : evt.items()) {
            requiredStock.merge(it.isbn(), it.quantity(), Integer::sum);
        }

        for (Map.Entry<String, Integer> entry : requiredStock.entrySet()) {
            String isbn = entry.getKey();
            int requiredQty = entry.getValue();
            
            InventoryItem item = InventoryItem.find("isbn", isbn).firstResult();
            
            if (item == null) {
                failedIsbns.add(isbn);
                reason = "ITEM_NOT_FOUND";
            } else if (item.quantity < requiredQty) {
                failedIsbns.add(isbn);
            } else {
                foundItems.put(isbn, item);
            }
        }

        if (!failedIsbns.isEmpty()) {
            var failureEvent = new StockReservationFailed(evt.orderId(), reason, failedIsbns);
            reservationFailedEmitter.send(M.writeValueAsString(failureEvent));
            return; 
        }

        for (Map.Entry<String, Integer> entry : requiredStock.entrySet()) {
            InventoryItem item = foundItems.get(entry.getKey());
            item.quantity -= entry.getValue();
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

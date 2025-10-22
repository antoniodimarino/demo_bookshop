package it.example.bookshop.order.service.messaging;

import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.reactive.messaging.annotations.Blocking;
import it.example.bookshop.order.service.CustomerOrder;
import it.example.bookshop.order.service.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class StockEventConsumer {

    private static final Logger LOG = Logger.getLogger(StockEventConsumer.class);
    private static final ObjectMapper M = new ObjectMapper();

    // DTO per l'evento di fallimento (deve corrispondere a quello in inventory-service)
    record StockReservationFailed(Long orderId, String reason, List<String> failedIsbns) {}

    /**
     * Consumer per la Saga di compensazione.
     * Se l'inventario segnala un fallimento nella prenotazione, cancelliamo l'ordine.
     */
    @Incoming("stock-reservation-failed")
    @Blocking
    @Transactional
    public void onStockReservationFailed(String payload) throws Exception {
        var evt = M.readValue(payload, StockReservationFailed.class);
        
        LOG.warnf("Ricevuto fallimento prenotazione stock per ordine %d (Reason: %s, ISBNs: %s). Tentativo di cancellazione.",
                evt.orderId(), evt.reason(), evt.failedIsbns());

        CustomerOrder o = CustomerOrder.findById(evt.orderId());
        
        if (o == null) {
            LOG.warnf("Ordine %d non trovato. Evento ignorato.", evt.orderId());
            return; // Idempotenza
        }

        // Cancelliamo l'ordine solo se è ancora in stato NEW.
        // Se fosse già CANCELLED o PAID (improbabile), non facciamo nulla.
        if (o.status == OrderStatus.NEW) {
            o.status = OrderStatus.CANCELLED;
            // .persist() non è necessario grazie a @Transactional e Panache
            LOG.infof("Ordine %d impostato su CANCELLED a causa di errore inventario.", o.id);
        } else {
            LOG.infof("Ordine %d già in stato %s. Nessuna azione eseguita.", o.id, o.status);
        }
    }
}
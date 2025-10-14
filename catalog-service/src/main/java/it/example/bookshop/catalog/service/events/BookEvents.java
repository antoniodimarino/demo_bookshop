package it.example.bookshop.catalog.service.events;

import java.util.Map;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.event.TransactionPhase;
import jakarta.inject.Inject;

@ApplicationScoped
public class BookEvents {

    @Inject
    @Channel("book-created")
    Emitter<String> created;
    private static final ObjectMapper M = new ObjectMapper();

    /*
    public void bookCreated(String isbn, String title, List<String> authors) {
        try {
            created.send(M.writeValueAsString(Map.of("isbn", isbn, "title", title, "authors", authors)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    */
   public void onBookCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) BookCreatedPayload payload) {
        try {
            var map = Map.of("isbn", payload.isbn(), "title", payload.title(), "authors", payload.authors());
            created.send(M.writeValueAsString(map));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

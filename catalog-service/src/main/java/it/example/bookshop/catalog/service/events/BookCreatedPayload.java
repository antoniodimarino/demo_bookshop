package it.example.bookshop.catalog.service.events;
import java.util.List;

public record BookCreatedPayload(String isbn, String title, List<String> authors) {
}
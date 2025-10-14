package it.example.bookshop.common.dto;

import java.util.List;

public record BookUpsert(String isbn, String title, String description, String language,
        Integer publishedYear, Long priceCents,
        List<String> authors, List<String> categories) {

}

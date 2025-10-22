package it.example.bookshop.catalog.service;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import it.example.bookshop.catalog.service.isbn.IsbnClient;
import it.example.bookshop.catalog.service.model.Book;
import it.example.bookshop.common.dto.BookUpsert;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class CatalogResourceTest {

    @InjectMock
    @RestClient
    IsbnClient isbnClient;

    // Pulisce il DB prima di ogni test
    @BeforeEach
    @Transactional
    void setup() {
        Book.deleteAll();
    }

    @Test
    void testCreateBookSuccess() {
        // GIVEN: Un DTO valido e il servizio ISBN che risponde positivamente
        String validIsbn = "9783161484100";
        BookUpsert newBook = new BookUpsert(validIsbn, "A new book", "Description", "it", 2025, 1500L, List.of("Test Author"), List.of("testing"));
        when(isbnClient.validate(validIsbn)).thenReturn(new IsbnClient.ValidationResult(validIsbn, true));

        // WHEN: Chiamiamo l'endpoint di creazione
        given()
            .contentType(ContentType.JSON)
            .body(newBook)
        .when()
            .post("/catalog/books")
        .then()
            // THEN: Ci aspettiamo uno status 201 e i dati corretti in risposta
            .statusCode(201)
            .body("isbn", is(validIsbn))
            .body("title", is("A new book"));

        // Verifichiamo anche che il libro sia stato salvato nel database
        assertEquals(1, Book.count());
    }

    @Test
    void testCreateBookWithInvalidIsbn() {
        // GIVEN: Un ISBN non valido
        String invalidIsbn = "12345";
        BookUpsert newBook = new BookUpsert(invalidIsbn, "A book with bad ISBN", null, null, null, null, null, null);
        when(isbnClient.validate(invalidIsbn)).thenReturn(new IsbnClient.ValidationResult(invalidIsbn, false));

        // WHEN: Chiamiamo l'endpoint di creazione
        given()
            .contentType(ContentType.JSON)
            .body(newBook)
        .when()
            .post("/catalog/books")
        .then()
            // THEN: Ci aspettiamo un errore 400 Bad Request
            .statusCode(400);
    }

    @Test
    @Transactional
    void testCreateBookWithDuplicateIsbn() {
        // GIVEN: Un libro con lo stesso ISBN è già presente nel database
        String existingIsbn = "9780321765723";
        Book existingBook = new Book();
        existingBook.isbn = existingIsbn;
        existingBook.title = "The Original Book";
        existingBook.persist();

        BookUpsert duplicateBook = new BookUpsert(existingIsbn, "A Duplicate Book", null, null, null, null, null, null);
        when(isbnClient.validate(existingIsbn)).thenReturn(new IsbnClient.ValidationResult(existingIsbn, true));

        // WHEN: Tentiamo di creare un libro con lo stesso ISBN
        given()
            .contentType(ContentType.JSON)
            .body(duplicateBook)
        .when()
            .post("/catalog/books")
        .then()
            // THEN: Ci aspettiamo un errore 409 Conflict
            .statusCode(409);
    }
}
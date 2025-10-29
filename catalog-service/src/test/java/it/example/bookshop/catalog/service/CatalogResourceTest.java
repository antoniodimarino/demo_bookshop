package it.example.bookshop.catalog.service;

import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import it.example.bookshop.catalog.service.isbn.IsbnClient;
import it.example.bookshop.catalog.service.model.Book;
import it.example.bookshop.common.dto.BookUpsert;
import jakarta.transaction.Transactional;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

@QuarkusTest
class CatalogResourceTest {

    @InjectMock
    @RestClient
    IsbnClient isbnClient;

    // Inietta UserTransaction
    @Inject
    UserTransaction utx;

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
    void testCreateBookWithDuplicateIsbn() throws Exception { // Aggiungi "throws Exception"
        // GIVEN: Un libro con lo stesso ISBN è già presente nel database
        String existingIsbn = "9780321765723";
        
        // Avvia e committa la transazione di setup
        utx.begin();
        Book existingBook = new Book();
        existingBook.isbn = existingIsbn;
        existingBook.title = "The Original Book";
        existingBook.persist();
        utx.commit();

        BookUpsert duplicateBook = new BookUpsert(existingIsbn, "A Duplicate Book", null, null, null, null, null, null);
        when(isbnClient.validate(existingIsbn)).thenReturn(new IsbnClient.ValidationResult(existingIsbn, true));

        // WHEN: Tentiamo di creare un libro con lo stesso ISBN
        given()
            .contentType(ContentType.JSON)
            .body(duplicateBook)
        .when()
            .post("/catalog/books")
        .then()
            // THEN: Ora ci aspettiamo un 409 (grazie alla Fix 1, non avremo più 500)
            .statusCode(409);
    }

    @Test
    void testListBooks() throws Exception { // Aggiungi "throws Exception"
        // GIVEN: Due libri presenti nel database (creati in una transazione separata)
        
        utx.begin();
        Book b1 = new Book();
        b1.isbn = "978-1111111111";
        b1.title = "Il primo libro";
        b1.persist();

        Book b2 = new Book();
        b2.isbn = "978-2222222222";
        b2.title = "Il secondo libro";
        b2.persist();
        utx.commit();

        // WHEN: Chiamiamo l'endpoint di elenco
        given()
        .when()
            .get("/catalog/books")
        .then()
            // THEN: Ci aspettiamo uno status 200 e un array di 2 elementi
            .statusCode(200)
            .body("$", hasSize(2)) 
            .body("[0].title", is("Il primo libro"))
            .body("[1].isbn", is("978-2222222222"));
    }
}
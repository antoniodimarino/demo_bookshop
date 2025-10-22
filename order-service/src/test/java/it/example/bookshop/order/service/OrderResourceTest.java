package it.example.bookshop.order.service;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import it.example.bookshop.order.service.dto.CreateOrderRequest;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
class OrderResourceTest {

    @BeforeEach
    @Transactional
    void setup() {
        OrderItem.deleteAll();
        CustomerOrder.deleteAll();
    }

    @Test
    void testCreateOrderSuccess() {
        // GIVEN: Una richiesta di creazione ordine valida
        var item1 = new CreateOrderRequest.Item("978-1-60309-452-8", 2, 1250L);
        var item2 = new CreateOrderRequest.Item("978-1-60309-453-5", 1, 1999L);
        var request = new CreateOrderRequest("user-123", List.of(item1, item2));

        // WHEN: Chiamiamo l'endpoint di creazione
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/orders")
        .then()
            // THEN: Ci aspettiamo uno status 201 e una risposta valida
            .statusCode(201)
            .body("id", notNullValue())
            .body("userId", is("user-123"))
            .body("status", is("NEW"))
            .body("items", hasSize(2))
            .body("items[0].isbn", is("978-1-60309-452-8"));

        // Verifichiamo anche che l'ordine sia stato salvato nel database
        assertEquals(1, CustomerOrder.count());
        assertEquals(2, OrderItem.count());
    }

    @Test
    void testCreateOrderWithMissingUserId() {
        // GIVEN: Una richiesta senza userId
        var request = new CreateOrderRequest(null, List.of(new CreateOrderRequest.Item("123", 1, 100L)));

        // WHEN: Chiamiamo l'endpoint
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/orders")
        .then()
            // THEN: Ci aspettiamo un errore 400 Bad Request
            .statusCode(400);
    }
}
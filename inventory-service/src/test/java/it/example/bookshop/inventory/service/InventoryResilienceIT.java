package it.example.bookshop.inventory.service;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class InventoryResilienceIT {

    /**
     * In application.properties per il profilo test mettiamo un URL irraggiungibile
     * così la validate() fallisce → scatta Fallback → validazione locale.
     * Usiamo un ISBN valido (9780306406157) per vedere il comportamento "ok".
     */
    @Test
    void adjust_uses_fallback_when_isbn_service_down() {
        given().contentType("application/json")
            .body("{\"delta\":3, \"location\":\"B2\"}")
            .when().post("/inventory/9780306406157/adjust")
            .then().statusCode(200)
            .body("quantity", is(3))
            .body("location", is("B2"));
    }
}

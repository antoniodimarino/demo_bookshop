package it.example.bookshop.inventory.service;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;

@QuarkusTest
class InventoryResourceTest {

  @InjectMock @RestClient IsbnClient isbn;

  @Test
  void adjust_happy_path() {
    when(isbn.validate("9780306406157")).thenReturn(new IsbnClient.ValidationResult("9780306406157", true));

    given().contentType("application/json")
        .body("{\"delta\":5, \"location\":\"A1\"}")
        .when().post("/inventory/9780306406157/adjust")
        .then().statusCode(200)
        .body("quantity", is(5))
        .body("location", is("A1"));
    }

  @Test
  void reject_invalid_isbn() {
    when(isbn.validate("bad")).thenReturn(new IsbnClient.ValidationResult("bad", false));

    given().contentType("application/json").body("{\"delta\":1,\"location\":\"X\"}")
        .when().post("/inventory/bad/adjust")
        .then().statusCode(400);
    }
}
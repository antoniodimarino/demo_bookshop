package it.example.bookshop.isbn;

import static org.hamcrest.Matchers.is;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;

@QuarkusTest
class IsbnResourceTest {
    @Test
    void validate_endpoint() {
        given().when().get("/isbn/validate/9780306406157")
            .then().statusCode(200).body("valid", is(true));
    }
}
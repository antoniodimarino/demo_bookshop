package it.example.bookshop.user.service;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import it.example.bookshop.common.dto.AuthRequest;
import it.example.bookshop.common.dto.RegisterRequest;
import it.example.bookshop.user.service.model.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class UserResourceTest {

    @BeforeEach
    @Transactional
    void setup() {
        User.deleteAll();
    }

    @Test
    void testRegisterUserSuccess() {
        // GIVEN: Una richiesta di registrazione valida
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test", "User");

        // WHEN: Chiamiamo l'endpoint di registrazione
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/users/register")
        .then()
            // THEN: Ci aspettiamo uno status 201 e i dati dell'utente (senza password)
            .statusCode(201)
            .body("id", notNullValue())
            .body("email", is("test@example.com"))
            .body("firstName", is("Test"));
    }

    @Test
    void testRegisterUserConflict() {
        // GIVEN: Un utente è già stato registrato con la stessa email
        RegisterRequest request = new RegisterRequest("conflict@example.com", "password123", "Conflict", "User");
        given().contentType(ContentType.JSON).body(request).when().post("/users/register").then().statusCode(201);

        // WHEN: Tentiamo di registrare di nuovo con la stessa email
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/users/register")
        .then()
            // THEN: Ci aspettiamo un errore 409 Conflict
            .statusCode(409);
    }

    @Test
    void testLoginSuccess() {
        // GIVEN: Un utente registrato
        RegisterRequest registerRequest = new RegisterRequest("login@example.com", "password123", "Login", "User");
        given().contentType(ContentType.JSON).body(registerRequest).when().post("/users/register");

        AuthRequest loginRequest = new AuthRequest("login@example.com", "password123");

        // WHEN: Effettuiamo il login con le credenziali corrette
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/users/login")
        .then()
            // THEN: Ci aspettiamo uno status 200 e un token JWT
            .statusCode(200)
            .body("token", notNullValue());
    }

    @Test
    void testLoginUnauthorized() {
        // GIVEN: Un utente registrato e una richiesta di login con password errata
        RegisterRequest registerRequest = new RegisterRequest("loginfail@example.com", "password123", "Login", "User");
        given().contentType(ContentType.JSON).body(registerRequest).when().post("/users/register");
        
        AuthRequest loginRequest = new AuthRequest("loginfail@example.com", "wrongpassword");

        // WHEN: Effettuiamo il login con la password sbagliata
        given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/users/login")
        .then()
            // THEN: Ci aspettiamo un errore 401 Unauthorized
            .statusCode(401);
    }

    @Test
    void testGetMeSuccess() {
        RegisterRequest registerRequest = new RegisterRequest("me@example.com", "password123", "Login", "User");
        given().contentType(ContentType.JSON).body(registerRequest).when().post("/users/register")
        .then().statusCode(201);

        AuthRequest loginRequest = new AuthRequest("me@example.com", "password123");
        String token = given()
            .contentType(ContentType.JSON)
            .body(loginRequest)
        .when()
            .post("/users/login")
        .then()
            .statusCode(200)
            .extract().path("token");
        
        given().header("Authorization", "Bearer " + token)
        .when().get("/users/me").then()
        .statusCode(200)
        .body("email", is("me@example.com"))
        .body("firstName", is("Login"))
        .body("role", is("CUSTOMER"));
        
    }
}
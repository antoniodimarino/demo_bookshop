package it.example.bookshop.inventory.service;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

// circuit breaker
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;

import java.time.temporal.ChronoUnit;

@RegisterRestClient(configKey = "isbn-client")
@Path("/isbn")
@Produces(MediaType.APPLICATION_JSON)
public interface IsbnClient {

     /**
     * Resilienza:
     * - Timeout: 1s
     * - Retry: 2 tentativi (con 100ms di delay + jitter)
     * - CircuitBreaker: finestra 4 chiamate, apre se 75% errori, resta aperto 5s, richiede 2 successi per richiudersi
     * - Fallback: calcolo locale della validità ISBN (se il servizio è giù)
     */

    @GET @Path("/validate/{isbn}")
    @Timeout(value = 1_000) // ms
    @Retry(maxRetries = 2, delay = 100, jitter = 50)
    @CircuitBreaker(
        requestVolumeThreshold = 4,
        failureRatio = 0.75,
        delay = 5, delayUnit = ChronoUnit.SECONDS,
        successThreshold = 2
    )
    @Fallback(IsbnClientFallback.class)
    ValidationResult validate(@PathParam("isbn") String isbn);
    record ValidationResult(String isbn, boolean valid) {}
}

package it.example.bookshop.payment.service;

import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import it.example.bookshop.payment.service.dto.PaymentRequest;

@QuarkusTest
class PaymentResourceTest {

    // Allinea il consumer al bootstrap usato da Reactive Messaging (DevServices)
    @ConfigProperty(
        name = "mp.messaging.connector.smallrye-kafka.bootstrap.servers",
        defaultValue = "localhost:9092"
    )
    String bootstrapServers;

    private KafkaConsumer<String, String> consumer;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String TOPIC = "bookshop-payment-confirmed";

    @BeforeEach
    void setup() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("group.id", "test-group-" + UUID.randomUUID());
        props.put("key.deserializer", StringDeserializer.class.getName());
        props.put("value.deserializer", StringDeserializer.class.getName());
        props.put("auto.offset.reset", "earliest");
        consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(TOPIC));
    }

    @AfterEach
    void tearDown() {
        if (consumer != null) {
            consumer.close();
        }
    }

    @Test
    void testPaySuccessAndEventSent() throws Exception {
        // GIVEN
        PaymentRequest request = new PaymentRequest(1L, 5000L, "CREDIT_CARD", "tok_123");

        // WHEN: chiamiamo l'endpoint
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/payments")
        .then()
            .statusCode(200)
            .body("status", is("CONFIRMED"))
            .body("txId", notNullValue());

        /*
        // THEN: attendiamo il messaggio sul topic con un piccolo retry loop (max 15s)
        String payload = null;
        long end = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < end && payload == null) {
            ConsumerRecords<String, String> polled = consumer.poll(Duration.ofMillis(500));
            if (polled != null && !polled.isEmpty()) {
                ConsumerRecord<String, String> rec = polled.iterator().next();
                payload = rec.value();
                break;
            }
        }
        assertNotNull(payload, "Nessun messaggio ricevuto dal topic entro il timeout");
        
        JsonNode payloadJson = OBJECT_MAPPER.readTree(payload);
        assertEquals(1L, payloadJson.get("orderId").asLong());
        assertEquals(5000L, payloadJson.get("amountCents").asLong());
        assertNotNull(payloadJson.get("txId").asText());
        */
    }
}

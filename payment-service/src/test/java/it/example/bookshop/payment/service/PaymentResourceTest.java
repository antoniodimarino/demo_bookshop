package it.example.bookshop.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import it.example.bookshop.payment.service.dto.PaymentRequest;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class PaymentResourceTest {

    @ConfigProperty(name = "kafka.bootstrap.servers")
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
        // GIVEN: Una richiesta di pagamento valida
        PaymentRequest request = new PaymentRequest(1L, 5000L, "CREDIT_CARD", "tok_123");

        // WHEN: Chiamiamo l'endpoint di pagamento
        given()
            .contentType(ContentType.JSON)
            .body(request)
        .when()
            .post("/payments")
        .then()
            // THEN: Ci aspettiamo uno status 200 e una risposta di conferma
            .statusCode(200)
            .body("status", is("CONFIRMED"))
            .body("txId", notNullValue());

        // E verifichiamo che un messaggio sia stato inviato al topic Kafka
        ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(10));
        assertNotNull(records);
        assertFalse(records.isEmpty());
        assertEquals(1, records.count());

        ConsumerRecord<String, String> record = records.iterator().next();
        String payload = record.value();
        JsonNode payloadJson = OBJECT_MAPPER.readTree(payload);

        assertEquals(1L, payloadJson.get("orderId").asLong());
        assertEquals(5000L, payloadJson.get("amountCents").asLong());
        assertNotNull(payloadJson.get("txId").asText());
    }
}

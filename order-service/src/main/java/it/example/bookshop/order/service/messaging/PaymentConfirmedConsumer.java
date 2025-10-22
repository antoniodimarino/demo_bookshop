package it.example.bookshop.order.service.messaging;

import org.eclipse.microprofile.reactive.messaging.Incoming;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.reactive.messaging.annotations.Blocking;
import it.example.bookshop.order.service.CustomerOrder;
import it.example.bookshop.order.service.OrderStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class PaymentConfirmedConsumer {

    private static final ObjectMapper M = new ObjectMapper();

    record PaymentConfirmed(Long orderId, Long amountCents, String method, String txId) {

    }

    @Incoming("payment-confirmed")
    @Blocking
    @Transactional
    public void onPayment(String payload) throws Exception {
        var evt = M.readValue(payload, PaymentConfirmed.class);
        CustomerOrder o = CustomerOrder.findById(evt.orderId());
        if (o == null) {
            return; // idempotenza soft

                }if (o.status == OrderStatus.NEW) {
            o.status = OrderStatus.PAID;
        }
    }
}

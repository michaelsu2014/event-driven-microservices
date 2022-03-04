package com.dailycodebuffer.OrderService.command.api.saga;

import com.dailycodebuffer.CommonService.commands.CancelOrderCommand;
import com.dailycodebuffer.CommonService.commands.CancelPaymentCommand;
import com.dailycodebuffer.CommonService.commands.CompleteOrderCommand;
import com.dailycodebuffer.CommonService.commands.ShipOrderCommand;
import com.dailycodebuffer.CommonService.commands.ValidatePaymentCommand;
import com.dailycodebuffer.CommonService.events.OrderCancelledEvent;
import com.dailycodebuffer.CommonService.events.OrderCompletedEvent;
import com.dailycodebuffer.CommonService.events.OrderShippedEvent;
import com.dailycodebuffer.CommonService.events.PaymentCancelledEvent;
import com.dailycodebuffer.CommonService.events.PaymentProcessedEvent;
import com.dailycodebuffer.CommonService.model.User;
import com.dailycodebuffer.CommonService.queries.GetUserPaymentDetailsQuery;
import com.dailycodebuffer.OrderService.command.api.events.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Saga
@Slf4j
public class OrderProcessingSaga {
    // need to start saga and end saga
    // saga events

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    public OrderProcessingSaga() {
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    private void handle(OrderCreatedEvent event) {
        log.info("OrderCreatedEvent in Saga for Order id: {}", event.getOrderId());

        GetUserPaymentDetailsQuery getUserPaymentDetailsQuery = new GetUserPaymentDetailsQuery(event.getUserId());

        User user = null;

        try {
            user = queryGateway.query(
                    getUserPaymentDetailsQuery,
                    ResponseTypes.instanceOf(User.class)
            ).join();
        } catch (Exception e) {
            log.error(e.getMessage());
            // Start the compensation transaction
            cancelOrderCommand(event.getOrderId());
        }


        //handle payment command
        //saga design pattern
        ValidatePaymentCommand validatePaymentCommand = ValidatePaymentCommand.builder()
                .cardDetails( user != null ? user.getCardDetails() : null)
                .orderId(event.getOrderId())
                .paymentId(UUID.randomUUID().toString())
                .build();

        commandGateway.sendAndWait(validatePaymentCommand);
    }

    private void cancelOrderCommand(String orderId) {
        CancelOrderCommand cancelOrderCommand = new CancelOrderCommand(orderId);
        commandGateway.sendAndWait(cancelOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    private void handle(PaymentProcessedEvent event) {
        log.info("PaymentProcessedEvent in Saga for Order id: {}", event.getOrderId());

        ShipOrderCommand shipOrderCommand = null;

        try {
//            if (true) {
//                throw new Exception();
//            }

            shipOrderCommand = ShipOrderCommand.builder()
                    .shipmentId(UUID.randomUUID().toString())
                    .orderId(event.getOrderId())
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            // if there are error, start the compensation transaction
            cancelPaymentCommand(event);
        }


        commandGateway.sendAndWait(shipOrderCommand);
    }

    private void cancelPaymentCommand(PaymentProcessedEvent event) {
        CancelPaymentCommand cancelPaymentCommand = new CancelPaymentCommand(event.getPaymentId(), event.getOrderId());

        commandGateway.sendAndWait(cancelPaymentCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderShippedEvent event) {
        log.info("OrderShippedEvent in Saga for Order id: {}", event.getOrderId());

        CompleteOrderCommand completeOrderCommand = CompleteOrderCommand.builder()
                .orderId(event.getOrderId())
                .orderStatus("APPROVED")
                .build();

        commandGateway.sendAndWait(completeOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handle(OrderCompletedEvent event) {
        log.info("OrderCompletedEvent in Saga for Order id: {}", event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    @EndSaga
    public void handle(OrderCancelledEvent event) {
        log.info("OrderCancelledEvent in Saga for Order id: {}", event.getOrderId());
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentCancelledEvent event) {
        log.info("PaymentCancelledEvent in Saga for Order id: {}", event.getOrderId());
        cancelOrderCommand(event.getOrderId());
    }
}

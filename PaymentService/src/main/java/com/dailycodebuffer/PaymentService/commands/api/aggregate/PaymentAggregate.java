package com.dailycodebuffer.PaymentService.commands.api.aggregate;

import com.dailycodebuffer.CommonService.commands.CancelPaymentCommand;
import com.dailycodebuffer.CommonService.commands.ValidatePaymentCommand;
import com.dailycodebuffer.CommonService.events.PaymentCancelledEvent;
import com.dailycodebuffer.CommonService.events.PaymentProcessedEvent;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.beans.BeanUtils;

@Aggregate
@Slf4j
public class PaymentAggregate {
    @AggregateIdentifier
    private String paymentId;
    private String orderId;
    private String paymentStatus;

    public PaymentAggregate() {
    }

    @CommandHandler
    public void handle(ValidatePaymentCommand validatePaymentCommand) {
        // validate the payment details
        // publish the payment process event
        log.info("executing validating payment comand"
                + "Order id {} and payment id {}", validatePaymentCommand.getOrderId(), validatePaymentCommand.getPaymentId());

        PaymentProcessedEvent paymentProcessedEvent = new PaymentProcessedEvent(
                validatePaymentCommand.getPaymentId(), validatePaymentCommand.getOrderId());

        AggregateLifecycle.apply(paymentProcessedEvent);

    }

    @EventSourcingHandler
    public void on(PaymentProcessedEvent event) {
        this.paymentId = event.getPaymentId();
        this.orderId = event.getOrderId();
    }

    @CommandHandler
    public void handle(CancelPaymentCommand cancelPaymentCommand) {
        PaymentCancelledEvent paymentCancelledEvent = new PaymentCancelledEvent();
        BeanUtils.copyProperties(cancelPaymentCommand, paymentCancelledEvent);

        AggregateLifecycle.apply(paymentCancelledEvent);
    }

    @EventSourcingHandler
    public void on(PaymentCancelledEvent event) {
        this.paymentStatus = event.getPaymentStatus();
    }
}

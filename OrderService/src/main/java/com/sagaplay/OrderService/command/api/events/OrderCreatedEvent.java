package com.sagaplay.OrderService.command.api.events;

import com.sagaplay.OrderService.command.api.command.CreateOrderCommand;
import lombok.Data;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.springframework.beans.BeanUtils;

@Data
public class OrderCreatedEvent {

    private String orderId;
    private String productId;
    private String userId;
    private String addressId;
    private Integer quantity;
    private String orderStatus;
}

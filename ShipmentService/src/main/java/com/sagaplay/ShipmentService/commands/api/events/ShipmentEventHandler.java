package com.sagaplay.ShipmentService.commands.api.events;

import com.sagaplay.CommonService.events.OrderShippedEvent;
import com.sagaplay.ShipmentService.commands.api.data.Shipment;
import com.sagaplay.ShipmentService.commands.api.data.ShipmentRepository;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class ShipmentEventHandler {

    private ShipmentRepository shipmentRepository;

    public ShipmentEventHandler(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @EventHandler
    public void on(OrderShippedEvent event) {
        Shipment shipment = new Shipment();
        BeanUtils.copyProperties(event, shipment);
        shipmentRepository.save(shipment);
    }
}

package com.sagaplay.ShipmentService.commands.api.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment, String > {
}

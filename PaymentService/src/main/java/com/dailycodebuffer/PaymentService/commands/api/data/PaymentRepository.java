package com.dailycodebuffer.PaymentService.commands.api.data;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, String > {
}

package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.payment.enums.PaymentStatus;

import java.util.UUID;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "payments")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;
    private UUID orderId;
    private Double totalPayment;
    private Double deliveryTotal;
    private Double feeTotal;
    private Double productTotal;
    private PaymentStatus status;
}
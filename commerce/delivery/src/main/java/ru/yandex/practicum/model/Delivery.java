package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ru.yandex.practicum.delivery.enums.DeliveryState;

import java.util.UUID;

@Getter
@Setter
@Entity
@RequiredArgsConstructor
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID deliveryId;
    @ManyToOne
    @JoinColumn(name = "from_address_id")
    private DeliveryAddress fromAddress;
    @ManyToOne
    @JoinColumn(name = "to_address_id")
    private DeliveryAddress toAddress;
    private UUID orderId;
    private DeliveryState deliveryState;
}
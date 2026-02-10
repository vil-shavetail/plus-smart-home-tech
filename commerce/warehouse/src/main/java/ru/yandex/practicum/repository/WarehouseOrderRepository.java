package ru.yandex.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.model.OrderBooking;

import java.util.UUID;

public interface WarehouseOrderRepository extends JpaRepository<OrderBooking, UUID> {
}
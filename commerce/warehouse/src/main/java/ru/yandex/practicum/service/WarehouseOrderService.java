package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.repository.WarehouseOrderRepository;

@Service
@RequiredArgsConstructor
public class WarehouseOrderService {
    private final WarehouseOrderRepository warehouseOrderRepository;

    public void save(OrderBooking orderBooking) {
        warehouseOrderRepository.save(orderBooking);
    }
}

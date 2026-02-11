package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.delivery.client.DeliveryClient;
import ru.yandex.practicum.delivery.dto.DeliveryDto;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.service.DeliveryService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/delivery")
public class DeliveryController implements DeliveryClient {
    private final DeliveryService deliveryService;

    @Override
    public DeliveryDto addDelivery(DeliveryDto newDelivery) {
        log.info("Event - request to create a delivery: {}", newDelivery);
        return deliveryService.addDelivery(newDelivery);
    }

    @Override
    public void successfulDelivery(UUID deliveryId) {
        log.info("Event - successful delivery: {} request",  deliveryId);
        deliveryService.successfulDelivery(deliveryId);
    }

    @Override
    public void pickedDelivery(UUID deliveryId) {
        log.info("Event - request to transfer an order for delivery: {}", deliveryId);
        deliveryService.pickedDelivery(deliveryId);
    }

    @Override
    public void failedDelivery(UUID deliveryId) {
        log.info("Event - Unsuccessful delivery: {} request", deliveryId);
        deliveryService.failedDelivery(deliveryId);
    }

    @Override
    public Double getDeliveryCost(OrderDto order) {
        log.info("Event - request to calculate the cost of order: {} delivery", order);
        return deliveryService.getDeliveryCost(order);
    }
}
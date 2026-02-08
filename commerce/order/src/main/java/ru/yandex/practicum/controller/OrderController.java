package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.order.client.OrderClient;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.order.dto.ProductReturnRequest;
import ru.yandex.practicum.service.OrderService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController implements OrderClient {
    private final OrderService orderService;

    @Override
    public List<OrderDto> getOrders(String username) {
        log.info("Event - request for receiving user: {} orders", username);
        return orderService.getOrder(username);
    }

    @Override
    public OrderDto createOrder(String username, OrderDto newOrder) {
        log.info("Event - User's request: {} to create a new order: {}", username, newOrder);
        return orderService.createOrder(username, newOrder);
    }

    @Override
    public OrderDto returnProducts(ProductReturnRequest request) {
        log.info("Event - request: {} for a refund", request);
        return orderService.returnProducts(request);
    }

    @Override
    public OrderDto payment(UUID orderId) {
        log.info("Event - request for order payment: {}", orderId);
        return orderService.payment(orderId);
    }

    @Override
    public OrderDto paymentFailed(UUID orderId) {
        log.info("Event - request for unsuccessful order payment: {}", orderId);
        return orderService.paymentFailed(orderId);
    }

    @Override
    public OrderDto delivery(UUID orderId) {
        log.info("Event - request for successful order delivery: {}", orderId);
        return orderService.delivery(orderId);
    }

    @Override
    public OrderDto deliveryFailed(UUID orderId) {
        log.info("Event - request for unsuccessful order delivery: {}", orderId);
        return orderService.deliveryFailed(orderId);
    }

    @Override
    public OrderDto completed(UUID orderId) {
        log.info("Event - request order completion: {}", orderId);
        return orderService.completed(orderId);
    }

    @Override
    public OrderDto calculateTotal(UUID orderId) {
        log.info("Event - request to calculate the full cost of the order: {}", orderId);
        return orderService.calculateTotal(orderId);
    }

    @Override
    public OrderDto calculateDelivery(UUID orderId) {
        log.info("Event - request to calculate the shipping cost: {}", orderId);
        return orderService.calculateDelivery(orderId);
    }

    @Override
    public OrderDto assembly(UUID orderId) {
        log.info("Event - request for order collection at the warehouse: {}", orderId);
        return orderService.assembly(orderId);
    }

    @Override
    public OrderDto assemblyFailed(UUID orderId) {
        log.info("Event - request in case of unsuccessful order assembly in the warehouse: {}", orderId);
        return orderService.assemblyFailed(orderId);
    }
}
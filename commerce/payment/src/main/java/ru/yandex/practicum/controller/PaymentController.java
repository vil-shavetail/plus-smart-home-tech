package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.payment.client.PaymentClient;
import ru.yandex.practicum.payment.dto.PaymentDto;
import ru.yandex.practicum.service.PaymentService;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payment")
public class PaymentController implements PaymentClient {
    private final PaymentService paymentService;

    @Override
    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        log.info("Event - request to create an order: {} payment", orderDto);
        return paymentService.createPaymentOrder(orderDto);
    }

    @Override
    public Double calculateTotalCost(OrderDto orderDto) {
        log.info("Event - request total calculation of the cost of products: {}", orderDto);
        return paymentService.calculateTotalCost(orderDto);
    }

    @Override
    public void refundPayment(UUID paymentId) {
        log.info("Event - request for successful order payment: {}", paymentId);
        paymentService.refundPayment(paymentId);
    }

    @Override
    public Double calculateProductCost(OrderDto orderDto) {
        log.info("Event - request to calculate the cost of products: {}", orderDto);
        return paymentService.calculateProductCost(orderDto);
    }

    @Override
    public void setPaymentFailed(UUID paymentId) {
        log.info("Event - Request failed order: {} payment", paymentId);
        paymentService.setPaymentFailed(paymentId);
    }
}
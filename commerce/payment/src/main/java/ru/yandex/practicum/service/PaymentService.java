package ru.yandex.practicum.service;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.mapper.PaymentMapper;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.order.client.OrderClient;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.payment.dto.PaymentDto;
import ru.yandex.practicum.payment.enums.PaymentStatus;
import ru.yandex.practicum.repository.PaymentRepository;
import ru.yandex.practicum.store.client.StoreClient;
import ru.yandex.practicum.store.dto.ProductDto;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final StoreClient storeClient;
    private final OrderClient orderClient;

    public PaymentDto createPaymentOrder(OrderDto orderDto) {
        if (orderDto.getTotalPrice() == null || orderDto.getDeliveryPrice() == null || orderDto.getProductPrice() == null) {
            throw new RuntimeException("Insufficient data to pay for the order");
        }
        Payment payment = PaymentMapper.mapToPayment(orderDto);
        Optional<Payment> oldPayment = paymentRepository.findByOrderId(orderDto.getOrderId());
        if (oldPayment.isPresent()) {
            log.info("Old Entity Payment: {}", oldPayment.get());
            payment.setPaymentId(oldPayment.get().getPaymentId());
        }
        payment.setStatus(PaymentStatus.PENDING);
        payment = paymentRepository.save(payment);
        log.info("Saving the payment in the database: {}", payment);
        return PaymentMapper.mapToPaymentDto(payment);
    }

    public Double calculateProductCost(OrderDto orderDto) {
        Map<UUID, Long> products = orderDto.getProducts();
        if (products == null || products.isEmpty()) {
            throw new IllegalArgumentException("The list of products must not be null or empty.");
        }
        Double productCost = 0.0;
        for (UUID productId : products.keySet()) {
            ProductDto product;
            try {
                product = storeClient.getProduct(productId);
                log.info("Product availability in the store: {}", product);
            } catch (FeignException e) {
                throw new RuntimeException(e.getMessage());
            }
            productCost += product.getPrice() * products.get(productId);
        }
        log.info("The cost of the products in the order: {}", productCost);
        return productCost;
    }

    public Double calculateTotalCost(OrderDto orderDto) {
        if (orderDto.getProductPrice() == null || orderDto.getDeliveryPrice() == null) {
            throw new RuntimeException("There is not enough data to calculate the full cost of the order");
        }
        Double totalCost = orderDto.getProductPrice() * 1.1 + orderDto.getDeliveryPrice();
        log.info("The total cost of the order: {}", totalCost);
        return totalCost;
    }

    public void setPaymentFailed(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("The payment ID cannot be null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("The payment with the Id was not found: " + paymentId));
        log.info("Find the required payment: {}", payment);
        payment.setStatus(PaymentStatus.FAILED);
        try {
            OrderDto dto = orderClient.paymentFailed(payment.getOrderId());
            log.info("Order status update: {}", dto);
        } catch (FeignException e) {
            throw new RuntimeException(e.getMessage());
        }
        payment = paymentRepository.save(payment);
        log.info("Updating the payment status: {}", payment);
    }

    public void refundPayment(UUID paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("The payment ID cannot be null");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("The payment with the Id was not found: " + paymentId));
        log.info("Find the required payment: {}", payment);
        payment.setStatus(PaymentStatus.SUCCESS);
        try {
            OrderDto dto = orderClient.payment(payment.getOrderId());
            log.info("Order status update: {}", dto);
        } catch (FeignException e) {
            throw new RuntimeException(e.getMessage());
        }
        payment = paymentRepository.save(payment);
        log.info("Updating the payment status: {}", payment);
    }
}
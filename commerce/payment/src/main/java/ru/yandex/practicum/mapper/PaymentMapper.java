package ru.yandex.practicum.mapper;


import lombok.experimental.UtilityClass;
import ru.yandex.practicum.model.Payment;
import ru.yandex.practicum.order.dto.OrderDto;
import ru.yandex.practicum.payment.dto.PaymentDto;

@UtilityClass
public class PaymentMapper {
    public static Payment mapToPayment(OrderDto orderDto) {
        Payment entity = new Payment();
        entity.setOrderId(orderDto.getOrderId());
        entity.setTotalPayment(orderDto.getTotalPrice());
        entity.setDeliveryTotal(orderDto.getDeliveryPrice());
        entity.setProductTotal(orderDto.getProductPrice());
        return entity;
    }

    public static PaymentDto mapToPaymentDto(Payment entity) {
        PaymentDto dto = new PaymentDto();
        dto.setPaymentId(entity.getPaymentId());
        dto.setTotalPayment(entity.getTotalPayment());
        dto.setDeliveryTotal(entity.getDeliveryTotal());
        dto.setFeeTotal(entity.getTotalPayment() - entity.getDeliveryTotal() - entity.getProductTotal());
        return dto;
    }
}

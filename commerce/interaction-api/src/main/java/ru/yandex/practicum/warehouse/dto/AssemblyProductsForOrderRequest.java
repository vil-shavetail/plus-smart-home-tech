package ru.yandex.practicum.warehouse.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class AssemblyProductsForOrderRequest {
    private UUID orderId;
    private Map<UUID, Long> products;
}
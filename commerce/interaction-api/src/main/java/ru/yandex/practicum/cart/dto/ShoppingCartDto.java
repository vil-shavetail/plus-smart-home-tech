package ru.yandex.practicum.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ShoppingCartDto {
    private final UUID shoppingCartId;
    private final Map<UUID, Long> products;
}
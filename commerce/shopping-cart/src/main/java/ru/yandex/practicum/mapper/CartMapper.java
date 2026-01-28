package ru.yandex.practicum.mapper;

import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import ru.yandex.practicum.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.model.Cart;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface CartMapper {
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "active", ignore = true)
    Cart toShoppingCart(final ShoppingCartDto productDto);
    ShoppingCartDto toShoppingCartDto(final Cart product);
}
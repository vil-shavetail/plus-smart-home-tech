package ru.yandex.practicum.cart.client;

import feign.FeignException;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.cart.dto.ChangeProductQuantityRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "shopping-cart", path = "/api/v1/shopping-cart")
public interface CartClient {
    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam @NotEmpty String username) throws FeignException;

    @PutMapping
    ShoppingCartDto addToCart(@RequestBody Map<UUID, Integer> products,
                              @RequestParam @NotEmpty String username
    ) throws FeignException;

    @DeleteMapping
    void deleteCart(@RequestParam @NotEmpty String username) throws FeignException;

    @PostMapping("/remove")
    ShoppingCartDto removeFromCart(@RequestBody List<UUID> products,
                                   @RequestParam @NotEmpty String username) throws FeignException;

    @PostMapping("/change-quantity")
    ShoppingCartDto changeProductQuantity(@RequestBody @Valid ChangeProductQuantityRequest request,
                                          @RequestParam @NotEmpty String username) throws FeignException;

}
package ru.yandex.practicum.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.cart.client.CartClient;
import ru.yandex.practicum.cart.dto.ChangeProductQuantityRequest;
import ru.yandex.practicum.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.service.CartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
public class CartController implements CartClient {
    private final CartService cartService;

    @Override
    public ShoppingCartDto getShoppingCart(@RequestParam(name = "username") String username) throws FeignException {
        log.info("Event - receipt of an authorized user's - {} shopping cart", username);
        return cartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addToCart(Map<UUID, Integer> products,
                                     @RequestParam(name = "username") String username) throws FeignException {
        log.info("Event - {} adding product {} to shopping cart", username, products);
        return cartService.addToCartProduct(username, products);
    }

    @Override
    public void deleteCart(@RequestParam(name = "username") String username) throws FeignException {
        log.info("Event - deactivating the user's - {} shopping cart", username);
        cartService.deleteCart(username);
    }

    @Override
    public ShoppingCartDto removeFromCart(List<UUID> products, @RequestParam(name = "username") String username) throws FeignException {
        return cartService.removeFromCart(products, username);
    }

    @Override
    public ShoppingCartDto changeProductQuantity(ChangeProductQuantityRequest request, @RequestParam(name = "username") String username) throws FeignException {
        log.info("Event - change the number of items in the cart");
        return cartService.changeProductQuantity(request, username);
    }
}
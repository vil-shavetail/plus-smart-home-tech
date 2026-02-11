package ru.yandex.practicum.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.model.OrderBooking;
import ru.yandex.practicum.service.WarehouseOrderService;
import ru.yandex.practicum.service.WarehouseService;
import ru.yandex.practicum.warehouse.client.WarehouseClient;
import ru.yandex.practicum.warehouse.dto.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseClient {
    private final WarehouseService warehouseService;
    private final WarehouseOrderService warehouseOrderService;

    @Override
    public void addProduct(NewProductInWarehouseRequest request) throws FeignException {
        warehouseService.addProduct(request);
    }

    @Override
    public BookedProductsDto checkProductCount(ShoppingCartDto shoppingCartDto) throws FeignException {
        return warehouseService.checkProductCount(shoppingCartDto);
    }

    @Override
    public void addProductQuantity(AddProductToWarehouseRequest request) throws FeignException {
        warehouseService.addProductQuantity(request);
    }

    @Override
    public AddressDto getWarehouseAddress() throws FeignException {
        return warehouseService.getWarehouseAddress();
    }

    @Override
    public void returnProductsToWarehouse(Map<UUID, Long> products) throws FeignException {
        warehouseService.returnProductsToWarehouse(products);
    }

    @Override
    public BookedProductsDto assembleProducts(AssemblyProductsForOrderRequest request) throws FeignException {
        ShoppingCartDto shoppingCartDto = new ShoppingCartDto(request.getOrderId(), request.getProducts());
        BookedProductsDto bookedProductsDto = checkProductCount(shoppingCartDto);
        OrderBooking orderBooking = new OrderBooking();
        orderBooking.setOrderId(request.getOrderId());
        orderBooking.setProducts(request.getProducts());
        warehouseOrderService.save(orderBooking);
        return bookedProductsDto;
    }
}
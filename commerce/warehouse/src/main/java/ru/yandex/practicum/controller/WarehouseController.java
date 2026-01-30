package ru.yandex.practicum.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.service.WarehouseService;
import ru.yandex.practicum.warehouse.client.WarehouseClient;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseClient {
    private final WarehouseService warehouseService;

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
}
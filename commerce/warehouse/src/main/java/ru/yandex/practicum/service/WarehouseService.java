package ru.yandex.practicum.service;

import feign.FeignException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.cart.dto.ShoppingCartDto;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.BookedProducts;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseRepository;
import ru.yandex.practicum.warehouse.dto.AddProductToWarehouseRequest;
import ru.yandex.practicum.warehouse.dto.AddressDto;
import ru.yandex.practicum.warehouse.dto.BookedProductsDto;
import ru.yandex.practicum.warehouse.dto.NewProductInWarehouseRequest;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WarehouseService {
    private final WarehouseRepository warehouseRepository;
    private final WarehouseMapper warehouseMapper;

    private static final String[] ADDRESSES =
            new String[]{"ADDRESS_1", "ADDRESS_2"};

    private static final String CURRENT_ADDRESS =
            ADDRESSES[Random.from(new SecureRandom()).nextInt(0, ADDRESSES.length)];

    public void addProduct(NewProductInWarehouseRequest request) throws FeignException {
        warehouseRepository.findById(request.getProductId()).ifPresent(product -> {
            throw new ValidationException("The product has already been added");
        });

        WarehouseProduct warehouseProduct = warehouseMapper.toWarehouseProduct(request);
        warehouseRepository.save(warehouseProduct);
    }

    public BookedProductsDto checkProductCount(ShoppingCartDto shoppingCartDto) throws FeignException {
        BookedProducts bookedProducts = new BookedProducts();
        shoppingCartDto.getProducts().forEach((productId, quantity) -> {
            WarehouseProduct warehouseProduct = warehouseRepository.findById(productId).orElseThrow(() -> new ValidationException("Товар не найден"));
            if (warehouseProduct.getQuantity() < quantity) {
                throw new ValidationException("Product was not found");
            }
            bookedProducts.setFragile(bookedProducts.getFragile() || warehouseProduct.getFragile());
            bookedProducts.setDeliveryVolume(bookedProducts.getDeliveryVolume() + warehouseProduct.getWeight()
                    * warehouseProduct.getDepth() * warehouseProduct.getHeight());
            bookedProducts.setDeliveryWeight(bookedProducts.getDeliveryWeight() + warehouseProduct.getWeight() * quantity);
        });
        return new BookedProductsDto(bookedProducts.getDeliveryWeight(), bookedProducts.getDeliveryVolume(), bookedProducts.getFragile());
    }

    public void addProductQuantity(AddProductToWarehouseRequest request) throws FeignException {
        WarehouseProduct warehouseProduct = warehouseRepository.findById(request.getProductId()).orElseThrow(() -> new ValidationException("Товар не найден"));
        warehouseProduct.setQuantity(request.getQuantity());
        warehouseRepository.save(warehouseProduct);
    }

    public AddressDto getWarehouseAddress() throws FeignException {
        return new AddressDto(CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS, CURRENT_ADDRESS);
    }

    public void returnProductsToWarehouse(Map<UUID, Long> products) throws FeignException {
        List<WarehouseProduct> warehouseProducts = warehouseRepository.findAllById(products.keySet());
        if (warehouseProducts.isEmpty()) {
            return;
        }
        warehouseProducts.forEach(warehouseProduct -> {warehouseProduct.setQuantity(warehouseProduct.getQuantity() +
                products.get(warehouseProduct.getProductId()));});
        warehouseRepository.saveAll(warehouseProducts);
    }
}
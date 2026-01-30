package ru.yandex.practicum.controller;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.service.StoreService;
import ru.yandex.practicum.store.client.StoreClient;
import ru.yandex.practicum.store.dto.ProductDto;
import ru.yandex.practicum.store.enums.ProductCategory;
import ru.yandex.practicum.store.enums.QuantityState;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/shopping-store")
@RequiredArgsConstructor
public class StoreController implements StoreClient {
    private final StoreService storeService;

    @Override
    public Page<ProductDto> getProducts(@RequestParam(name = "category") ProductCategory productCategory, Pageable pageable) throws FeignException {
        log.info("Event - receiving products by category {} on page {}", productCategory, pageable);
        return storeService.getProductsByCategory(productCategory, pageable);
    }

    @Override
    public ProductDto addProduct(ProductDto productDto) throws FeignException {
        log.info("Event - add product {}", productDto);
        return storeService.addProduct(productDto);
    }

    @Override
    public ProductDto updateProduct(ProductDto productDto) throws FeignException {
        log.info("Event - update product {}", productDto);
        return storeService.updateProduct(productDto);
    }

    @Override
    public Boolean removeProductFromStore(String productId) throws FeignException {
        log.info("Event - delete product {}", productId);
        return storeService.removeProductFromStore(UUID.fromString(productId.replace("\"", "")));
    }

    @Override
    public Boolean setQuantityState(UUID productId, QuantityState quantityState) throws FeignException {
        log.info("Event - update quantity {} for product {}", quantityState, productId);
        return storeService.setQuantityState(productId, quantityState);
    }

    @Override
    public ProductDto getProduct(UUID productId) throws FeignException {
        log.info("Event - receipt product with ID: {}", productId);
        return storeService.getProductById(productId);
    }
}
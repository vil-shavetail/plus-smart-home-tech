package ru.yandex.practicum.store.client;

import feign.FeignException;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import ru.yandex.practicum.store.dto.ProductDto;
import ru.yandex.practicum.store.enums.ProductCategory;
import ru.yandex.practicum.store.enums.QuantityState;

import java.util.UUID;

@FeignClient(name = "shopping-store", url = "/api/v1/shopping-store")
public interface StoreClient {
    @GetMapping
    Page<ProductDto> getProducts(@RequestParam ProductCategory productCategory,
                                 @PageableDefault Pageable pageable) throws FeignException;

    @PutMapping
    ProductDto addProduct(@RequestBody @Valid ProductDto productDto) throws FeignException;

    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto) throws FeignException;

    @PostMapping("/removeProductFromStore")
    Boolean removeProductFromStore(@RequestBody String productId) throws FeignException;

    @PostMapping("/quantityState")
    Boolean setQuantityState(@RequestParam UUID productId,
                             @RequestParam QuantityState quantityState) throws FeignException;

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable UUID productId) throws FeignException;
}
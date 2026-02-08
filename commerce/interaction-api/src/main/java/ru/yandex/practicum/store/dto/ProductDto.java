package ru.yandex.practicum.store.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.store.enums.ProductCategory;
import ru.yandex.practicum.store.enums.ProductState;
import ru.yandex.practicum.store.enums.QuantityState;

import java.util.UUID;

@Getter
@Setter
@Builder
public class ProductDto {
    private UUID productId;
    @NotBlank
    private String productName;
    @NotBlank
    private String description;
    private String imageSrc;
    @NotNull(message = "The productId quantityState cannot be empty")
    private QuantityState quantityState;
    @NotNull(message = "The productId productState cannot be empty")
    private ProductState productState;
    @NotNull(message = "The productId productCategory cannot be empty")
    private ProductCategory productCategory;
    @DecimalMin(value = "1.0", message = "The minimum value of the price parameter should be 1")
    @NotNull
    private Double price;
}
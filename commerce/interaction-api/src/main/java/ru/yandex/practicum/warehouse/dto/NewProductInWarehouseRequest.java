package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class NewProductInWarehouseRequest {
    @NotNull(message = "The productId parameter cannot be empty")
    private UUID productId;
    @NotNull(message = "The fragile parameter cannot be empty")
    private boolean fragile;
    @NotNull(message = "The dimension parameter cannot be empty")
    private DimensionDto dimension;
    @DecimalMin(value = "1.0", message = "The minimum value of the weight parameter should be 1")
    private double weight;
}
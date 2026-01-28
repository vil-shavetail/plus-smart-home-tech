package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.constraints.Min;
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
public class AddProductToWarehouseRequest {
    @NotNull(message = "The productId parameter cannot be empty")
    private UUID productId;
    @Min(value = 1, message = "The quantity parameter cannot be empty")
    private Long quantity;
}
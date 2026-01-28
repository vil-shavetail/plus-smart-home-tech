package ru.yandex.practicum.warehouse.dto;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class DimensionDto {
    @DecimalMin(value = "1.0", message = "The minimum value of the width parameter should be 1")
    private double width;
    @DecimalMin(value = "1.0", message = "The minimum value of the height parameter should be 1")
    private double height;
    @DecimalMin(value = "1.0", message = "The minimum value of the depth parameter should be 1")
    private double depth;
}
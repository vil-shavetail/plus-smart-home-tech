package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import ru.yandex.practicum.store.enums.ProductCategory;
import ru.yandex.practicum.store.enums.ProductState;
import ru.yandex.practicum.store.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID productId;
    @Column(nullable = false)
    private String productName;
    @Column(nullable = false)
    private String description;
    private String imageSrc;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuantityState quantityState;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductState productState;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory productCategory;
    @Column(nullable = false)
    private BigDecimal price;
}
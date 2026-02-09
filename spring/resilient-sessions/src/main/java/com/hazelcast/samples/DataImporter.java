package com.hazelcast.samples;

import com.hazelcast.map.IMap;
import com.hazelcast.samples.model.ProductDto;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataImporter {

    @Autowired @Qualifier("products")
    private IMap<Long, ProductDto> products;

    @PostConstruct
    public void init() {
        products.set(1L, new ProductDto(1L, "Coca-Cola", new BigDecimal(8)));
        products.set(2L, new ProductDto(2L, "Pizza Margherita", new BigDecimal(40)));
        products.set(3L, new ProductDto(3L, "Pizza Marinara", new BigDecimal(38)));
        products.set(4L, new ProductDto(4L, "Pizza Salami", new BigDecimal(45)));
    }
}

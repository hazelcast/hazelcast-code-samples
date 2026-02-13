package com.hazelcast.samples;

import com.hazelcast.map.IMap;
import com.hazelcast.samples.model.Basket;
import com.hazelcast.samples.model.BasketItem;
import com.hazelcast.samples.model.ProductDto;
import jakarta.servlet.http.HttpSession;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
public class ShopController {
    private final IMap<Long, ProductDto> products;

    public ShopController(@Qualifier("products") IMap<Long, ProductDto> products) {
        this.products = products;
    }

    @RequestMapping("/products")
    public Collection<ProductDto> getProducts() {
        return products.values().stream().sorted(Comparator.comparing(ProductDto::id)).toList();
    }

    @RequestMapping("/basket/show")
    public List<BasketItem> getBasket(HttpSession session) {
        return getBasketAttr(session).items();
    }

    @RequestMapping("/basket/add")
    public String addToBasket(HttpSession session, @RequestParam("items") Set<Long> items) {
        Basket basket = getBasketAttr(session);
        Map<Long, ProductDto> productDtoMap = products.getAll(items);
        productDtoMap.forEach((key, productDto) -> basket.items().add(new BasketItem(productDto, productDto.listPrice(), 1)));
        session.setAttribute("basket", basket);
        return items.size() + " items added";
    }

    private @NonNull Basket getBasketAttr(HttpSession session) {
        var basket = (Basket) session.getAttribute("basket");
        if (basket == null) {
            basket = new Basket("", new ArrayList<>());
        }
        return basket;
    }
}

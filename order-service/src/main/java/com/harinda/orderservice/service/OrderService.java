package com.harinda.orderservice.service;

import com.harinda.orderservice.dto.InventoryResponse;
import com.harinda.orderservice.dto.OrderLineItemsDto;
import com.harinda.orderservice.dto.OrderRequest;
import com.harinda.orderservice.model.Order;
import com.harinda.orderservice.model.OrderLineItems;
import com.harinda.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest) {

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList()
                .stream()
                .map(this::mapFromDto)
                .toList();

        Order order = Order.builder()
                .orderNumber(UUID.randomUUID().toString())
                .orderLineItemsList(orderLineItemsList)
                .build();

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(OrderLineItems::getSkuCode).collect(Collectors.toSet()).stream().toList();
        // call for inventory-service to check stock avaialbility
        InventoryResponse[] result = webClient.get()
                .uri("http://localhost:8082/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build()) // implement service discovery
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();

        assert result != null;
        boolean allProductsInStock = Arrays.stream(result).allMatch(InventoryResponse::isInStock);
        if(allProductsInStock){
            orderRepository.save(order);
        } else {
            throw new IllegalArgumentException("Product is not in Stock.");
        }
    }

    private OrderLineItems mapFromDto(OrderLineItemsDto orderLineItemsDto) {
        return OrderLineItems.builder()
                .price(orderLineItemsDto.getPrice())
                .skuCode(orderLineItemsDto.getSkuCode())
                .quantity(orderLineItemsDto.getQuantity())
                .build();
    }
}

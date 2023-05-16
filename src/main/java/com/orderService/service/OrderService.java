package com.orderService.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import com.orderService.dto.OrderLineItemsDto;
import com.orderService.dto.OrderRequest;
import com.orderService.model.Order;
import com.orderService.model.OrderLineItems;
import com.orderService.repository.OrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    private final WebClient webClient;

    public void placeOrder(OrderRequest orderRequest){

        Order order = new Order();

        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItems = orderRequest.getOrderLineItemsDtoList().stream().map(orderLineItemsDto -> mapToDto(orderLineItemsDto)).toList();

        order.setOrderLineItemsList(orderLineItems);

        List<String> skuCodes = order.getOrderLineItemsList().stream().map(orderLineItem -> orderLineItem.getSkuCode()).toList();

        //call inventory service

        Boolean result = webClient.get().uri("http//localhost:8082/api/inventory", UriBuilder -> UriBuilder.queryParam("skuCode", skuCodes).build())
            .retrieve()
            .bodyToMono(Boolean.class)
            .block();

        if(result){
            orderRepository.save(order);
        }else{
            throw new IllegalArgumentException("Product is not in stock");
        }
    }

    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){

        OrderLineItems orderLineItems = new OrderLineItems();

        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());

        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        
        return orderLineItems;
    }
    
}

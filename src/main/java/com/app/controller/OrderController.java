package com.app.controller;

import com.app.model.dto.OrderDto;
import com.app.payloads.requests.AddProductToOrderPayload;
import com.app.payloads.requests.ChangeOrderStatusPayload;
import com.app.security.CurrentUser;
import com.app.security.CustomUserDetails;
import com.app.service.OrderService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@Api(tags = "Order controller")
public class OrderController {
    private OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @ApiOperation(
            value = "Add product to order",
            response = OrderDto.class
    )
    @PostMapping
    public OrderDto createOrder(@CurrentUser CustomUserDetails customUserDetails, @RequestBody AddProductToOrderPayload addProductToOrderPayload) {
        return orderService.order(customUserDetails.getId(), addProductToOrderPayload);
    }

    @ApiOperation(
            value = "Reduce orderItem quantity by 1",
            response = OrderDto.class
    )
    @PostMapping("/reduce/{id}")
    public OrderDto reduceOrderQuantity(@PathVariable Long id, @RequestBody AddProductToOrderPayload addProductToOrderPayload){
        return orderService.reduceQuantity(id, addProductToOrderPayload);
    }

    @ApiOperation(
            value = "Delete product from order",
            response = OrderDto.class
    )
    @DeleteMapping("/{orderId}/delete/{productId}")
    public OrderDto deleteProductFromOrder(@PathVariable Long orderId,@PathVariable Long productId) {
        return orderService.deleteProductFromOrder(orderId, productId);
    }

    @ApiOperation(
            value = "Fetch all orders",
            response = OrderDto.class
    )
    @GetMapping
    public List<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/current")
    public List<OrderDto> getAllCurrentOrders() {
        return orderService.getQueueOrders();
    }

    @ApiOperation(
            value = "Get one order",
            response = OrderDto.class
    )
    @GetMapping("/{id}")
    public OrderDto getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @ApiOperation(
            value = "Change order status",
            response = OrderDto.class
    )
    @PostMapping("/{id}")
    public OrderDto changeOrderStatus(@PathVariable Long id, @RequestBody ChangeOrderStatusPayload changeOrderStatusPayload) {
        return orderService.changeOrderStatus(id, changeOrderStatusPayload);
    }

    @ApiOperation(
            value = "Get order position",
            response = Integer.class
    )
    @PostMapping("/orderPosition/{id}")
    public Integer getQueuePosition(@PathVariable Long id) {
        return orderService.getUserQueuePosition(id);
    }
}

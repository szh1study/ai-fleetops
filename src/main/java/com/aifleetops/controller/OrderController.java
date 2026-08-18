package com.aifleetops.controller;

import com.aifleetops.common.Result;
import com.aifleetops.entity.Order;
import com.aifleetops.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<List<Order>> list() {
        return Result.success(orderService.list());
    }

    @GetMapping("/{id}")
    public Result<Order> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    @PostMapping
    public Result<Order> create(@RequestBody Order order) {
        orderService.save(order);
        return Result.success(order);
    }

    @PutMapping("/{id}")
    public Result<Order> update(@PathVariable Long id, @RequestBody Order order) {
        order.setId(id);
        orderService.updateById(order);
        return Result.success(order);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        orderService.removeById(id);
        return Result.success();
    }
}

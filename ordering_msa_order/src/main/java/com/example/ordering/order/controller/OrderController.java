package com.example.ordering.order.controller;

import com.example.ordering.common.CommonResponse;
import com.example.ordering.order.domain.Ordering;
import com.example.ordering.order.dto.OrderReqDto;
import com.example.ordering.order.dto.OrderResDto;
import com.example.ordering.order.service.OrderService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/order/create")
    public ResponseEntity<CommonResponse> orderCreate(@RequestBody List<OrderReqDto> orderReqDtos, @RequestHeader("myEmail") String email) {
        Ordering ordering = orderService.create(orderReqDtos, email);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.CREATED, "order success create", ordering.getId()), HttpStatus.CREATED);
    }

    @GetMapping("/orders")
    public List<OrderResDto> orderList() {
        return orderService.findAll();
    }


/*
    @DeleteMapping("/order/{id}/cancle")

    public ResponseEntity<CommonResponse> orderCancel(@PathVariable Long id) {
        Ordering ordering = orderService.cancel(id);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.OK, "order success cancel", ordering.getId()), HttpStatus.CREATED);
    }
    */
}

package com.example.ordering.order.dto;


import com.example.ordering.order.domain.Ordering;
import com.example.ordering.order_item.domain.OrderItem;
import lombok.Data;
import org.aspectj.weaver.ast.Or;

import java.util.ArrayList;
import java.util.List;

@Data
public class OrderResDto {
    private Long id;
    private String memberEmail;
    private String orderStatus;
    private List<OrderResDto.orderResItemDtos> orderItems;

    @Data
    public static class orderResItemDtos {
        private Long id;
        private String itemName;
        private int count;
    }

    public static OrderResDto toDto(Ordering ordering) {
        OrderResDto orderResDto = new OrderResDto();
        orderResDto.setId(ordering.getId());
        orderResDto.setMemberEmail(ordering.getMember().getEmail());
        orderResDto.setOrderStatus(ordering.getOrderStatus().toString());
        List<OrderResDto.orderResItemDtos> orderResItemDtos = new ArrayList<>();
        for (OrderItem orderItem : ordering.getOrderItems()) {
            OrderResDto.orderResItemDtos dto = new OrderResDto.orderResItemDtos();
            dto.setId(orderItem.getId());
            dto.setItemName(orderItem.getItem().getName());
            dto.setCount(orderItem.getQuantity());
            orderResItemDtos.add(dto);
        }
        orderResDto.setOrderItems(orderResItemDtos);

        return orderResDto;
    }
}

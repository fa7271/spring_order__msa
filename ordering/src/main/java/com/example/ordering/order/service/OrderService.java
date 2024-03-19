package com.example.ordering.order.service;

import com.example.ordering.common.CommonResponse;
import com.example.ordering.item.domain.Item;
import com.example.ordering.item.repository.ItemRepository;
import com.example.ordering.member.domain.Member;
import com.example.ordering.member.repository.MemberRepository;
import com.example.ordering.order.domain.OrderStatus;
import com.example.ordering.order.domain.Ordering;
import com.example.ordering.order.dto.OrderReqDto;
import com.example.ordering.order.dto.OrderResDto;
import com.example.ordering.order.repository.OrderRepository;
import com.example.ordering.order_item.domain.OrderItem;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;

    public OrderService(OrderRepository orderRepository, MemberRepository memberRepository, ItemRepository itemRepository) {
        this.orderRepository = orderRepository;
        this.memberRepository = memberRepository;
        this.itemRepository = itemRepository;
    }

    public Ordering create(List<OrderReqDto> orderReqDtos) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("not found email"));
        Ordering ordering = Ordering.builder().member(member).build();

//        OderingItem객체도 함께 생성
        for (OrderReqDto dto : orderReqDtos) {
            Item item = itemRepository.findById(dto.getItemId()).orElseThrow(() -> new EntityNotFoundException("Item not found"));
            OrderItem orderItem = OrderItem.builder()
                    .item(item)
                    .quantity(dto.getCount())
                    .ordering(ordering)
                    .build();
            ordering.getOrderItems().add(orderItem);
            if (item.getStockQuantity() - dto.getCount() < 0) {
                throw new IllegalArgumentException("제고 부족합니다.");
            }
            orderItem.getItem().updateStockQuantity(item.getStockQuantity() - dto.getCount());
        }
        orderRepository.save(ordering);
        return ordering;
    }


    public Ordering cancel(long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Ordering ordering = orderRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("order not found"));

        // 로드인 한 아이디가 email이 아니고 어드민이 아닐때 오륰
        if (!ordering.getMember().getEmail().equals(email) && !authentication.getAuthorities().contains((new SimpleGrantedAuthority("ROLE_ADMIN")))) {

            throw new AccessDeniedException("권한 오류");
        }
        if (ordering.getOrderStatus() == OrderStatus.CANCLED) {
            throw new IllegalArgumentException("이미 취소된 주문입니다..");
        }

        ordering.cancleOrder();
        for (OrderItem orderItem : ordering.getOrderItems()) {
            orderItem.getItem().updateStockQuantity(
                    orderItem.getItem().getStockQuantity() + orderItem.getQuantity()
            );
        }
        return ordering;
    }

    public List<OrderResDto> findAll() {
        List<Ordering> orderings = orderRepository.findAll();
        return orderings.stream().map(x -> OrderResDto.toDto(x)).collect(Collectors.toList());
    }

    /*
    관리자가 보는 멤버별 주문횟수.
     */
    public List<OrderResDto> findByMember(Long id) {
//        Member member = memberRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("not found member"));
//        List<Ordering> orderings = member.getOrderings();
        List<Ordering> orderings = orderRepository.findByMemberId(id);
        return orderings.stream().map(x -> OrderResDto.toDto(x)).collect(Collectors.toList());
    }

    /*
    마이페이지
     */
    public List<OrderResDto> findMyOrders() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("없는 사용자 입니다."));
        List<Ordering> orderings = member.getOrderings();
        return orderings.stream().map(x -> OrderResDto.toDto(x)).collect(Collectors.toList());
    }
}

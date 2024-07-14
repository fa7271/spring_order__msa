package com.example.ordering.order.service;

import com.example.ordering.common.CommonResponse;
import com.example.ordering.order.dto.*;
import com.example.ordering.order.domain.Ordering;
import com.example.ordering.order.repository.OrderRepository;
import com.example.ordering.order.domain.OrderItem;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    public OrderService(OrderRepository orderRepository, RestTemplate restTemplate) {
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    private final String MEMBER_API = "http://member-service/";
    private final String ITEM_API = "http://item-service/";
    public Ordering create(List<OrderReqDto> orderReqDtos, String email) {
        String url = MEMBER_API + "/member/findByEmail?email=" + email;
        MemberDto member = restTemplate.getForObject(url, MemberDto.class);
        Ordering ordering = Ordering.builder().memberId(member.getId()).build();
        List<ItemQuantityUpdateDto> itemQuantityUpdateDtos = new ArrayList<>();
//        Ordering 객체가 생성될 때 OrderingItem객체도 함께 생성 : cascading
        for(OrderReqDto dto : orderReqDtos) {
            OrderItem orderItem = OrderItem.builder()
                    .ordering(ordering)
                    .itemId(dto.getItemId())
                    .quantity(dto.getCount())
                    .build();
            ordering.getOrderItems().add(orderItem);
            String itemUrl = ITEM_API + "/item/" + dto.getItemId();
            // getForEntity는 ResponseEntity로 반환
            ResponseEntity<ItemDto> itemResponseEntity =  restTemplate.getForEntity(itemUrl, ItemDto.class);
            // getBody()로 ResponseEntity안의 ItemDto를 꺼냄
            if(itemResponseEntity.getBody().getStockQuantity() - dto.getCount() < 0) {
                throw new IllegalArgumentException("재고가 부족합니다.");
            }
            int newQuantity = itemResponseEntity.getBody().getStockQuantity() - dto.getCount();

            ItemQuantityUpdateDto updateDto = new ItemQuantityUpdateDto();
            updateDto.setId(dto.getItemId());
            updateDto.setStockQuantity(newQuantity);
            itemQuantityUpdateDtos.add(updateDto);

        }
        Ordering ordering1 = orderRepository.save(ordering); // OrderItem:케스케이딩, Item:더티체킹
//        save를 먼저 함으로서 위 코드에서 에러 발생 시 item 서비스가 호출되지 않으므로 트랜잭션 문제 발생하지 않음
        String itemPatchUrl = ITEM_API + "/item/updateQuantity";
        HttpEntity<List<ItemQuantityUpdateDto>> entity = new HttpEntity<>(itemQuantityUpdateDtos);
        ResponseEntity<CommonResponse> response =
                restTemplate.exchange(itemPatchUrl, HttpMethod.POST, entity, CommonResponse.class);
//        만약 위 update 이후에 추가적인 로직이 존재할 경우, 트랜잭션 이슈는 여전히 발생됨
//        해결책으로 에러 발생할 가능성이 있는 코드전체를 try/catch로 예외처리 이후에 catch에서 updateRollbackQuantity호출
        return ordering1;
    }


/*

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
*/

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
    public List<OrderResDto> findMyOrders(Long memberId) {
        List<Ordering> orderings = orderRepository.findByMemberId(memberId);
        return orderings.stream().map(x -> OrderResDto.toDto(x)).collect(Collectors.toList());
    }
}

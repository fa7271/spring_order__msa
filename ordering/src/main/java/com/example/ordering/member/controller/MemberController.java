package com.example.ordering.member.controller;

import com.example.ordering.common.CommonResponse;
import com.example.ordering.member.domain.Member;
import com.example.ordering.member.dto.LoginReqDto;
import com.example.ordering.member.dto.MemberCreateReqDto;
import com.example.ordering.member.dto.MemberResponseDto;
import com.example.ordering.member.service.MemberService;
import com.example.ordering.order.dto.OrderResDto;
import com.example.ordering.order.service.OrderService;
import com.example.ordering.securities.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final OrderService orderService;


    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider, OrderService orderService) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.orderService = orderService;
    }

    @PostMapping("/member/create")
    public ResponseEntity<CommonResponse> memberCreate(@Valid @RequestBody MemberCreateReqDto memberCreateReqDto) {
        Member member = memberService.create(memberCreateReqDto);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.CREATED, "member successfully created", member.getId()), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members")
    public List<MemberResponseDto> members() {
        return memberService.findAll();
    }

    @GetMapping("/member/myinfo")
    public MemberResponseDto findMyInfo() {
        return memberService.findMyInfo();
    }

//    @PreAuthorize("hasRole('ADMIN') or #email == authentication.principal.username") // username , admin 체크

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/{id}/orders")
    public List<OrderResDto> findMemberOrders(@PathVariable Long id) {
        return orderService.findByMember(id);
    }

    //    마이 페이지
    @GetMapping("/member/myorders")
    public List<OrderResDto> findMyOrders() {
        return orderService.findMyOrders();
    }


    @PostMapping("/doLogin")
    public ResponseEntity<CommonResponse> memberLogin(@Valid @RequestBody LoginReqDto loginReqDto) {
        Member member = memberService.login(loginReqDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        Map<String, Object> member_info = new HashMap<>();
        member_info.put("id", member.getId());
        member_info.put("token", jwtToken);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.OK, "member successfully logined", member_info), HttpStatus.OK);
    }

//    관리자가 보는 페이지
//    @GetMapping("/member/{id}/orders")

//    @GetMapping("member/myorders")
}

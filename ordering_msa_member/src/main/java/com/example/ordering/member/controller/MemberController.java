package com.example.ordering.member.controller;

import com.example.ordering.common.CommonResponse;
import com.example.ordering.member.domain.Member;
import com.example.ordering.member.dto.LoginReqDto;
import com.example.ordering.member.dto.MemberCreateReqDto;
import com.example.ordering.member.dto.MemberResponseDto;
import com.example.ordering.member.service.MemberService;
import com.example.ordering.securities.JwtTokenProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;


    public MemberController(MemberService memberService, JwtTokenProvider jwtTokenProvider) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/member/create")
    public ResponseEntity<CommonResponse> memberCreate(@Valid @RequestBody MemberCreateReqDto memberCreateReqDto) {
        Member member = memberService.create(memberCreateReqDto);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.CREATED, "member successfully created", member.getId()), HttpStatus.CREATED);
    }


    //    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/members")
    public List<MemberResponseDto> members() {
        return memberService.findAll();
    }


    @GetMapping("/member/myinfo")
    public MemberResponseDto findMyInfo(@RequestHeader("myEmail") String email) {
        return memberService.findMyInfo(email);
    }

/*

    @GetMapping("/member/{id}/orders")
    public List<OrderResDto> findMemberOrders(@PathVariable Long id) {
        return orderService.findByMember(id);
    }

    //    마이 페이지
    @GetMapping("/member/myorders")
    public List<OrderResDto> findMyOrders() {
        return orderService.findMyOrders();
    }
*/


    @PostMapping("/doLogin")
    public ResponseEntity<CommonResponse> memberLogin(@Valid @RequestBody LoginReqDto loginReqDto) {
        Member member = memberService.login(loginReqDto);
        String jwtToken = jwtTokenProvider.createToken(member.getEmail(), member.getRole().toString());
        Map<String, Object> member_info = new HashMap<>();
        member_info.put("id", member.getId());
        member_info.put("token", jwtToken);
        return new ResponseEntity<>(new CommonResponse(HttpStatus.OK, "member successfully logined", member_info), HttpStatus.OK);
    }

    @GetMapping("member/{id}")
    public MemberResponseDto findById(@PathVariable Long id) {
        return memberService.findById(id);
    }
    @GetMapping("member/findByEmail")
    public MemberResponseDto findByEmail(@RequestParam String email) {
        return memberService.findByEmail(email);
    }

//    관리자가 보는 페이지
//    @GetMapping("/member/{id}/orders")

//    @GetMapping("member/myorders")
}

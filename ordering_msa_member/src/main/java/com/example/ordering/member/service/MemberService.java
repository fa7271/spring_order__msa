package com.example.ordering.member.service;

import com.example.ordering.member.domain.Address;
import com.example.ordering.member.domain.Member;
import com.example.ordering.member.domain.Role;
import com.example.ordering.member.dto.LoginReqDto;
import com.example.ordering.member.dto.MemberCreateReqDto;
import com.example.ordering.member.dto.MemberResponseDto;
import com.example.ordering.member.repository.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

//    fromDtoToEntity
    public Member create(MemberCreateReqDto memberCreateReqDto) {
        Optional<Member> check_email = memberRepository.findByEmail(memberCreateReqDto.getEmail());
        System.out.println(check_email);
        if (check_email.isEmpty()) {
            memberCreateReqDto.setPassword(passwordEncoder.encode(memberCreateReqDto.getPassword()));
            Member member = Member.toEntity(memberCreateReqDto);
            return memberRepository.save(member);
        }
        else {
            throw new IllegalArgumentException("이미 있는 이메일 입니다.");
        }
    }

     /*
     * 로그인
      */
    public Member login(LoginReqDto loginReqDto) throws IllegalArgumentException{
        Member member = memberRepository.findByEmail(loginReqDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 이메일입니다."));
//        create Token
        if (!passwordEncoder.matches(loginReqDto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        return member;
    }

    /*
     * 내 정보 찾기.
     */
    public MemberResponseDto findMyInfo(String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
        return MemberResponseDto.toMemberResponseDto(member);
    }

    public List<MemberResponseDto> findAll() {
        List<Member> members = memberRepository.findAll();
        return members.stream().map(member -> MemberResponseDto.toMemberResponseDto(member)).collect(Collectors.toList());
    }

    public MemberResponseDto findById(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(EntityNotFoundException::new);
        return MemberResponseDto.toMemberResponseDto(member);

    }

    public MemberResponseDto findByEmail(@RequestParam String email) {
        Member member = memberRepository.findByEmail(email).orElseThrow(EntityNotFoundException::new);
        return MemberResponseDto.toMemberResponseDto(member);
    }
}

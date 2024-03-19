package com.example.ordering.member.domain;

import com.example.ordering.member.dto.MemberCreateReqDto;
import com.example.ordering.order.domain.Ordering;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Embedded
    private Address address;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
    @OneToMany(mappedBy = "member")
    private List<Ordering> orderings;

    @CreationTimestamp
    LocalDateTime createdTime;
    @UpdateTimestamp
    LocalDateTime updatedTime;

    //    @Column(nullable = false)
//    private String address;
//    @Enumerated(EnumType.STRING)
//    private Role role;
    public static Member toEntity(MemberCreateReqDto memberCreateReqDto) {
        Address address = new Address(memberCreateReqDto.getCity(), memberCreateReqDto.getStreet(), memberCreateReqDto.getZipcode());
        return Member.builder()
                .email(memberCreateReqDto.getEmail())
                .password(memberCreateReqDto.getPassword())
                .name(memberCreateReqDto.getName())
                .address(address)
                .role(Role.USER)
                .build();
    }
}

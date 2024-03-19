package com.example.ordering.member.dto;

import com.example.ordering.member.domain.Address;
import com.example.ordering.member.domain.Member;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Getter
@Builder
public class MemberResponseDto {

    private Long id ;
    private String name;
    private String email;
    private String city;
    private String street;
    private String zipcode;
    private int orderCount;

//    중복된 코드들
    public static MemberResponseDto toMemberResponseDto(Member member) {
        MemberResponseDtoBuilder builder = MemberResponseDto.builder();
        Address address = member.getAddress();

        builder.name(member.getName());
        builder.email(member.getEmail());
        builder.id(member.getId());
        builder.orderCount(member.getOrderings().size());
        if (address != null) {
            builder.city(address.getCity());
            builder.street(address.getStreet());
            builder.zipcode(address.getZipcode());
        }

        return builder.build();
    }
}

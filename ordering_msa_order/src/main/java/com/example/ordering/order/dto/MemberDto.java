package com.example.ordering.order.domain;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class MemberDto {

    private Long id ;
    private String name;
    private String email;
    private String city;
    private String street;
    private String zipcode;
    private int orderCount;

}

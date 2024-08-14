package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.member.domain.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResDto {
    private Long id;
    private String name;
    private String email;
    private Address address;
    private int orderCount;

    public MemberResDto fromEntity(Member member) {
        return builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .address(member.getAddress())
                .build();
    }
}

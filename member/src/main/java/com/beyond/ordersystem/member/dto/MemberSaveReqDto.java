package com.beyond.ordersystem.member.dto;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.domain.Role;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberSaveReqDto {

    //  간단한 validation check
    //  validation dependency 필요함
    @NotEmpty(message = "email is essential")
    private String email;
    private String name;
    @Builder.Default
    private Role role = Role.USER;

    @NotEmpty(message = "password is essential")
    @Size(min = 8, message = "password minimum length is 8")
    private String password;

    private Address address;

    public Member toEntity(String password) {
        return Member.builder()
                .email(this.email)
                .name(this.name)
                .password(password)
                .role(this.role)
                .address(this.address)
                .build();
    }
}
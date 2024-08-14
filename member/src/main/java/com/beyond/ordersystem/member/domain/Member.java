package com.beyond.ordersystem.member.domain;

import com.beyond.ordersystem.common.domain.Address;
import com.beyond.ordersystem.common.domain.BaseTimeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Getter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty(message = "Email is essential")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email should not be longer than 255 characters")
    @Column(nullable = false, unique = true)
    private String email;

    @NotEmpty(message = "Name is essential")
    @Size(max = 100, message = "Name should not be longer than 100 characters")
    @Column(nullable = false)
    private String name;

    @NotEmpty(message = "Password is essential")
    @Size(min = 8, message = "Password minimum length is 8")
    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Embedded
    private Address address;

    public void updatePassword(String password) {
        this.password = password;
    }
}

package com.beyond.ordersystem.member.service;

import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.MemberLoginDto;
import com.beyond.ordersystem.member.dto.MemberResDto;
import com.beyond.ordersystem.member.dto.MemberSaveReqDto;
import com.beyond.ordersystem.member.dto.ResetPasswordDto;
import com.beyond.ordersystem.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member memberCreate(MemberSaveReqDto dto) {
        if (memberRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("중복된 이메일");
        }
        return memberRepository.save(dto.toEntity(passwordEncoder.encode(dto.getPassword())));
    }

   public Member login(MemberLoginDto dto) {
        //  email 의 존재여부
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new EntityNotFoundException("존재하지 않는 이메일입니다."));
        //  password 일치여부
        if (!passwordEncoder.matches(dto.getPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다");
        }
        return member;
    }

    public Page<MemberResDto> memberList(Pageable pageable) {
        return memberRepository.findAll(pageable).map(member -> new MemberResDto().fromEntity(member));
    }

    public MemberResDto memberInfo() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("member is not found"));
        return new MemberResDto().fromEntity(member);
    }

    public void resetPassword(ResetPasswordDto dto) {
        Member member = memberRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new EntityNotFoundException("not found"));
        if (!passwordEncoder.matches(dto.getAsIsPassword(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        member.updatePassword(passwordEncoder.encode(dto.getToBePassword()));
    }
}


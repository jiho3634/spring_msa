package com.beyond.ordersystem.member.controller;
import com.beyond.ordersystem.common.auth.JwtTokenProvider;
import com.beyond.ordersystem.common.dto.CommonErrorDto;
import com.beyond.ordersystem.common.dto.CommonResDto;
import com.beyond.ordersystem.member.domain.Member;
import com.beyond.ordersystem.member.dto.*;
import com.beyond.ordersystem.member.service.MemberService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Validated
@RestController
public class MemberController {
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Qualifier("2")
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secretKeyRt}")
    private String secretKeyRt;

    @Autowired
    MemberController(JwtTokenProvider jwtTokenProvider, MemberService memberService, @Qualifier("2") RedisTemplate<String, Object> redisTemplate) {
        this.memberService = memberService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.redisTemplate = redisTemplate;
    }

    @PostMapping("/member/create")
    public ResponseEntity<?> memberCreate(@Valid @RequestBody MemberSaveReqDto dto) {
        try {
            Long id = memberService.memberCreate(dto).getId();
            HttpStatus sig = HttpStatus.CREATED;
            String msg = "Member is successfully created";
            CommonResDto commonResDto = new CommonResDto(sig, msg, id);
            return new ResponseEntity<>(commonResDto, sig);
        } catch (IllegalArgumentException e) {
            HttpStatus sig = HttpStatus.BAD_REQUEST;
            String msg = "BAD REQUEST";
            return new ResponseEntity<>(new CommonErrorDto(sig.value(), msg), sig);
        }
    }

   @PostMapping("/doLogin")
    public ResponseEntity<?> doLogin(@RequestBody MemberLoginDto dto) {
        //  email, password 가 일치하는지 검증
        Member member = memberService.login(dto);

        //  일치하는 경우 accessToken 생성
        String jwtToken = jwtTokenProvider
                .createToken(member.getEmail(), member.getRole().toString());

        //
       String refreshToken = jwtTokenProvider
               .createRefreshToken(member.getEmail(), member.getRole().toString());

       //   redis 에 email 과 rt 를 key:value 로 하여 저장
       redisTemplate.opsForValue().set(member.getEmail(), refreshToken, 240, TimeUnit.HOURS);   // 240 시간
        //  생성된 토큰을 CommonResDto 에 담아 사용자에게 return

        Map<String, Object> loginInfo = new HashMap<>();
        loginInfo.put("id", member.getId());
        loginInfo.put("token", jwtToken);
        loginInfo.put("refreshToken", refreshToken);
        String msg = "login is successful";
        HttpStatus sig = HttpStatus.OK;
        return new ResponseEntity<>(new CommonResDto(sig, msg, loginInfo), sig);
    }

    //  admin 만 회원 목록 전체 조회 가능
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/member/list")
    public ResponseEntity<?> memberList(Pageable pageable) {
        Page<MemberResDto> list = memberService.memberList(pageable);
        String msg = "members are found";
        HttpStatus sig = HttpStatus.OK;
        return new ResponseEntity<>(new CommonResDto(sig, msg, list), sig);
    }

    //  본인은 본인 회원 정보만 조회 가능
    //  MemberResDto 로 반환
    @GetMapping("/member/myinfo")
    public ResponseEntity<?> memberInfo() {
        MemberResDto dto = memberService.memberInfo();
        String msg = "memberInfo is successfully taken";
        HttpStatus sig = HttpStatus.OK;
        return new ResponseEntity<>(new CommonResDto(sig, msg, dto), sig);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> generateNewAccessToken(@RequestBody MemberRefreshDto dto) {
        Claims claims = null;
        String rt = dto.getRefreshToken();
        try {
            claims = Jwts.parser().setSigningKey(secretKeyRt).parseClaimsJws(rt).getBody();
        } catch (Exception e) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.BAD_REQUEST.value(),  "invalid refresh token"), HttpStatus.BAD_REQUEST);
        }

        String email = claims.getSubject();
        String role = claims.get("role").toString();

        Object obj = redisTemplate.opsForValue().get(email);
        if (obj == null || !obj.toString().equals(rt)) {
            return new ResponseEntity<>(new CommonErrorDto(HttpStatus.UNAUTHORIZED.value(), "token is not found"), HttpStatus.UNAUTHORIZED);
        }
        String newAt = jwtTokenProvider.createToken(email, role);

        Map<String, Object> info = new HashMap<>();
        info.put("token", newAt);
        String msg = "login is successful";
        HttpStatus sig = HttpStatus.OK;
        return new ResponseEntity<>(new CommonResDto(sig, msg, info), sig);
    }

    @PatchMapping("/member/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        memberService.resetPassword(resetPasswordDto);
        CommonResDto commonResDto = new CommonResDto(HttpStatus.OK, "password is renewed", "ok");
        return new ResponseEntity<>(commonResDto, HttpStatus.OK);
    }
}

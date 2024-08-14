package com.beyond.ordersystem.common.configs;
import com.beyond.ordersystem.common.auth.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//  Spring Security 사용하겠따.
@EnableWebSecurity
//  사전검증 : 사용자의 요청이 스프링으로 들어오기 전에 인증을 검증하겠다.
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfigs {
    @Autowired
    private JwtAuthFilter jwtAuthFilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        //  filter 단계
        return httpSecurity
                .csrf().disable()
                //  Cross Origin Resource Sharing 다른 도메인에서 서버로 요청하는 것을 금지
                .cors().and()
                .httpBasic().disable()
                .authorizeRequests()
                .antMatchers("/member/create", "/", "/doLogin", "/refresh-token", "/product/list", "/member/reset-password")
                .permitAll()
                .anyRequest().authenticated()
                .and()
                //  세션로그인이 아닌 stateless token 을 사용
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                //  로그인 시 발급받은 토큰을 요청 때마다 http header 에 넣음.
                //  사용자의 토큰이 정상인지 검증
                //  세션 방식에서는 세션 ID를 저장한 후 비교하여 검증했지만, 토큰은 서버가 저장하고 있지 않기 때문에 검증하는 필터로직이 필요하다.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}

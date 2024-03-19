package com.example.ordering.securities;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@Slf4j
public class JwtTokenProvider {
    @Value("${jwt.expiration}")
    private int expiration;

    @Value("${jwt.secretKey}")
    private String secretKey;

    public String createToken(String email, String role) {
//        claims : 토큰 사용자에 대한 속성이나 데이터포함, -> 주로 페이로드 의미 (토큰안에 2번쨰꺼)

        Claims claims = Jwts.claims().setSubject(email);
        claims.put("role", role);
        Date now = new Date();
        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiration * 60 * 1000L ))  // 유지보수가 어려움, yml로 해서 공통화
                .signWith(SignatureAlgorithm.HS256, secretKey) // 위에 동일한 이유
                .compact();

        return token;
    }
/*

//    만료시간 생성 굳이 이렇게 할 필요는 없다.
    public static Date getExpiration() {
        return Date.from(
            LocalDateTime.now()
            .plusMinutes(30)
            .atZone(ZoneId.systemDefault())    // 시스템 기본 시간대로 변환
            .toInstant()                        // Instant로 변환
        );

    }*/
}

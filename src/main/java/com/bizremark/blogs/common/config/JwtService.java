package com.bizremark.blogs.common.config;

import com.bizremark.blogs.user.info.UserInfo;
import com.bizremark.blogs.user.mapper.UserInfoMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {
    // BUG: MOVE TO APPLICATION DEV
    private static final String SECRET_KEY = "0459022f685c9af39d232c64d010892e406dfea4ec282e36d00512ce33401180";
    // BUG: MOVE TO APPLICATION DEV
    private static final Integer TOKEN_VALIDITY = 1000 * 60 * 24;

    private final UserInfoMapper userInfoMapper;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && Boolean.FALSE.equals(isTokenExpired(token));
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String generateToken(UserInfo userInfo) {
        return generateToken(new HashMap<>(), userInfo);
    }

    public String generateToken(Map<String, Object> claims, UserInfo userInfo) {
        var user = userInfoMapper.userInfoToUser(userInfo);
        return Jwts.builder()
                .subject(user.getUsername())
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + TOKEN_VALIDITY))
                .signWith(getSignInKey())
                .compact();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsTFunction) {
        final Claims claims = extractAllClaims(token);
        return claimsTFunction.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
package com.inf5190.chat.auth.session;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Repository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Repository
public class SessionManager {
    public static final long HOUR = 3600*1000;
    private static final String SECRET_KEY_BASE64 = "xhHdo8JHSui0ZJ+sKShBNh10c4Vtth62/pOEpCVWWIA=";
    private final SecretKey secretKey;
    private final JwtParser jwtParser;
    
    public SessionManager() {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET_KEY_BASE64));
        this.jwtParser = Jwts.parserBuilder().setSigningKey(this.secretKey).build();
    }

    public String addSession(SessionData authData) {
        String username = authData.username();
        Date issuedAt = new Date();
        Date expiration = new Date(issuedAt.getTime() + 2 * HOUR);
        String jws = Jwts.builder()
            .setAudience("AppChat")
            .setIssuedAt(issuedAt)
            .setSubject(username)
            .setExpiration(expiration)
            .signWith(secretKey)
            .compact();
        return jws;
    }

    public void removeSession(String token) {
    }

    public SessionData getSession(String token) {
        try {
            Jws<Claims> jws = jwtParser.parseClaimsJws(token);
            String username = jws.getBody().getSubject();
            return new SessionData(username);
        } catch (JwtException ex) {
            return null;
        }
    }
}

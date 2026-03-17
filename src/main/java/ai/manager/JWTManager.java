package ai.manager;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JWTManager {

    @Value("${jwt.secret}")
    private String secretKey;

    @PostConstruct
    public void debugSecret() {
        System.out.println("JWT SECRET LOADED: " + secretKey);
        System.out.println("JWT SECRET LENGTH: " + secretKey.length());
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String email, int role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateTokenAndGetClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("JWT PARSE ERROR: " + e.getMessage());
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        return claims == null ? null : claims.getSubject();
    }

    public Integer getRoleFromToken(String token) {
        Claims claims = validateTokenAndGetClaims(token);
        if (claims == null) return null;

        Object roleObj = claims.get("role");
        return roleObj == null ? null : Integer.parseInt(roleObj.toString());
    }
}

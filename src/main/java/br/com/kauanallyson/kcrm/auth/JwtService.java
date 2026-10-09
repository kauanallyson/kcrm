package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.dto.auth.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.MacAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class JwtService {
    private static final String TOKEN_TYPE = "Bearer";
    private static final MacAlgorithm ALGORITHM = Jwts.SIG.HS256;

    private final SecretKey key;
    private final Duration expiration;

    public JwtService(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration}") Duration expiration) {
        this.key = toKey(secret);
        this.expiration = expiration;
    }

    // Fail at startup with a clear message instead of on the first login
    private static SecretKey toKey(String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT_SECRET is not set. Generate one with: openssl rand -base64 32");
        }
        try {
            return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        } catch (RuntimeException e) {
            throw new IllegalStateException("JWT_SECRET must be base64 and at least 256 bits. Generate one with: openssl rand -base64 32", e);
        }
    }

    public TokenResponse issue(UUID corretorId) {
        Date now = new Date();
        String token = Jwts.builder()
                .subject(corretorId.toString())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration.toMillis()))
                .signWith(key, ALGORITHM)
                .compact();
        return new TokenResponse(token, TOKEN_TYPE, expiration.toSeconds());
    }

    public Optional<UUID> parseSubject(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return subjectOf(jws);
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Optional<UUID> subjectOf(Jws<Claims> jws) {
        if (!ALGORITHM.getId().equals(jws.getHeader().getAlgorithm())) {
            return Optional.empty();
        }
        return Optional.of(UUID.fromString(jws.getPayload().getSubject()));
    }
}

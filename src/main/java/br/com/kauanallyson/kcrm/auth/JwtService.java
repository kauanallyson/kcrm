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
    private static final String PAPEL_CLAIM = "papel";

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

    // Token sem papel (ou com papel desconhecido) não resolve
    private static Optional<Sujeito> sujeitoOf(Jws<Claims> jws) {
        if (!ALGORITHM.getId().equals(jws.getHeader().getAlgorithm())) {
            return Optional.empty();
        }
        Papel papel = Papel.valueOf(jws.getPayload().get(PAPEL_CLAIM, String.class));
        return Optional.of(new Sujeito(UUID.fromString(jws.getPayload().getSubject()), papel));
    }

    public TokenResponse issue(UUID id, Papel papel) {
        Date now = new Date();
        String token = Jwts.builder()
                .subject(id.toString())
                .claim(PAPEL_CLAIM, papel.name())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration.toMillis()))
                .signWith(key, ALGORITHM)
                .compact();
        return new TokenResponse(token, TOKEN_TYPE, expiration.toSeconds());
    }

    public Optional<Sujeito> parse(String token) {
        try {
            Jws<Claims> jws = Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return sujeitoOf(jws);
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            return Optional.empty();
        }
    }
}

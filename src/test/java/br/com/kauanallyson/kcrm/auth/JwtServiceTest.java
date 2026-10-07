package br.com.kauanallyson.kcrm.auth;

import br.com.kauanallyson.kcrm.TestJwt;
import br.com.kauanallyson.kcrm.dto.TokenResponse;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private static final String SECRET = TestJwt.randomSecret();
    private static final String OTHER_SECRET = TestJwt.randomSecret();
    private static final UUID ALICE_ID = UUID.randomUUID();

    private final JwtService jwtService = new JwtService(SECRET, Duration.ofMinutes(1));

    @Test
    void issuedTokenParsesToItsSubject() {
        TokenResponse response = jwtService.issue(ALICE_ID);

        assertThat(response.type()).isEqualTo("Bearer");
        assertThat(response.expiresIn()).isEqualTo(60);
        assertThat(jwtService.parseSubject(response.token())).contains(ALICE_ID);
    }

    @Test
    void tokenSignedWithAnotherKeyIsRejected() {
        String forged = new JwtService(OTHER_SECRET, Duration.ofMinutes(1))
                .issue(ALICE_ID).token();

        assertUnresolved(forged);
    }

    @Test
    void tamperedTokenIsRejected() {
        String[] parts = jwtService.issue(ALICE_ID).token().split("\\.");
        String tampered = parts[0] + "." + parts[1] + "x." + parts[2];

        assertUnresolved(tampered);
    }

    @Test
    void expiredTokenIsRejected() {
        String expired = Jwts.builder()
                .subject(ALICE_ID.toString())
                .issuedAt(new Date(System.currentTimeMillis() - 120_000))
                .expiration(new Date(System.currentTimeMillis() - 60_000))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertUnresolved(expired);
    }

    @Test
    void tokenWithNonUuidSubjectIsRejected() {
        String legacy = Jwts.builder()
                .subject("alice@test.com")
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET)))
                .compact();

        assertUnresolved(legacy);
    }

    @Test
    void tokenSignedWithOtherHmacAlgorithmIsRejected() {
        // A 512-bit key is strong enough for HS512, so only the algorithm pin rejects it
        byte[] keyBytes = new byte[64];
        new SecureRandom().nextBytes(keyBytes);
        JwtService service = new JwtService(Base64.getEncoder().encodeToString(keyBytes),
                Duration.ofMinutes(1));
        String hs512 = Jwts.builder()
                .subject(ALICE_ID.toString())
                .signWith(Keys.hmacShaKeyFor(keyBytes), Jwts.SIG.HS512)
                .compact();

        assertThat(service.parseSubject(hs512)).isEmpty();
    }

    @Test
    void garbageTokenIsRejected() {
        assertUnresolved("not-a-jwt");
    }

    private void assertUnresolved(String token) {
        assertThat(jwtService.parseSubject(token)).isEmpty();
    }
}

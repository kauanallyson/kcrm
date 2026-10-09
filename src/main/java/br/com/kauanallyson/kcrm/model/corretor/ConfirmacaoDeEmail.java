package br.com.kauanallyson.kcrm.model.corretor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

// Link de Confirmação de E-mail vigente de um Corretor: vale 1 hora e uma única vez.
// Só o hash do token é guardado; o token em si só existe no e-mail enviado
@Entity
@Table(name = "confirmacoes_email")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConfirmacaoDeEmail {
    public static final Duration VALIDADE = Duration.ofHours(1);
    private static final SecureRandom RANDOM = new SecureRandom();

    @Id
    private UUID corretorId;

    @Column(nullable = false, unique = true, length = 64)
    private String tokenHash;

    @Column(nullable = false)
    private OffsetDateTime expiraEm;

    public static ConfirmacaoDeEmail para(UUID corretorId) {
        ConfirmacaoDeEmail confirmacao = new ConfirmacaoDeEmail();
        confirmacao.corretorId = corretorId;
        return confirmacao;
    }

    // Gera um token novo, invalidando o anterior; devolve-o em texto puro, para ir no e-mail
    public String renovar(OffsetDateTime agora) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        tokenHash = hash(token);
        expiraEm = agora.plus(VALIDADE);
        return token;
    }

    public static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean expirada(OffsetDateTime agora) {
        return !agora.isBefore(expiraEm);
    }
}

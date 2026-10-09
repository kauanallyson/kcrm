package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.exception.CredenciaisInvalidasException;
import br.com.kauanallyson.kcrm.exception.LinkDeConfirmacaoInvalidoException;
import br.com.kauanallyson.kcrm.model.common.Email;
import br.com.kauanallyson.kcrm.model.corretor.ConfirmacaoDeEmail;
import br.com.kauanallyson.kcrm.model.corretor.Corretor;
import br.com.kauanallyson.kcrm.repository.ConfirmacaoDeEmailRepository;
import br.com.kauanallyson.kcrm.repository.CorretorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

// Confirmação de E-mail: link de 1 hora e uso único; a conta nunca confirmada some depois de 7 dias
@Service
public class ConfirmacaoDeEmailService {
    public static final Duration PRAZO_PARA_CONFIRMAR = Duration.ofDays(7);
    private static final Logger log = LoggerFactory.getLogger(ConfirmacaoDeEmailService.class);

    private final ConfirmacaoDeEmailRepository confirmacaoRepository;
    private final CorretorRepository corretorRepository;
    private final EnvioDeEmail envioDeEmail;
    private final PasswordEncoder passwordEncoder;
    private final String urlBase;

    public ConfirmacaoDeEmailService(
            ConfirmacaoDeEmailRepository confirmacaoRepository,
            CorretorRepository corretorRepository,
            EnvioDeEmail envioDeEmail,
            PasswordEncoder passwordEncoder,
            @Value("${kcrm.confirmacao.url-base}") String urlBase
    ) {
        this.confirmacaoRepository = confirmacaoRepository;
        this.corretorRepository = corretorRepository;
        this.envioDeEmail = envioDeEmail;
        this.passwordEncoder = passwordEncoder;
        this.urlBase = urlBase;
    }

    // Gera um link novo (o anterior deixa de valer) e o envia ao e-mail cadastrado
    @Transactional
    public void enviarLink(Corretor corretor) {
        ConfirmacaoDeEmail confirmacao = confirmacaoRepository.findById(corretor.getId())
                .orElseGet(() -> ConfirmacaoDeEmail.para(corretor.getId()));
        String token = confirmacao.renovar(OffsetDateTime.now());
        confirmacaoRepository.save(confirmacao);

        String link = UriComponentsBuilder.fromUriString(urlBase).queryParam("token", token).toUriString();
        envioDeEmail.enviar(corretor.getEmail().value(), "Confirme seu e-mail no kcrm", """
                Olá, %s!

                Para começar a usar o kcrm, abra o link abaixo e confirme com a senha que você cadastrou:

                %s

                O link vale por 1 hora e pode ser usado uma única vez. Se expirar, peça um novo na tela de login.
                Se você não criou uma conta no kcrm, ignore este e-mail.
                """.formatted(corretor.getNome(), link));
    }

    // Pede a senha junto do link: quem abre um link não pedido não confirma a conta com a senha de outra pessoa.
    // Senha errada não gasta o link
    @Transactional
    public void confirmar(String token, String senha) {
        ConfirmacaoDeEmail confirmacao = Optional.ofNullable(token)
                .filter(t -> !t.isBlank())
                .flatMap(t -> confirmacaoRepository.findByTokenHash(ConfirmacaoDeEmail.hash(t)))
                .orElseThrow(LinkDeConfirmacaoInvalidoException::new);
        if (confirmacao.expirada(OffsetDateTime.now())) {
            throw new LinkDeConfirmacaoInvalidoException();
        }
        Corretor corretor = corretorRepository.findById(confirmacao.getCorretorId())
                .orElseThrow(LinkDeConfirmacaoInvalidoException::new);
        if (senha == null || !corretor.getSenhaHash().matches(senha, passwordEncoder)) {
            throw new CredenciaisInvalidasException();
        }
        corretor.confirmarEmail();
        // Uso único: o link some assim que confirma
        confirmacaoRepository.delete(confirmacao);
    }

    // Não revela se o e-mail existe: quem chama responde igual em todos os casos
    @Transactional
    public void reenviar(String email) {
        Email valido;
        try {
            valido = new Email(email);
        } catch (IllegalArgumentException e) {
            return;
        }
        corretorRepository.findByEmail(valido)
                .filter(corretor -> !corretor.isEmailConfirmado())
                .ifPresent(this::enviarSemRevelarFalha);
    }

    private void enviarSemRevelarFalha(Corretor corretor) {
        try {
            enviarLink(corretor);
        } catch (MailException e) {
            log.error("Falha ao reenviar o link de confirmação do Corretor {}", corretor.getId(), e);
        }
    }

    // Libera o e-mail de contas nunca confirmadas; os links delas caem junto (on delete cascade)
    @Scheduled(cron = "${kcrm.confirmacao.limpeza-cron:0 0 * * * *}")
    @Transactional
    public int apagarNaoConfirmadas() {
        int apagadas = corretorRepository.apagarNaoConfirmadosCriadosAntesDe(
                OffsetDateTime.now().minus(PRAZO_PARA_CONFIRMAR));
        if (apagadas > 0) {
            log.info("{} conta(s) não confirmada(s) apagada(s) após {} dias", apagadas, PRAZO_PARA_CONFIRMAR.toDays());
        }
        return apagadas;
    }
}

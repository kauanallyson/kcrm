package br.com.kauanallyson.kcrm;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Servidor SMTP em memória, um só para a suíte inteira; os testes filtram pelo destinatário (e-mails aleatórios)
public final class TestEmail {
    public static final GreenMail SMTP = iniciar();
    private static final Pattern TOKEN = Pattern.compile("[?&]token=([A-Za-z0-9_-]+)");

    private TestEmail() {
    }

    private static GreenMail iniciar() {
        GreenMail greenMail = new GreenMail(ServerSetupTest.SMTP.dynamicPort());
        greenMail.start();
        Runtime.getRuntime().addShutdownHook(new Thread(greenMail::stop));
        return greenMail;
    }

    public static int porta() {
        return SMTP.getSmtp().getPort();
    }

    public static List<MimeMessage> recebidos(String para) {
        return Arrays.stream(SMTP.getReceivedMessages())
                .filter(mensagem -> destinatario(mensagem).equalsIgnoreCase(para))
                .toList();
    }

    public static String texto(MimeMessage mensagem) {
        try {
            return mensagem.getContent().toString();
        } catch (IOException | MessagingException e) {
            throw new IllegalStateException(e);
        }
    }

    // Token do link do e-mail mais recente enviado a esse endereço
    public static String ultimoToken(String para) {
        List<MimeMessage> mensagens = recebidos(para);
        if (mensagens.isEmpty()) {
            throw new AssertionError("Nenhum e-mail enviado para " + para);
        }
        Matcher matcher = TOKEN.matcher(texto(mensagens.getLast()));
        if (!matcher.find()) {
            throw new AssertionError("E-mail sem link de confirmação");
        }
        return matcher.group(1);
    }

    private static String destinatario(MimeMessage mensagem) {
        try {
            return mensagem.getRecipients(Message.RecipientType.TO)[0].toString();
        } catch (MessagingException e) {
            throw new IllegalStateException(e);
        }
    }
}

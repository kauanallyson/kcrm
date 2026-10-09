package br.com.kauanallyson.kcrm.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// E-mails em texto simples por SMTP genérico (Mailpit em dev, Resend em prod)
@Service
public class EnvioDeEmail {
    private final JavaMailSender mailSender;
    private final String remetente;

    public EnvioDeEmail(JavaMailSender mailSender, @Value("${kcrm.email.remetente}") String remetente) {
        this.mailSender = mailSender;
        this.remetente = remetente;
    }

    public void enviar(String para, String assunto, String texto) {
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(para);
        mensagem.setSubject(assunto);
        mensagem.setText(texto);
        mailSender.send(mensagem);
    }
}

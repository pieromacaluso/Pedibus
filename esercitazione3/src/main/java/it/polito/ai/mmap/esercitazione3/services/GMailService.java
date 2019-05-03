package it.polito.ai.mmap.esercitazione3.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class GMailService {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JavaMailSender mailSender;

    public void sendRegisterEmail(String email) {
        sendMail(email, "<p>Clicca per confermare account</p><a href='#'>Link</a>");
        logger.info("Inviata register email a: " + email);
    }

    public void sendRecoverEmail(String email) {
        sendMail(email, "<p>Clicca per modificare la password</p><a href='#'>Link</a>");
        logger.info("Inviata recover email a: " + email);
    }

    // TODO: settare link per rispettare sicurezza
    private void sendMail(String email, String msg) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            // true = multi part message
            helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            // true = text/html
            helper.setText(msg, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error(e.toString());
        }

    }
}

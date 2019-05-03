package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.repository.UserRepository;
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

    @Autowired
    UserRepository userRepository;

    public void sendRegisterEmail(UserEntity userEntity) {
        String msg = "http://localhost:8080/confirm/" + userEntity.getId();
        sendMail(userEntity.getUsername(), "<p>Clicca per confermare account</p><a href='" + msg + "'>Confirmation Link</a>");
        logger.info("Inviata register email a: " + userEntity.getUsername());
    }

    public void sendRecoverEmail(UserEntity userEntity) {
        sendMail(userEntity.getUsername(), "<p>Clicca per modificare la password</p><a href='#'>Link</a>");
        logger.info("Inviata recover email a: " + userEntity.getUsername());
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

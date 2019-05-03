package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.objectDTO.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class GMailSender {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JavaMailSender mailSender;

    // TODO: settare link per rispettare sicurezza
    public void sendEmail(UserDTO userDTO) {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            // true = multi part message
            helper = new MimeMessageHelper(message, true);
            helper.setTo(userDTO.getEmail());
            // true = text/html
            helper.setText("<p>Clicca per confermare account</p><a href='#'>Link</a>", true);
            mailSender.send(message);
            logger.info("Inviata email a: " + userDTO.getEmail());
        } catch (MessagingException e) {
            e.printStackTrace();
        }


    }


}

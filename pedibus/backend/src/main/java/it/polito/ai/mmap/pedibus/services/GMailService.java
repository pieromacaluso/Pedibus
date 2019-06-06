package it.polito.ai.mmap.pedibus.services;

import it.polito.ai.mmap.pedibus.entity.RecoverTokenEntity;
import it.polito.ai.mmap.pedibus.entity.UserEntity;
import it.polito.ai.mmap.pedibus.repository.RecoverTokenRepository;
import it.polito.ai.mmap.pedibus.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
public class GMailService {


    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JavaMailSender mailSender;

    @Async("threadPoolTaskExecutor")
    public void sendMail(String email, String msg, String subject) {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = null;
        try {
            // true = multi part message
            helper = new MimeMessageHelper(message, true);
            helper.setSubject(subject);
            helper.setTo(email);
            // true = text/html
            helper.setText(msg, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            logger.error(e.toString());
        }

    }
}

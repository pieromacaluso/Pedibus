package it.polito.ai.mmap.esercitazione3.services;

import it.polito.ai.mmap.esercitazione3.entity.RecoverTokenEntity;
import it.polito.ai.mmap.esercitazione3.entity.UserEntity;
import it.polito.ai.mmap.esercitazione3.repository.RecoverTokenRepository;
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

    private static final String baseURL = "http://localhost:8080/";
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecoverTokenRepository tokenRepository;


    public void sendRegisterEmail(UserEntity userEntity) {
        String href = baseURL + "confirm/" + userEntity.getId();
        //TODO trovare un metodo nativo di spring per gestire l'asincronicità
        new Thread(() -> sendMail(userEntity.getUsername(), "<p>Clicca per confermare account</p><a href='" + href + "'>Confirmation Link</a>")).start();
        logger.info("Inviata register email a: " + userEntity.getUsername());
    }

    public void sendRecoverEmail(UserEntity userEntity) {
        RecoverTokenEntity tokenEntity = new RecoverTokenEntity(userEntity.getUsername());
        tokenRepository.save(tokenEntity);
        String href = baseURL + "recover/" + tokenEntity.getTokenValue();
        //TODO trovare un metodo nativo di spring per gestire l'asincronicità
        new Thread(() -> sendMail(userEntity.getUsername(), "<p>Clicca per modificare la password</p><a href='" + href + "'>Reset your password</a>")).start();
        logger.info("Inviata recover email a: " + userEntity.getUsername());
    }


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

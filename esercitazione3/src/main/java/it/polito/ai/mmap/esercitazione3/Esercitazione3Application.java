package it.polito.ai.mmap.esercitazione3;

import it.ozimov.springboot.mail.configuration.EnableEmailTools;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableEmailTools
public class Esercitazione3Application {

    public static void main(String[] args) {
        SpringApplication.run(Esercitazione3Application.class, args);
    }

}

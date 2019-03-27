package it.polito.mmap.esercizio1;

import it.polito.mmap.esercizio1.viewModels.FormUserRegistration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class Esercizio1Application {

    @Bean
    public ConcurrentHashMap<String, FormUserRegistration> users() {
        return new ConcurrentHashMap<>();
    }

    public static void main(String[] args) {
        SpringApplication.run(Esercizio1Application.class, args);
    }

}

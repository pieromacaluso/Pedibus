package it.polito.mmap.esercitazione1;

import it.polito.mmap.esercitazione1.model.User;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class Esercitazione1Application {

    @Bean
    public ConcurrentHashMap<String, User> users() {
        return new ConcurrentHashMap<>();
    }

    public static void main(String[] args) {
        SpringApplication.run(Esercitazione1Application.class, args);
    }

}

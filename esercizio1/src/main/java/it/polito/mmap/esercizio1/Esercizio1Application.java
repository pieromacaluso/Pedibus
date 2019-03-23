package it.polito.mmap.esercizio1;

import it.polito.mmap.esercizio1.viewModels.UserVM;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.util.concurrent.ConcurrentHashMap;

@SpringBootApplication
public class Esercizio1Application {


    @Bean
    public ConcurrentHashMap<String, UserVM> users(){
        return new ConcurrentHashMap<String, UserVM>();
    }

    public static void main(String[] args) {
        SpringApplication.run(Esercizio1Application.class, args);
    }

}

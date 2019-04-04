package it.polito.ai.mmap.esercitazione2.config;


import com.mongodb.Mongo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public Mongo mongo() throws Exception {
        return new Mongo("mongodb+srv://mmap:<password>@esercitazione2-0buq8.mongodb.net/test?retryWrites=true");
    }

    @Bean
    public MongoTemplate mongoTemplate()
    {
        return new MongoTemplate(mongo(),"test");
    }

}
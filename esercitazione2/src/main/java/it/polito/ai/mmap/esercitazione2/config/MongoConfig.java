package it.polito.ai.mmap.esercitazione2.config;


import com.mongodb.Mongo;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClient mongoClient() throws Exception {
        //mongodb+srv://mmap:mmapmmap1!@esercitazione2-0buq8.mongodb.net/test?retryWrites=true
        MongoClientURI uri = new MongoClientURI("mongodb+srv://mmap:<password>@esercitazione2-0buq8.mongodb.net/test?retryWrites=true");
        return new MongoClient(uri);
    }

    @Bean
    public MongoTemplate mongoTemplate()
    {
        try {
            return new MongoTemplate(mongoClient(),"test");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
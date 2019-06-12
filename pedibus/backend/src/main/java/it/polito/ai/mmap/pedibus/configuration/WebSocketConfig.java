package it.polito.ai.mmap.pedibus.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * recente -> https://www.baeldung.com/spring-websockets-sendtouser
 * meno recente -> https://www.baeldung.com/websockets-spring
 */
//TODO copiati e incollati, capire bene cosa lasciare e cosa no
//TODO sicurezza ?
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/messages").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue", "/handled","/reservation");
        config.setApplicationDestinationPrefixes("/app");
    }
}
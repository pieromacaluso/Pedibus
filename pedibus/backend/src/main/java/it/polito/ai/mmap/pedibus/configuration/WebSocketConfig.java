package it.polito.ai.mmap.pedibus.configuration;

import it.polito.ai.mmap.pedibus.services.JwtTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * recente -> https://www.baeldung.com/spring-websockets-sendtouser
 * meno recente -> https://www.baeldung.com/websockets-spring
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private JwtTokenService jwtTokenService;


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/messages").setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/handled", "/reservation", "/dispws", "dispws-add", "/turnows", "/turno-aperto", "/turno-chiuso", "/turno confermato", "/reminder-turno");
    }
}

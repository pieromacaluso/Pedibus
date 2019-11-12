package it.polito.ai.mmap.pedibus.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

import static org.springframework.messaging.simp.SimpMessageType.MESSAGE;
import static org.springframework.messaging.simp.SimpMessageType.SUBSCRIBE;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        // TODO: modifiche per autenticazione
        messages
//                .nullDestMatcher().authenticated()
//                .simpSubscribeDestMatchers("/*").permitAll()
//                .simpDestMatchers("/app/**").hasRole("USER")
//                .simpSubscribeDestMatchers("/user/**", "/topic/friends/*").hasRole("USER")
//                .simpTypeMatchers(MESSAGE, SUBSCRIBE).denyAll()
                .simpSubscribeDestMatchers("/admin/**").hasRole("ADMIN")
                .anyMessage().authenticated();
//        .anyMessage().permitAll();

    }
    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}

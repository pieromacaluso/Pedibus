package it.polito.ai.mmap.pedibus.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.messaging.MessageSecurityMetadataSourceRegistry;
import org.springframework.security.config.annotation.web.socket.AbstractSecurityWebSocketMessageBrokerConfigurer;

@Configuration
public class WebSocketSecurityConfig extends AbstractSecurityWebSocketMessageBrokerConfigurer {

    @Override
    protected void configureInbound(MessageSecurityMetadataSourceRegistry messages) {
        messages
                .simpSubscribeDestMatchers("/reservation/**").hasAnyRole("ADMIN", "SYSTEM-ADMIN", "GUIDE")
                .simpSubscribeDestMatchers("/dispws/**").hasAnyRole("ADMIN", "SYSTEM-ADMIN", "GUIDE")
                .simpSubscribeDestMatchers("/turnows/**").hasAnyRole("ADMIN", "SYSTEM-ADMIN", "GUIDE")
                .simpSubscribeDestMatchers("/notifiche/**").hasRole("ADMIN")
                .simpSubscribeDestMatchers("/child/**").hasRole("USER")
                .simpSubscribeDestMatchers("/anagrafica/**").hasAnyRole("ADMIN", "SYSTEM-ADMIN")
                .anyMessage().authenticated();
    }

    @Override
    protected boolean sameOriginDisabled() {
        return true;
    }
}

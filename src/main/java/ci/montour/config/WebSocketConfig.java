package ci.montour.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final MontourProperties properties;

    public WebSocketConfig(MontourProperties properties) {
        this.properties = properties;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Les navigateurs se connectent sur ws://<serveur>/ws
        registry.addEndpoint("/ws").setAllowedOrigins(properties.frontendUrl());
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Le serveur publie sur /topic/... et les navigateurs s'y abonnent.
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }
}

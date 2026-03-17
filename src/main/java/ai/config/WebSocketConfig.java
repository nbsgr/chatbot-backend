package ai.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig
        implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private WebSocketAuthInterceptor authInterceptor;

    @Override
    public void registerStompEndpoints(
            StompEndpointRegistry registry) {

        System.out.println("===== WEBSOCKET CONFIG =====");
        System.out.println("Registering STOMP endpoint: /ws-chat");

        registry
            .addEndpoint("/ws-chat")
            .setAllowedOriginPatterns("*")
            .withSockJS();

        System.out.println("SockJS enabled for /ws-chat");
    }

    @Override
    public void configureMessageBroker(
            MessageBrokerRegistry registry) {

        System.out.println("Configuring Message Broker");

        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");

        System.out.println("Simple broker enabled for /topic and /queue");
        System.out.println("Application prefix: /app");
        System.out.println("User destination prefix: /user");
    }

    @Override
    public void configureClientInboundChannel(
            ChannelRegistration registration) {

        System.out.println("Attaching WebSocketAuthInterceptor to inbound channel");

        registration.interceptors(authInterceptor);

        System.out.println("WebSocketAuthInterceptor attached successfully");
    }
}
package ai.config;

import ai.manager.JWTManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    @Autowired
    private JWTManager jwtManager;

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(
                        message,
                        StompHeaderAccessor.class
                );

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String authHeader =
                    accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null ||
                !authHeader.startsWith("Bearer ")) {

                System.out.println("WS: Missing or invalid Authorization header");
                return message;
            }

            String token =
                    authHeader.substring(7).trim();

            String email =
                    jwtManager.getEmailFromToken(token);

            Integer role =
                    jwtManager.getRoleFromToken(token);

            if (email == null || role == null) {
                System.out.println("WS: Invalid token");
                return message;
            }

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.singletonList(
                                    new SimpleGrantedAuthority("ROLE_USER")
                            )
                    );

            // 🔥 CRITICAL FIX
            accessor.setUser(authentication);
            accessor.setLeaveMutable(true);

            System.out.println("WebSocket Authentication SUCCESS for user: " + email);
        }

        return message;
    }
}
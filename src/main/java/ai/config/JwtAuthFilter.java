package ai.config;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ai.dto.ApiResponse;
import ai.manager.JWTManager;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JWTManager jwtManager;

    private static final List<String> PUBLIC_URL_PREFIXES = List.of(
            "/users/request-signup-otp",
            "/users/verify-signup-otp",
            "/users/signup",
            "/users/signin",
            "/users/request-password-reset",
            "/users/reset-password",
            "/ws-chat",
            "/topic",
            "/app"
    );

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();
        String method = request.getMethod();

        System.out.println("===== JWT FILTER START =====");
        System.out.println("Request Path: " + path);
        System.out.println("Request Method: " + method);

        // Allow OPTIONS (CORS preflight)
        if ("OPTIONS".equalsIgnoreCase(method)) {
            System.out.println("OPTIONS request allowed");
            filterChain.doFilter(request, response);
            return;
        }

        // Allow public paths
        for (String prefix : PUBLIC_URL_PREFIXES) {
            if (path.startsWith(prefix)) {
                System.out.println("Public URL matched: " + prefix);
                filterChain.doFilter(request, response);
                return;
            }
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("Authorization header missing or invalid");
            sendUnauthorized(response, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("Extracted Token: " + token);

        try {
            String email = jwtManager.getEmailFromToken(token);
            Integer role = jwtManager.getRoleFromToken(token);

            System.out.println("Extracted Email: " + email);
            System.out.println("Extracted Role: " + role);

            if (email == null || role == null) {
                System.out.println("Token invalid (email or role null)");
                sendUnauthorized(response, "Invalid token");
                return;
            }

            String roleName;

            if (role == 1) {
                roleName = "ROLE_USER";
            } else {
                System.out.println("Invalid role value: " + role);
                sendForbidden(response, "Invalid role");
                return;
            }

            SimpleGrantedAuthority authority =
                    new SimpleGrantedAuthority(roleName);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(authority)
                    );

            SecurityContextHolder
                    .getContext()
                    .setAuthentication(authentication);

            System.out.println("Authentication set for user: " + email);
            System.out.println("===== JWT FILTER SUCCESS =====");

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            System.out.println("Exception while validating token: " + e.getMessage());
            sendUnauthorized(response, "Invalid or expired token");
        }
    }

    private void sendUnauthorized(HttpServletResponse response, String message) throws IOException {
        ApiResponse<Void> apiResponse = new ApiResponse<>(401, message, null);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }

    private void sendForbidden(HttpServletResponse response, String message) throws IOException {
        ApiResponse<Void> apiResponse = new ApiResponse<>(403, message, null);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json");
        new ObjectMapper().writeValue(response.getWriter(), apiResponse);
    }
}
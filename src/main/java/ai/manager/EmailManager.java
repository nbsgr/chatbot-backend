package ai.manager;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import ai.dto.ApiResponse;

import java.util.*;

@Service
public class EmailManager {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.api.url}")
    private String brevoApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // =========================
    // Generic Email Sender
    // =========================
    public ApiResponse<Void> sendEmail(String toEmail, String subject, String message) {
        try {
            Map<String, Object> payload = new HashMap<>();

            Map<String, String> sender = new HashMap<>();
            sender.put("email", "balaganesh7944978@gmail.com"); // must be verified
            payload.put("sender", sender);

            List<Map<String, String>> toList = new ArrayList<>();
            Map<String, String> to = new HashMap<>();
            to.put("email", toEmail);
            toList.add(to);

            payload.put("to", toList);
            payload.put("subject", subject);
            payload.put("textContent", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", brevoApiKey);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response =
                    restTemplate.postForEntity(brevoApiUrl, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                return new ApiResponse<>(200, "Email sent successfully", null);
            } else {
                return new ApiResponse<>(500, "Email sending failed", null);
            }

        } catch (Exception e) {
            return new ApiResponse<>(500, "Email error: " + e.getMessage(), null);
        }
    }

    // =========================
    // OTP Email
    // =========================
    public ApiResponse<Void> sendOtpEmail(String email, String otp) {
        String subject = "Your Signup OTP";
        String body = "Your OTP is: " + otp + "\nValid for 10 minutes.";
        return sendEmail(email, subject, body);
    }

    // =========================
    // Password Reset Email (LINK-STYLE)
    // =========================
    public ApiResponse<Void> sendPasswordResetEmail(String email, String resetLink) {
        String subject = "Reset Your Password";
        String body = "Click the link below to reset your password:\n\n"
                    + resetLink
                    + "\n\nThis link is valid for 10 minutes.";

        return sendEmail(email, subject, body);
    }
}

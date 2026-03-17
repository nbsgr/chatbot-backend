package ai.manager;

import java.security.SecureRandom;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import ai.dto.ApiResponse;
import ai.model.UserToken;
import ai.model.Users;
import ai.rep.UserTokenRepository;
import ai.rep.UsersRepository;

@Service
public class UsersManager {

    @Autowired
    private UsersRepository UR;

    @Autowired
    private UserTokenRepository tokenRepo;

    @Autowired
    private EmailManager EM;

    @Autowired
    private JWTManager JWT;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private final SecureRandom random = new SecureRandom();

    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int RESET_EXPIRY_MINUTES = 10;
    private static final int MAX_OTP_ATTEMPTS = 5;

    // =========================
    // Generate 6-digit OTP
    // =========================
    private String generateOtp() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    // =========================
    // Request Signup OTP
    // =========================
    @Transactional
    public ApiResponse<Void> requestSignupOtp(String email) {

        if (UR.existsByEmail(email)) {
            return new ApiResponse<>(409, "Email already registered", null);
        }

        tokenRepo.deleteAllByEmailAndType(email, "SIGNUP_OTP");

        String otp = generateOtp();
        String otpHash = passwordEncoder.encode(otp);

        UserToken token = new UserToken();
        token.setEmail(email);
        token.setTokenHash(otpHash);
        token.setType("SIGNUP_OTP");
        token.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES));
        token.setUsed(false);
        token.setAttempts(0);
        token.setLocked(false);
        token.setResendCount(1);
        token.setLastSentAt(LocalDateTime.now());

        tokenRepo.save(token);
        EM.sendOtpEmail(email, otp);

        return new ApiResponse<>(200, "OTP sent successfully", null);
    }

    // =========================
    // Verify Signup OTP
    // =========================
    public ApiResponse<Void> verifySignupOtp(String email, String otp) {

        Optional<UserToken> optionalToken =
                tokenRepo.findByEmailAndTypeAndUsedFalse(email, "SIGNUP_OTP");

        if (optionalToken.isEmpty()) {
            return new ApiResponse<>(401, "OTP not found or already used", null);
        }

        UserToken token = optionalToken.get();

        if (token.getLocked()) {
            return new ApiResponse<>(403, "OTP locked due to multiple failures", null);
        }

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            return new ApiResponse<>(401, "OTP expired", null);
        }

        boolean matches = passwordEncoder.matches(otp, token.getTokenHash());

        if (!matches) {
            token.setAttempts(token.getAttempts() + 1);

            if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
                token.setLocked(true);
            }

            tokenRepo.save(token);
            return new ApiResponse<>(401, "Invalid OTP", null);
        }

        token.setUsed(true);
        tokenRepo.save(token);

        return new ApiResponse<>(200, "OTP verified successfully", null);
    }

    // =========================
    // Signup (OTP mandatory)
    // =========================
    public ApiResponse<Void> addUsers(Users user) {

        if (UR.existsByEmail(user.getEmail())) {
            return new ApiResponse<>(409, "Email already exists", null);
        }

        Optional<UserToken> token =
                tokenRepo.findByEmailAndTypeAndUsedFalse(user.getEmail(), "SIGNUP_OTP");

        if (token.isPresent()) {
            return new ApiResponse<>(401, "OTP not verified", null);
        }

        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);

        UR.save(user);

        return new ApiResponse<>(200, "User Registered Successfully", null);
    }

    // =========================
    // Login (JWT now includes ROLE)
    // =========================
    public ApiResponse<String> validateCredentials(String email, String password) {

        Optional<Users> optionalUser = UR.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(401, "Invalid Credentials", null);
        }

        Users user = optionalUser.get();

        boolean matches = passwordEncoder.matches(password, user.getPassword());

        if (!matches) {
            return new ApiResponse<>(401, "Invalid Credentials", null);
        }

        // 🔥 JWT now contains ROLE
        String token = JWT.generateToken(email, user.getRole());
        return new ApiResponse<>(200, "Login Successful", token);
    }

    // =========================
    // Request Password Reset
    // =========================
    public ApiResponse<Void> requestPasswordReset(String email) {

        Optional<Users> optionalUser = UR.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(404, "User not found", null);
        }

        tokenRepo.deleteAllByEmailAndType(email, "PASSWORD_RESET");

        String rawToken = UUID.randomUUID().toString().replace("-", "");
        String hashedToken = passwordEncoder.encode(rawToken);

        UserToken token = new UserToken();
        token.setEmail(email);
        token.setTokenHash(hashedToken);
        token.setType("PASSWORD_RESET");
        token.setExpiryTime(LocalDateTime.now().plusMinutes(RESET_EXPIRY_MINUTES));
        token.setUsed(false);
        token.setAttempts(0);
        token.setLocked(false);

        tokenRepo.save(token);

        String resetLink =
                "https://2sg5mh11-5173.inc1.devtunnels.ms/reset-password?email=" + email + "&token=" + rawToken;

        EM.sendPasswordResetEmail(email, resetLink);

        return new ApiResponse<>(200, "Password reset link sent", null);
    }

    // =========================
    // Reset Password
    // =========================
    public ApiResponse<Void> resetPassword(String email, String rawToken, String newPassword) {

        Optional<UserToken> optionalToken =
                tokenRepo.findByEmailAndTypeAndUsedFalse(email, "PASSWORD_RESET");

        if (optionalToken.isEmpty()) {
            return new ApiResponse<>(401, "Invalid or used token", null);
        }

        UserToken token = optionalToken.get();

        if (token.getLocked()) {
            return new ApiResponse<>(403, "Token locked", null);
        }

        if (token.getExpiryTime().isBefore(LocalDateTime.now())) {
            return new ApiResponse<>(401, "Token expired", null);
        }

        boolean matches = passwordEncoder.matches(rawToken, token.getTokenHash());
        if (!matches) {
            token.setAttempts(token.getAttempts() + 1);
            if (token.getAttempts() >= MAX_OTP_ATTEMPTS) {
                token.setLocked(true);
            }
            tokenRepo.save(token);
            return new ApiResponse<>(401, "Invalid token", null);
        }

        Optional<Users> optionalUser = UR.findByEmail(email);
        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(404, "User not found", null);
        }

        Users user = optionalUser.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        UR.save(user);

        token.setUsed(true);
        tokenRepo.save(token);

        return new ApiResponse<>(200, "Password reset successful", null);
    }

    // =========================
    // Get Fullname (JWT-based)
    // =========================
    public ApiResponse<String> getFullname(String token) {

        String email = JWT.getEmailFromToken(token);

        if (email == null) {
            return new ApiResponse<>(401, "Token Expired or Invalid", null);
        }

        Optional<Users> optionalUser = UR.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return new ApiResponse<>(404, "User not found", null);
        }

        return new ApiResponse<>(200, "Success", optionalUser.get().getFullname());
    }
}

package ai.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ai.dto.ApiResponse;
import ai.manager.UsersManager;
import ai.model.Users;

@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UsersManager UM;

    // =========================
    // 1️⃣ Request Signup OTP
    // =========================
    @PostMapping("/request-signup-otp")
    public ResponseEntity<ApiResponse<Void>> requestSignupOtp(
            @RequestBody Map<String, String> data) {

        ApiResponse<Void> response = UM.requestSignupOtp(data.get("email"));
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // 2️⃣ Verify Signup OTP
    // =========================
    @PostMapping("/verify-signup-otp")
    public ResponseEntity<ApiResponse<Void>> verifySignupOtp(
            @RequestBody Map<String, String> data) {

        ApiResponse<Void> response =
                UM.verifySignupOtp(data.get("email"), data.get("otp"));

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // 3️⃣ Signup (OTP mandatory)
    // =========================
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody Users user) {
        ApiResponse<Void> response = UM.addUsers(user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // 4️⃣ Login
    // =========================
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<String>> signin(@RequestBody Users user) {
        ApiResponse<String> response =
                UM.validateCredentials(user.getEmail(), user.getPassword());

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // 5️⃣ Get Fullname (JWT protected)
    // =========================
    @PostMapping("/getfullname")
    public ResponseEntity<ApiResponse<String>> getFullname(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "").trim();

        ApiResponse<String> response = UM.getFullname(token);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // 6️⃣ Request Password Reset (link-based)
    // =========================
    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
            @RequestBody Map<String, String> data) {

        ApiResponse<Void> response =
                UM.requestPasswordReset(data.get("email"));

        return ResponseEntity.status(response.getStatus()).body(response);
    }

    // =========================
    // 7️⃣ Reset Password
    // =========================
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody Map<String, String> data) {

        ApiResponse<Void> response =
                UM.resetPassword(
                        data.get("email"),
                        data.get("token"),
                        data.get("newPassword")
                );

        return ResponseEntity.status(response.getStatus()).body(response);
    }
}

package br.com.kauanallyson.kcrm.controller;

import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.LoginRequest;
import br.com.kauanallyson.kcrm.dto.TokenResponse;
import br.com.kauanallyson.kcrm.dto.UserRequest;
import br.com.kauanallyson.kcrm.dto.UserResponse;
import br.com.kauanallyson.kcrm.service.AuthService;
import br.com.kauanallyson.kcrm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<TokenResponse> register(@RequestBody @Valid UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(@AuthenticationPrincipal AuthenticatedUser principal) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(principal.id())));
    }
}

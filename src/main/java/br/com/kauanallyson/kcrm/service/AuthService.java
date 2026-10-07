package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.auth.JwtService;
import br.com.kauanallyson.kcrm.auth.AuthenticatedUser;
import br.com.kauanallyson.kcrm.dto.LoginRequest;
import br.com.kauanallyson.kcrm.dto.TokenResponse;
import br.com.kauanallyson.kcrm.dto.UserRequest;
import br.com.kauanallyson.kcrm.exception.InvalidCredentialsException;
import br.com.kauanallyson.kcrm.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public TokenResponse login(LoginRequest request) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(request.email(), request.password()));
        } catch (AuthenticationException e) {
            throw new InvalidCredentialsException();
        }
        return jwtService.issue(((AuthenticatedUser) authentication.getPrincipal()).id());
    }

    public TokenResponse register(UserRequest request) {
        User user = userService.create(request);
        return jwtService.issue(user.getId());
    }
}

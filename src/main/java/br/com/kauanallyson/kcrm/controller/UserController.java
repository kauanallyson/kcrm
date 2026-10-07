package br.com.kauanallyson.kcrm.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import br.com.kauanallyson.kcrm.dto.UserRequest;
import br.com.kauanallyson.kcrm.dto.UserResponse;
import br.com.kauanallyson.kcrm.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<UserResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(UserResponse.from(userService.findById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<UserResponse> update(@PathVariable UUID id, @RequestBody @Valid UserRequest request) {
        return ResponseEntity.ok(UserResponse.from(userService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("#id == principal.id")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

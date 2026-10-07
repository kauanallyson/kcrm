package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.dto.UserRequest;
import br.com.kauanallyson.kcrm.exception.UserAlreadyExistsException;
import br.com.kauanallyson.kcrm.exception.UserNotFoundException;
import br.com.kauanallyson.kcrm.model.User;
import br.com.kauanallyson.kcrm.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User create(UserRequest request) {
        if (userRepository.existsByEmailOrCpf(request.email(), request.cpf())) {
            throw new UserAlreadyExistsException();
        }
        User user = new User(request.name(), request.cpf(), request.email(),
                passwordEncoder.encode(request.password()), request.phone(), request.address());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User update(UUID id, UserRequest request) {
        User user = findById(id);
        if (userRepository.existsByEmailOrCpfAndIdNot(request.email(), request.cpf(), id)) {
            throw new UserAlreadyExistsException();
        }
        // Keep the stored hash when the password didn't change, so a profile edit isn't a password change
        String password = passwordEncoder.matches(request.password(), user.getPassword())
                ? user.getPassword()
                : passwordEncoder.encode(request.password());
        user.update(request.name(), request.cpf(), request.email(), password, request.phone(), request.address());
        return user;
    }

    @Transactional
    public void delete(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}

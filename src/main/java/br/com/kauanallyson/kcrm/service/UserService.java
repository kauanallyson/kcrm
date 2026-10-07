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
        User.Profile profile = request.toProfile();
        if (userRepository.existsByEmailOrCpf(profile.email(), profile.cpf())) {
            throw new UserAlreadyExistsException();
        }
        return userRepository.save(User.register(profile, request.password(), passwordEncoder));
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User update(UUID id, UserRequest request) {
        User user = findById(id);
        User.Profile profile = request.toProfile();
        if (userRepository.existsByEmailOrCpfAndIdNot(profile.email(), profile.cpf(), id)) {
            throw new UserAlreadyExistsException();
        }
        user.updateProfile(profile);
        user.changePassword(request.password(), passwordEncoder);
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

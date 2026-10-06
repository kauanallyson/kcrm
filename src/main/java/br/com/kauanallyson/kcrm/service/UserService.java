package br.com.kauanallyson.kcrm.service;

import br.com.kauanallyson.kcrm.dto.UserRequest;
import br.com.kauanallyson.kcrm.exception.UserAlreadyExistsException;
import br.com.kauanallyson.kcrm.exception.UserNotFoundException;
import br.com.kauanallyson.kcrm.model.User;
import br.com.kauanallyson.kcrm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User create(UserRequest request) {
        if (userRepository.existsByCpf(request.cpf()) || userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("Invalid cpf or e-mail");
        }
        User user = new User(request.name(), request.cpf(), request.email(),
                request.password(), request.phone(), request.address());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User update(UUID id, UserRequest request) {
        User user = findById(id);
        if (userRepository.existsByCpfAndIdNot(request.cpf(), id)
                || userRepository.existsByEmailAndIdNot(request.email(), id)) {
            throw new UserAlreadyExistsException("Invalid cpf or e-mail");
        }
        user.update(request.name(), request.cpf(), request.email(),
                request.password(), request.phone(), request.address());
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

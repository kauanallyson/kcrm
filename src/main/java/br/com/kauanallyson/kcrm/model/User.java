package br.com.kauanallyson.kcrm.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
@ToString
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private Cpf cpf;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private Email email;

    @Column(name = "password", nullable = false)
    private PasswordHash passwordHash;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    @CreationTimestamp
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    public static User register(Profile profile, String rawPassword, PasswordEncoder encoder) {
        User user = new User();
        user.updateProfile(profile);
        user.passwordHash = PasswordHash.encode(rawPassword, encoder);
        return user;
    }

    public void updateProfile(Profile profile) {
        this.name = profile.name();
        this.cpf = profile.cpf();
        this.email = profile.email();
        this.phone = profile.phone();
        this.address = profile.address();
    }

    // Re-hashing an unchanged password would make every profile edit look like a password change
    public void changePassword(String rawPassword, PasswordEncoder encoder) {
        if (passwordMatches(rawPassword, encoder)) {
            return;
        }
        this.passwordHash = PasswordHash.encode(rawPassword, encoder);
    }

    public boolean passwordMatches(String rawPassword, PasswordEncoder encoder) {
        return passwordHash.matches(rawPassword, encoder);
    }

    public record Profile(String name, Cpf cpf, Email email, String phone, String address) {
    }
}

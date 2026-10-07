package br.com.kauanallyson.kcrm.model;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {
    private static final PasswordEncoder ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();
    private static final User.Profile PROFILE = new User.Profile(
            "Alice", new Cpf("52998224725"), new Email("alice@test.com"), "1", "a");

    @Test
    void registeredPasswordIsHashed() {
        User user = User.register(PROFILE, "secret123", ENCODER);

        assertThat(user.getPasswordHash().value()).doesNotContain("secret123");
        assertThat(user.passwordMatches("secret123", ENCODER)).isTrue();
    }

    @Test
    void unchangedPasswordKeepsItsHash() {
        User user = User.register(PROFILE, "secret123", ENCODER);
        PasswordHash before = user.getPasswordHash();

        user.changePassword("secret123", ENCODER);

        assertThat(user.getPasswordHash()).isSameAs(before);
    }

    @Test
    void newPasswordIsRehashed() {
        User user = User.register(PROFILE, "secret123", ENCODER);

        user.changePassword("other-secret", ENCODER);

        assertThat(user.passwordMatches("other-secret", ENCODER)).isTrue();
        assertThat(user.passwordMatches("secret123", ENCODER)).isFalse();
    }
}
